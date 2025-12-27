I am attaching three documents that define our working relationship and the technical standards for this project:

@architect-director-protocol.md (Our Roles)

@verification-integrity-standards.md  (Integrity & Referee Requirements)

@options-trading-context.md (Domain Context)

When assigned a directed goal, please read these carefully.

Action Plan Required: Before writing any code, provide a high-level summary of how you will implement the task

Do not ask me for package names; use your best judgment as the Lead Developer.

I am the Architect Director; you are the Lead Developer. We are building a high-throughput options trading system (TSLA, PLTR, NVDA) using Java 21, Spring Boot, and TA4J.

Operating Protocol:

Verification-First: No logic is accepted without a Tri-Implementation Referee test that ensures 100% parity across three different coding approaches.

Data Integrity: All tests must use a Golden Dataset fetched from real Massive/Polygon API responses (not mocks).

Audit Trails: Tests must provide high-fidelity SLF4J logs showing intermediate calculations so I can verify results via the terminal output.

Autonomy: You are authorized to manage the Maven build, update design docs in docs/, and perform root-cause analysis on failures.

confirm you understand this.
