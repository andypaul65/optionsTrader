package com.optionstrader.backtest;

/**
 * Result of a backtest run.
 */
public class BacktestResult {
    public double totalNetProfit = 0;
    public double maxDrawdown = 0;
    public int tradeCount = 0;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BacktestResult)) return false;
        BacktestResult other = (BacktestResult) obj;
        return Math.abs(totalNetProfit - other.totalNetProfit) < 0.0001 &&
               Math.abs(maxDrawdown - other.maxDrawdown) < 0.0001 &&
               tradeCount == other.tradeCount;
    }

    @Override
    public String toString() {
        return String.format("BacktestResult{profit=%.4f, drawdown=%.4f, trades=%d}", totalNetProfit, maxDrawdown, tradeCount);
    }
}
