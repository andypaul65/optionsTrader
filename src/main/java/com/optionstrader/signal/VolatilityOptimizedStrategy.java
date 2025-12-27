package com.optionstrader.signal;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

/**
 * Builds TA4J Strategy for Volatility-Optimized trading.
 */
public class VolatilityOptimizedStrategy {

    public static Strategy build(BarSeries series) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator sma50 = new SMAIndicator(closePrice, 50);
        SMAIndicator sma200 = new SMAIndicator(closePrice, 200);
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);

        // Entry: sma50 > sma200 and rsi < 40
        Rule entryRule = new OverIndicatorRule(sma50, sma200).and(new UnderIndicatorRule(rsi, 40));

        // Exit: rsi > 70 or sma50 < sma200
        Rule exitRule = new UnderIndicatorRule(rsi, 70).negation().or(new OverIndicatorRule(sma50, sma200).negation());

        return new org.ta4j.core.BaseStrategy(entryRule, exitRule);
    }
}
