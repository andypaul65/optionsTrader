package com.optionstrader.signal;

import org.ta4j.core.BarSeries;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure Java Signal Engine implementing Volatility-Optimized strategy.
 */
public class ManualSignalEngine implements SignalEngine {

    @Override
    public List<Signal> generateSignals(BarSeries series) {
        List<Signal> signals = new ArrayList<>();
        int count = series.getBarCount();
        if (count < 201) {
            return signals;
        }

        double[] closes = new double[count];
        for (int i = 0; i < count; i++) {
            closes[i] = series.getBar(i).getClosePrice().doubleValue();
        }

        double[] sma50 = new double[count];
        for (int i = 49; i < count; i++) {
            double sum = 0;
            for (int j = i - 49; j <= i; j++) {
                sum += closes[j];
            }
            sma50[i] = sum / 50;
        }

        double[] sma200 = new double[count];
        for (int i = 199; i < count; i++) {
            double sum = 0;
            for (int j = i - 199; j <= i; j++) {
                sum += closes[j];
            }
            sma200[i] = sum / 200;
        }

        double[] gains = new double[count - 1];
        double[] losses = new double[count - 1];
        for (int j = 1; j < count; j++) {
            double change = closes[j] - closes[j - 1];
            gains[j - 1] = change > 0 ? change : 0;
            losses[j - 1] = change < 0 ? -change : 0;
        }

        double[] rsi = new double[count];
        if (count >= 15) {
            double avgGain = 0;
            double avgLoss = 0;
            for (int j = 0; j < 14; j++) {
                avgGain += gains[j];
                avgLoss += losses[j];
            }
            avgGain /= 14;
            avgLoss /= 14;
            rsi[14] = avgLoss == 0 ? 100 : 100 - (100 / (1 + avgGain / avgLoss));
            for (int i = 15; i < count; i++) {
                avgGain = (avgGain * 13 + gains[i - 1]) / 14;
                avgLoss = (avgLoss * 13 + losses[i - 1]) / 14;
                rsi[i] = avgLoss == 0 ? 100 : 100 - (100 / (1 + avgGain / avgLoss));
            }
        }

        int warmUp = 0;
        boolean previousEntryCond = false;
        for (int i = 200; i < count; i++) {
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

            boolean entryCond = sma50[i] > sma200[i] && rsi[i] < 40;
            boolean exitCond = rsi[i] > 70 || sma50[i] < sma200[i];

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
