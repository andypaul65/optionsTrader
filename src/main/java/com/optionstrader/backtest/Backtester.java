package com.optionstrader.backtest;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

import java.time.LocalDate;
import java.util.Map;

import com.optionstrader.options.OptionChain;

/**
 * Interface for option backtesters.
 */
public interface Backtester {
    BacktestResult runBacktest(BarSeries series, Strategy strategy, Map<LocalDate, OptionChain> chains);
}
