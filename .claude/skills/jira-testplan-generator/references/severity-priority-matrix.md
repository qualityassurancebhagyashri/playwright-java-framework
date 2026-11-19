# Severity and Priority Matrix

## 1. Purpose

This document defines the standards for classifying defect severity and test execution priority.

The Jira-to-TestPlan Generator must distinguish between:

- Defect Severity: How seriously a defect impacts the system or business.
- Test Priority: How urgently a test scenario should be executed.

These are related but different concepts.

## 2. Defect Severity

### Critical (S1)

Definition:
A defect that causes a major business failure, significant security exposure, financial loss, or complete failure of critical functionality.

Examples:
- Duplicate payment charges.
- Unauthorized access to sensitive customer data.
- Complete checkout failure.
- Critical data corruption.

Expected action:
Immediate investigation and resolution.

### High (S2)

Definition:
A defect that significantly impacts important functionality but does not completely prevent the entire application from operating.

Examples:
- Payment failure for a supported payment method.
- Incorrect coupon discount calculation.
- Order confirmation not generated after successful payment.
- Important API integration failure.

Expected action:
Prioritize for the current release based on business impact.

### Medium (S3)

Definition:
A defect that impacts functionality but has a reasonable workaround.

Examples:
- Incorrect non-critical validation message.
- Certain coupon types not displaying correctly.
- Minor data presentation inconsistency.

Expected action:
Resolve according to sprint and release priorities.

### Low (S4)

Definition:
A defect with minimal functional or business impact.

Examples:
- Minor UI alignment issue.
- Cosmetic inconsistency.
- Non-critical wording issue.

Expected action:
Address according to backlog priority.

## 3. Test Execution Priority

### P0 – Critical

Must be tested with the highest priority.

Examples:
- Payment processing.
- Duplicate payment prevention.
- Authentication and authorization.
- Critical financial calculations.

### P1 – High

Important functionality with significant customer or business impact.

Examples:
- Applying valid coupons.
- Payment validation.
- Order confirmation.
- Error handling for payment failures.

### P2 – Medium

Functionality with moderate business impact.

Examples:
- Non-critical checkout validations.
- Optional user preferences.
- Secondary workflows.

### P3 – Low

Lower-risk functionality.

Examples:
- Cosmetic behavior.
- Minor UI presentation.
- Non-critical informational messages.

## 4. Risk Assessment

Evaluate risks using two dimensions:

Impact:
- High
- Medium
- Low

Likelihood:
- High
- Medium
- Low

### Risk Matrix

| Impact / Likelihood | High Likelihood | Medium Likelihood | Low Likelihood |
|---|---|---|---|
| High Impact | Critical | High | Medium |
| Medium Impact | High | Medium | Low |
| Low Impact | Medium | Low | Low |

### Risk Classification

Critical:
Potential for severe business, financial, security, or data integrity consequences.

High:
Significant functionality or customer experience impact.

Medium:
Moderate operational or functional impact.

Low:
Limited impact with a straightforward workaround.

## 5. Risk-Based Test Prioritization

The agent should consider:

1. Business criticality.
2. Customer impact.
3. Financial impact.
4. Data integrity.
5. Security implications.
6. Integration complexity.
7. Historical defect patterns, if available.
8. Likelihood of failure.
9. Change complexity.
10. Production exposure.

Do not automatically assign P0 to every scenario belonging to a critical Jira story.

Each scenario should receive an individual priority based on its specific risk.

## 6. Example: E-Commerce Checkout

| Scenario | Risk | Test Priority | Reason |
|---|---|---|---|
| Successful payment | Critical | P0 | Core revenue functionality |
| Duplicate payment prevention | Critical | P0 | Prevents financial loss |
| Invalid card validation | High | P1 | Prevents invalid transactions |
| Expired coupon rejection | Medium | P2 | Business rule enforcement |
| Minor coupon error message formatting | Low | P3 | Limited customer impact |

These classifications are illustrative and should be reviewed against the actual business context.

## 7. Defect Severity vs Test Priority

| Scenario / Defect | Severity | Test Priority |
|---|---|---|
| Duplicate payment charge | S1 | P0 |
| Payment failure for supported cards | S2 | P1 |
| Incorrect coupon discount | S2 | P1 |
| Minor validation message issue | S4 | P3 |

A test scenario does not have defect severity. Severity is assigned when an actual defect is identified.

## 8. Agent Decision Rules

When generating test plans:

1. Use test execution priority for scenarios.
2. Do not confuse Jira issue priority with scenario priority.
3. Use defect severity only when classifying identified defects.
4. Explain the reason for every P0 or P1 classification.
5. Consider business and technical risk.
6. Do not assume severity from a ticket title alone.
7. Flag insufficient information for human review.

## 9. Output Requirements

The generated test plan should include:

| Scenario ID | Scenario | Risk Level | Test Priority | Justification |
|---|---|---|---|---|

The agent should provide a brief justification for high-priority scenarios.

Final prioritization remains subject to QA Lead and Product Engineering review.