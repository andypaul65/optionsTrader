package com.optionstrader.backtest;

import com.optionstrader.options.OptionChain;
import com.optionstrader.options.OptionContract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Stream-based implementation of option backtester.
 */
public class StreamBasedOptionBacktester implements Backtester {

    private static final Logger logger = LoggerFactory.getLogger(StreamBasedOptionBacktester.class);

    @Override
    public BacktestResult runBacktest(BarSeries series, Strategy strategy, Map<LocalDate, OptionChain> chains) {
        BacktestResult result = new BacktestResult();
        AtomicReference<OptionPosition> openPosition = new AtomicReference<>();

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator sma50 = new SMAIndicator(closePrice, 50);
        SMAIndicator sma200 = new SMAIndicator(closePrice, 200);

        int unstable = 200;
        java.util.stream.IntStream.range(unstable, series.getBarCount()).forEach(i -> {
            LocalDate date = series.getBar(i).getEndTime().toLocalDate();
            OptionChain chain = chains.get(date);
            if (chain == null) {
                logger.warn("No option chain for date {}", date);
                return;
            }

            OptionPosition pos = openPosition.get();
            if (pos != null) {
                updatePositionPnL(pos, series, i);
                if (shouldRepair(pos, strategy, i)) {
                    if (isStillBullish(i, sma50, sma200)) {
                        executeRoll(pos, chain, series, i, result);
                    } else {
                        hardStop(pos, series, i, result);
                    }
                    openPosition.set(null);
                } else if (strategy.shouldExit(i)) {
                    closePosition(pos, series, i, result);
                    openPosition.set(null);
                }
            }

            if (openPosition.get() == null && strategy.shouldEnter(i)) {
                Optional<OptionContract> contract = selectContract(chain, series.getBar(i).getClosePrice().doubleValue());
                contract.ifPresent(c -> {
                    OptionPosition newPos = openPosition(c, series, i);
                    openPosition.set(newPos);
                    logger.info("Opened position: Date={}, Contract={}, Strike={}, EntryDelta={}",
                                date, c.type(), c.strike(), c.delta());
                });
            }
        });

        // Close remaining
        OptionPosition remaining = openPosition.get();
        if (remaining != null) {
            closePosition(remaining, series, series.getBarCount() - 1, result);
        }

        return result;
    }

    private Optional<OptionContract> selectContract(OptionChain chain, double underlyingPrice) {
        return chain.contracts().stream()
            .filter(c -> "CALL".equals(c.type()) && c.dte() >= 30 && c.dte() <= 45)
            .min(Comparator.comparingDouble(c -> Math.abs(c.delta() - 0.30)));
    }

    private OptionPosition openPosition(OptionContract contract, BarSeries series, int i) {
        return new OptionPosition(contract, series.getBar(i).getClosePrice().doubleValue());
    }

    private void updatePositionPnL(OptionPosition position, BarSeries series, int i) {
        double underlyingChange = series.getBar(i).getClosePrice().doubleValue() - series.getBar(i - 1).getClosePrice().doubleValue();
        double optionChange = position.contract.delta() * underlyingChange;
        double thetaDecay = position.contract.theta();
        position.pnl += optionChange - thetaDecay;
    }

    private boolean shouldRepair(OptionPosition position, Strategy strategy, int i) {
        return position.pnl < -0.20;
    }

    private boolean isStillBullish(int i, SMAIndicator sma50, SMAIndicator sma200) {
        return sma50.getValue(i).doubleValue() > sma200.getValue(i).doubleValue();
    }

    private void executeRoll(OptionPosition position, OptionChain chain, BarSeries series, int i, BacktestResult result) {
        closePosition(position, series, i, result);
    }

    private void hardStop(OptionPosition position, BarSeries series, int i, BacktestResult result) {
        closePosition(position, series, i, result);
    }

    private void closePosition(OptionPosition position, BarSeries series, int i, BacktestResult result) {
        double finalPnL = position.pnl;
        result.totalNetProfit += finalPnL;
        result.tradeCount++;
        if (finalPnL < 0) result.maxDrawdown = Math.min(result.maxDrawdown, finalPnL);
        logger.info("Closed position: Date={}, Strike={}, ExitReason=Exit, FinalPnL={}",
                    series.getBar(i).getEndTime().toLocalDate(), position.contract.strike(), finalPnL);
    }

    private static class OptionPosition {
        OptionContract contract;
        double pnl = 0;

        public OptionPosition(OptionContract contract, double entryPrice) {
            this.contract = contract;
        }
    }
}
