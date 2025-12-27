# Integrity Report

## Referee Test Results

- **Date**: 2025-12-27
- **Accuracy**: All three implementations (Standard, Streaming, Record-based) produce identical BarSeries, confirming 1:1 data parity.
- **Performance Metrics**: To be determined via local benchmarks. The most performant version will be selected for production to meet the 10k/sec throughput goal.

## OptionBacktester Referee Test Results

- **Date**: 2025-12-27
- **Accuracy**: All three implementations (Sequential, Event-Driven, Stream-based) produce identical BacktestResult (Total Net Profit, Max Drawdown, Trade Count) with strict equality to 4 decimal places, confirming 1:1 logic parity.
- **Performance Metrics**: SequentialOptionBacktester: Baseline throughput; EventDrivenOptionBacktester: Event-based processing; StreamBasedOptionBacktester: Parallel stream processing. Full benchmarks pending for 10k/sec goal using Java 21 Virtual Threads.
