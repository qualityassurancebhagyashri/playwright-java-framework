# Test Design Techniques Reference

## 1. Purpose

This reference defines test design techniques that the Jira-to-TestPlan Generator should apply when deriving test scenarios.

The agent should select appropriate techniques based on the requirement, application behavior, and business risk.

Do not blindly apply every technique to every requirement.

## 2. Equivalence Partitioning

Divide input data into groups that should produce similar system behavior.

Example: Payment amount validation.

Requirement:
Payment amount must be greater than zero.

| Partition | Input | Expected Result |
|---|---|---|
| Valid | 100 | Accepted |
| Invalid | 0 | Rejected |
| Invalid | -10 | Rejected |
| Invalid | Null | Rejected |
| Invalid | Text | Rejected |

Generate representative scenarios from each relevant partition.

## 3. Boundary Value Analysis

Test values at and around defined boundaries.

Example:

Requirement:
Coupon discount cannot exceed the order value.

Assume the order value is GBP 100.

| Test Value | Expected Result |
|---|---|
| GBP 0 | Apply according to defined minimum |
| GBP 1 | Valid if allowed |
| GBP 99 | Valid |
| GBP 100 | Maximum permitted |
| GBP 101 | Reject or cap according to confirmed business rules |

Important:
Do not invent boundary rules. If the expected behavior is unclear, raise an open question.

## 4. Decision Table Testing

Use decision tables when multiple conditions influence the outcome.

Example: Coupon application.

| Coupon Valid | Coupon Expired | Order Eligible | Expected Result |
|---|---|---|---|
| Yes | No | Yes | Apply coupon |
| No | No | Yes | Reject coupon |
| Yes | Yes | Yes | Reject coupon |
| Yes | No | No | Reject coupon |

Generate scenarios for meaningful combinations.

## 5. State Transition Testing

Use when the application has states and transitions.

Example: Payment lifecycle.

Possible states:
- Initiated
- Processing
- Successful
- Failed
- Timed Out

Example transitions:

| Current State | Event | Expected State |
|---|---|---|
| Initiated | Valid payment request | Processing |
| Processing | Gateway success | Successful |
| Processing | Gateway failure | Failed |
| Processing | Gateway timeout | Timed Out |

Validate:
- Valid transitions.
- Invalid transitions.
- Repeated transitions.
- Recovery behavior.
- State consistency.

Do not assume a state machine unless supported by requirements or explicitly labelled as a recommendation.

## 6. Error Guessing

Use domain knowledge and experience to identify likely failure conditions.

Examples:
- Multiple clicks on Pay.
- Expired authentication token.
- Payment gateway timeout.
- Repeated coupon application.
- Network interruption.
- Missing request headers.
- Unexpected API response.
- Concurrent updates to the same order.

These scenarios can be included as recommendations where their expected behavior is not explicitly defined.

## 7. Pairwise Testing

Use when multiple input parameters can create a large number of combinations.

Example: Checkout combinations.

Parameters:
- Payment method: Saved card, New card.
- Coupon: Applied, Not applied.
- User status: Authenticated, Unauthenticated.
- Device: Desktop, Mobile.

Instead of testing every possible combination, pairwise testing selects combinations that cover every pair of parameter values at least once.

Use an established combinatorial test generation method when actual pairwise coverage is required.

Do not claim complete pairwise coverage unless it has been verified.

## 8. Negative Testing

Validate how the system behaves with invalid or unsupported inputs.

Examples:
- Missing payment amount.
- Invalid card expiry.
- Incorrect coupon format.
- Expired coupon.
- Invalid authentication token.
- Unsupported HTTP method.
- Malformed JSON.

Expected results should include appropriate errors and no unintended state changes.

## 9. API Contract Testing

Validate:
- Request schema.
- Response schema.
- Mandatory fields.
- Optional fields.
- Data types.
- Headers.
- HTTP status codes.
- Error response structure.
- Backward compatibility.

Example:

API:
POST /api/v1/payments

Potential validation areas:
- Missing payment method.
- Invalid order identifier.
- Missing authorization token.
- Invalid request body.
- Gateway failure.
- Duplicate idempotency key.

Use actual API contracts where available.

## 10. Idempotency Testing

Use for operations that should not create duplicate side effects when requests are repeated.

Example:

Requirement:
Repeated payment requests with the same idempotency key should not create duplicate charges.

Scenarios:
1. Send a valid payment request.
2. Repeat the request using the same key.
3. Verify that the original transaction result is returned.
4. Verify that no second charge is created.
5. Submit a new request with a new key and verify behavior against the documented business rules.

Also consider:
- Concurrent requests.
- Retry after timeout.
- Reuse of keys with different request payloads.
- Key expiration and retention policies.

Raise clarification questions for undefined key behavior.

## 11. Concurrency Testing

Use when multiple requests or users can access the same resource simultaneously.

Examples:
- Multiple payment requests for one order.
- Two users updating the same record.
- Concurrent coupon redemption.
- Simultaneous inventory updates.

Validate:
- Data consistency.
- Duplicate processing.
- Race conditions.
- Transaction integrity.
- Expected conflict handling.

## 12. Exploratory Testing

Use exploratory testing to identify unexpected behaviors beyond documented scenarios.

Examples:
- Unusual user navigation.
- Rapid repeated actions.
- Unexpected interruption.
- Session expiration during checkout.
- Recovery after network failure.

Exploratory scenarios should be labelled as recommendations rather than confirmed acceptance criteria.

## 13. Security-Oriented Test Design

Where relevant, consider:
- Authentication.
- Authorization.
- Input validation.
- Sensitive data exposure.
- Session handling.
- Access control.
- Injection risks.
- Error message information disclosure.

Use approved security testing environments.

## 14. Performance Test Design

Where performance requirements exist, consider:
- Response time.
- Concurrent users.
- Throughput.
- Peak load.
- Stress conditions.
- Recovery after load.
- Resource utilization.

If performance thresholds are not documented, raise questions rather than inventing pass/fail criteria.

## 15. Technique Selection Guidelines

| Requirement Characteristic | Recommended Technique |
|---|---|
| Input validation | Equivalence Partitioning |
| Minimum/maximum limits | Boundary Value Analysis |
| Multiple business conditions | Decision Table |
| Workflow and status changes | State Transition Testing |
| Complex input combinations | Pairwise Testing |
| Repeated operations | Idempotency Testing |
| Concurrent operations | Concurrency Testing |
| Invalid inputs | Negative Testing |
| API-based functionality | API Contract Testing |
| Unexpected user behavior | Exploratory Testing |
| Security-sensitive functionality | Security-Oriented Testing |
| Performance requirements | Performance Test Design |

## 16. Scenario Generation Rules

For each requirement:

1. Identify the primary behavior.
2. Select relevant test design techniques.
3. Generate positive scenarios.
4. Generate negative scenarios.
5. Identify boundary conditions.
6. Identify integration and failure scenarios.
7. Evaluate regression impact.
8. Prioritize scenarios by risk.
9. Map scenarios to acceptance criteria.
10. Flag missing expected behavior.

The objective is meaningful and risk-based coverage, not the maximum possible number of scenarios.