package com.optionstrader.signal;

import org.ta4j.core.BarSeries;

import java.util.ArrayList;
import java.util.List;

/**
 * TA4J implementation of SignalEngine using manual calculations with double.
 */
public class Ta4jSignalEngine implements SignalEngine {

    @Override
    public List<Signal> generateSignals(BarSeries series) {
        List<Double> closes = new ArrayList<>();
        for (int i = 0; i < series.getBarCount(); i++) {
            closes.add(series.getBar(i).getClosePrice().doubleValue());
        }

        List<Signal> signals = new ArrayList<>();
        for (int i = 0; i < series.getBarCount(); i++) {
            double sma50 = calculateSMA(closes, i, 50);
            double sma200 = calculateSMA(closes, i, 200);
            double rsi = calculateRSI(closes, i, 14);

            if (Double.isNaN(sma50) || Double.isNaN(sma200) || Double.isNaN(rsi)) {
                signals.add(Signal.HOLD);
            } else if (sma50 > sma200 && rsi < 40) {
                signals.add(Signal.BUY);
            } else if (rsi > 70) {
                signals.add(Signal.SELL);
            } else {
                signals.add(Signal.HOLD);
            }
        }
        return signals;
    }

    private double calculateSMA(List<Double> closes, int index, int period) {
        if (index < period - 1) {
            return Double.NaN;
        }
        double sum = 0;
        for (int i = index - period + 1; i <= index; i++) {
            sum += closes.get(i);
        }
        return sum / period;
    }

    private double calculateRSI(List<Double> closes, int index, int period) {
        if (index < period) {
            return Double.NaN;
        }
        List<Double> gains = new ArrayList<>();
        List<Double> losses = new ArrayList<>();
        for (int i = 1; i <= index; i++) {
            double diff = closes.get(i) - closes.get(i - 1);
            gains.add(Math.max(0, diff));
            losses.add(Math.max(0, -diff));
        }
        double avgGain = 0;
        double avgLoss = 0;
        for (int i = 0; i < period; i++) {
            avgGain += gains.get(i);
            avgLoss += losses.get(i);
        }
        avgGain /= period;
        avgLoss /= period;
        double multiplier = 2.0 / (period + 1);
        for (int i = period; i < gains.size(); i++) {
            avgGain = (gains.get(i) * multiplier) + (avgGain * (1 - multiplier));
            avgLoss = (losses.get(i) * multiplier) + (avgLoss * (1 - multiplier));
        }
        if (avgLoss == 0) {
            return 100;
        }
        double rs = avgGain / avgLoss;
        return 100 - 100 / (1 + rs);
    }
}
