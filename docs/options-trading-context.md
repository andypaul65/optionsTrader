# Domain Context: TA4J-to-Options Bridge

## 1. The Stack
- **Trend Engine**: TA4J (using `BarSeries` for underlying stock signals).
- **Execution Engine**: Custom Java 21 logic using Virtual Threads for high-throughput (10k events/sec).
- **Data Source**: Massive.com (Polygon.io backend) for historical Option Chains and Greeks.

## 2. Business Logic Guardrails
- **Signal Translation**: Convert `ta4j.Strategy.shouldEnter()` into a `Call` or `Put` contract selection based on Delta (~0.30) and DTE (30-45 days).
- **Repair Logic**: If a trade hits -20% PnL, the system must choose between `executeRoll()` (if still bullish) or `hardStop()` (if trend reversed).

## 3. Verification Goal
I do not need to review your `MassiveDataLoader` code. I need to see a **Comparison Report** showing that the `BarSeries` generated matches a reference CSV for the same ticker/period.