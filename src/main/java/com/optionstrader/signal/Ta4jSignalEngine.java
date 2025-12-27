package com.optionstrader.signal;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;

import java.util.ArrayList;
import java.util.List;

/**
 * TA4J-based Signal Engine implementing Volatility-Optimized strategy.
 */
public class Ta4jSignalEngine implements SignalEngine {

    @Override
    public List<Signal> generateSignals(BarSeries series) {
        List<Signal> signals = new ArrayList<>();
        if (series.getBarCount() < 201) {
            return signals;
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator sma50 = new SMAIndicator(closePrice, 50);
        SMAIndicator sma200 = new SMAIndicator(closePrice, 200);
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);

        int warmUp = 0;
        boolean previousEntryCond = false;

        for (int i = 200; i < series.getBarCount(); i++) {
            // Check for gap
            if (i > 0) {
                double open = series.getBar(i).getOpenPrice().doubleValue();
                double prevClose = series.getBar(i - 1).getClosePrice().doubleValue();
                if (Math.abs(open - prevClose) / prevClose > 0.02) {
                    warmUp = 5;
                }
            }

            if (warmUp > 0) {
                signals.add(Signal.HOLD);
                warmUp--;
                previousEntryCond = false;
                continue;
            }

            boolean entryCond = sma50.getValue(i).doubleValue() > sma200.getValue(i).doubleValue() &&
                                rsi.getValue(i).doubleValue() < 40;
            boolean exitCond = rsi.getValue(i).doubleValue() > 70 ||
                               sma50.getValue(i).doubleValue() < sma200.getValue(i).doubleValue();

            if (exitCond) {
                signals.add(Signal.SELL);
            } else if (i > 200 && entryCond && previousEntryCond) {
                signals.add(Signal.BUY);
            } else {
                signals.add(Signal.HOLD);
            }

            previousEntryCond = entryCond;
        }

        return signals;
    }
}
