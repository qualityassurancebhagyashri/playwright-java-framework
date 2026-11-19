# QA Testing Guidelines

## 1. Purpose

This document defines the QA reasoning standards used by the Jira-to-TestPlan Generator.

The agent must approach requirements from the perspective of a Senior QA Engineer rather than simply converting acceptance criteria into test scenarios.

The goal is to identify meaningful coverage, business risks, technical risks, and requirement gaps.

## 2. Requirement Understanding

Before generating test scenarios, understand:

- What business problem is being solved?
- Who is the intended user?
- What functionality is changing?
- What are the expected outcomes?
- What existing functionality could be affected?
- Which systems and services are involved?
- What are the dependencies?
- What happens when the expected flow fails?

Break complex requirements into smaller testable behaviors.

## 3. Requirement Classification

Classify requirements into the following categories.

### Functional Requirements

Describe what the system should do.

Examples:
- User can add a payment method.
- User can apply a coupon.
- System generates an order confirmation.

### Non-Functional Requirements

Describe how the system should perform.

Examples:
- Payment processing should meet agreed response-time targets.
- System should support concurrent requests.
- Sensitive data should be protected.

### Business Rules

Define domain-specific constraints.

Examples:
- Only one coupon can be applied per order.
- Discounts cannot exceed the order value.
- Duplicate payment requests must not result in duplicate charges.

### Integration Requirements

Describe communication with external or internal systems.

Examples:
- Payment gateway integration.
- Order confirmation service.
- Authentication service.

## 4. QA Reasoning Approach

For every requirement, evaluate the following.

### A. Happy Path

What is the expected successful user journey?

### B. Negative Path

What happens when invalid input or an invalid action is provided?

### C. Boundary Conditions

What happens at minimum, maximum, and threshold values?

### D. Data Integrity

Can the operation create duplicate, incomplete, or inconsistent data?

### E. Integration Failure

What happens when an external dependency is unavailable or returns an error?

### F. Security

Can unauthorized users access functionality or sensitive data?

### G. Performance

Does the functionality remain reliable under expected load?

### H. Regression Impact

What existing functionality could be affected by this change?

Only include dimensions relevant to the requirement.

## 5. Testing Layers

### Unit Testing

Validates isolated business logic and individual components.

Typically owned by developers, but QA should identify important unit-level risks.

### API Testing

Validate:
- Request parameters.
- Required and optional fields.
- Authentication and authorization.
- Response schemas.
- HTTP status codes.
- Error messages.
- Data validation.
- Idempotency.
- Retry behavior.

### Integration Testing

Validate interactions between dependent systems.

Examples:
- Payment service and gateway.
- Coupon service and checkout.
- Checkout and order management.

### End-to-End Testing

Validate complete customer journeys.

Examples:
- Add items to cart.
- Apply coupon.
- Complete payment.
- Receive order confirmation.

### Regression Testing

Identify existing features that could be impacted by the changes.

### Non-Functional Testing

Consider:
- Performance.
- Security.
- Reliability.
- Availability.
- Scalability.

## 6. Risk-Based Testing

Prioritize testing based on:
- Business impact.
- Financial impact.
- Customer experience.
- Data integrity.
- Security.
- Complexity.
- Integration dependencies.
- Likelihood of failure.

High-risk functionality should receive deeper testing coverage.

Do not assign priorities based only on Jira story priority.

## 7. Requirement Ambiguity Detection

Identify unclear or missing details.

Example requirement:

"Payment should be processed successfully."

Questions to raise:
- What is the payment timeout?
- What happens if the gateway is unavailable?
- How many retries are permitted?
- What happens after a gateway timeout?
- How is transaction status reconciled?
- Can a user retry using the same idempotency key?

Record these as open questions rather than inventing answers.

## 8. Scenario Quality Standards

Every generated scenario must:

- Have a clear objective.
- Be independently understandable.
- Validate a specific behavior.
- Have a measurable expected result.
- Reference the source requirement where applicable.
- Include appropriate priority.
- Avoid unnecessary duplication.

Avoid vague scenarios such as:
- Verify payment functionality.
- Verify coupon functionality.
- Verify application behavior.

Prefer:
- Verify that a valid payment request creates exactly one successful transaction.
- Verify that an expired coupon is rejected without changing the order total.
- Verify that repeated requests with the same idempotency key do not create duplicate charges.

## 9. Negative Testing Guidelines

Consider:
- Missing required fields.
- Null values.
- Empty strings.
- Invalid formats.
- Unauthorized requests.
- Expired sessions.
- Invalid state transitions.
- Unsupported values.
- Service failures.
- Duplicate requests.
- Timeout and retry conditions.

## 10. API Testing Guidelines

For API-based requirements, consider:

| Area | Validation |
|---|---|
| HTTP Method | Correct method |
| Authentication | Valid and invalid tokens |
| Authorization | Access permissions |
| Headers | Required headers |
| Request Body | Required and optional fields |
| Data Types | Correct data types |
| Response | Status code and schema |
| Error Handling | Meaningful error responses |
| Idempotency | Duplicate request protection |
| Performance | Response time and load |
| Security | Sensitive information exposure |

Do not assume API behavior that is not documented.

## 11. Security Testing Guidelines

Where applicable, consider:
- Authentication bypass.
- Authorization failures.
- Sensitive data exposure.
- Input manipulation.
- Improper error disclosure.
- Insecure data storage.
- Logging of sensitive information.

For payment features, follow approved payment security and sandbox testing practices.

## 12. Regression Impact Analysis

For every change, ask:

- Which existing modules are impacted?
- Which APIs are shared?
- Which business rules are reused?
- Are database changes involved?
- Could the change affect other customer journeys?
- Are there backward compatibility concerns?

Include relevant regression recommendations in the test plan.

## 13. Assumption Management

Classify information as:

CONFIRMED:
Explicitly supported by Jira requirements or approved documentation.

ASSUMPTION:
Reasonable interpretation requiring confirmation.

OPEN QUESTION:
Information required to establish expected behavior.

RECOMMENDATION:
Additional QA coverage suggested based on risk.

Never represent assumptions as approved requirements.

## 14. Final QA Review Checklist

Before completing the test plan, verify:

- Business requirements are understood.
- All acceptance criteria have been evaluated.
- Positive scenarios are covered.
- Negative scenarios are considered.
- Boundary scenarios are considered.
- Integration risks are identified.
- Regression risks are evaluated.
- High-risk requirements receive appropriate coverage.
- Expected results are measurable.
- Requirement traceability is accurate.
- Ambiguities are documented.
- No unsupported requirements have been introduced.

The final output must be actionable for QA, Product, and Engineering teams.