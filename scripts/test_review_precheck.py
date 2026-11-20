#!/usr/bin/env python3
"""Deterministic first pass over changed test files. No AI, no network.

Usage: python3 scripts/test_review_precheck.py [base_ref]   (default origin/main)
Prints a Markdown report. Exit code is always 0: advisory only.
"""
import re
import subprocess
import sys

BASE = sys.argv[1] if len(sys.argv) > 1 else "origin/main"

RULES = [
    (r"Thread\.sleep\(", "high", "Thread.sleep found. Use Playwright auto-wait or a web-first assertion."),
    (r"locator\(\s*\"(//|xpath=/)", "medium", "Absolute XPath locator. Prefer role, label or data-test."),
    (r"(password|passwd|secret|token|apikey)\s*=\s*\"[^\"]{4,}\"", "high", "Possible hard-coded secret. Read it from Config or env."),
    (r"enabled\s*=\s*false|@Disabled", "medium", "Test disabled. Use the quarantine group with ticket, owner and expiry instead."),
    (r"implicitlyWait\(", "medium", "Implicit wait. Use explicit WebDriverWait conditions instead."),
    (r"dependsOnMethods|dependsOnGroups", "medium", "Test depends on another test. Tests must be independent."),
    (r"System\.out\.print", "low", "System.out in a test. Use Allure steps or logging."),
    (r"static\s+(?!final)[A-Za-z<>\[\], ]+\s+\w+\s*;", "medium", "Static mutable field. Breaks parallel runs."),
]
ASSERT_HINT = re.compile(r"assertThat|assertEquals|assertTrue|assertFalse|assertThrows|assertAll|hasText|hasCount|isVisible|expect\(")


def changed_files():
    try:
        out = subprocess.run(
            ["git", "diff", "--name-only", f"{BASE}...HEAD", "--", "src/test"],
            capture_output=True, text=True, check=True).stdout
    except Exception:
        out = subprocess.run(["git", "ls-files", "src/test"], capture_output=True, text=True).stdout
    return [f for f in out.splitlines() if f.endswith(".java")]


def test_methods(src):
    """Yield (name, line_no, body) for each @Test method, using brace matching."""
    for m in re.finditer(r"@Test\b[^{;]*?\b(\w+)\s*\([^)]*\)[^{]*\{", src, re.S):
        depth, i = 1, m.end()
        while i < len(src) and depth:
            depth += {"{": 1, "}": -1}.get(src[i], 0)
            i += 1
        yield m.group(1), src.count("\n", 0, m.start()) + 1, src[m.end():i]


def main():
    findings = []
    files = changed_files()
    for path in files:
        try:
            src = open(path, encoding="utf-8").read()
        except OSError:
            continue
        for n, line in enumerate(src.splitlines(), 1):
            if line.strip().startswith("//"):
                continue
            for pat, sev, msg in RULES:
                if re.search(pat, line, re.I):
                    findings.append((path, n, sev, msg))
        if "/tests/" in path:
            for name, line_no, body in test_methods(src):
                if not ASSERT_HINT.search(body):
                    findings.append((path, line_no, "high", f"Test `{name}` has no assertion."))
                if re.search(r"^\s*(test|test\d+|check)$", name, re.I):
                    findings.append((path, line_no, "low", f"Test name `{name}` does not describe a behaviour."))

    print("### Test precheck (rule-based, advisory)")
    if not files:
        print("No Java test files changed.")
        return
    if not findings:
        print(f"Checked {len(files)} file(s). No mechanical issues found.")
        return
    order = {"high": 0, "medium": 1, "low": 2}
    for path, n, sev, msg in sorted(findings, key=lambda f: (order[f[2]], f[0], f[1])):
        print(f"- `{path}:{n}` [{sev}] {msg}")


if __name__ == "__main__":
    main()
