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

/**
 * Sequential implementation of option backtester.
 */
public class SequentialOptionBacktester implements Backtester {

    private static final Logger logger = LoggerFactory.getLogger(SequentialOptionBacktester.class);

    // Assuming OptionChains are provided per date
    @Override
    public BacktestResult runBacktest(BarSeries series, Strategy strategy, Map<LocalDate, OptionChain> chains) {
        BacktestResult result = new BacktestResult();
        OptionPosition openPosition = null;

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator sma50 = new SMAIndicator(closePrice, 50);
        SMAIndicator sma200 = new SMAIndicator(closePrice, 200);

        for (int i = 200; i < series.getBarCount(); i++) {
            LocalDate date = series.getBar(i).getEndTime().toLocalDate();
            OptionChain chain = chains.get(date);
            if (chain == null) {
                logger.warn("No option chain for date {}", date);
                continue;
            }

            // Check for exit or repair if position open
            if (openPosition != null) {
                updatePositionPnL(openPosition, series, i);
                if (shouldRepair(openPosition, strategy, i)) {
                    if (isStillBullish(i, sma50, sma200)) {
                        executeRoll(openPosition, chain, series, i, result);
                    } else {
                        hardStop(openPosition, series, i, result);
                    }
                    openPosition = null;
                } else if (strategy.shouldExit(i)) {
                    closePosition(openPosition, series, i, result);
                    openPosition = null;
                }
            }

            // Check for entry
            if (openPosition == null && strategy.shouldEnter(i)) {
                Optional<OptionContract> contract = selectContract(chain, series.getBar(i).getClosePrice().doubleValue());
                if (contract.isPresent()) {
                    openPosition = openPosition(contract.get(), series, i);
                    logger.info("Opened position: Date={}, Contract={}, Strike={}, EntryDelta={}",
                                date, contract.get().type(), contract.get().strike(), contract.get().delta());
                }
            }
        }

        // Close any remaining position at end
        if (openPosition != null) {
            closePosition(openPosition, series, series.getBarCount() - 1, result);
        }

        return result;
    }

    private Optional<OptionContract> selectContract(OptionChain chain, double underlyingPrice) {
        // Select Call if bullish, assume Call
        return chain.contracts().stream()
            .filter(c -> "CALL".equals(c.type()) && c.dte() >= 30 && c.dte() <= 45)
            .min(Comparator.comparingDouble(c -> Math.abs(c.delta() - 0.30)));
    }

    private OptionPosition openPosition(OptionContract contract, BarSeries series, int i) {
        return new OptionPosition(contract, series.getBar(i).getClosePrice().doubleValue());
    }

    private void updatePositionPnL(OptionPosition position, BarSeries series, int i) {
        // Simplified: delta-based change + theta decay
        double underlyingChange = series.getBar(i).getClosePrice().doubleValue() - series.getBar(i - 1).getClosePrice().doubleValue();
        double optionChange = position.contract.delta() * underlyingChange;
        double thetaDecay = position.contract.theta(); // assume per bar
        position.pnl += optionChange - thetaDecay;
    }

    private boolean shouldRepair(OptionPosition position, Strategy strategy, int i) {
        return position.pnl < -0.20; // -20%
    }

    private boolean isStillBullish(int i, SMAIndicator sma50, SMAIndicator sma200) {
        return sma50.getValue(i).doubleValue() > sma200.getValue(i).doubleValue();
    }

    private void executeRoll(OptionPosition position, OptionChain chain, BarSeries series, int i, BacktestResult result) {
        closePosition(position, series, i, result);
        Optional<OptionContract> newContract = selectContract(chain, series.getBar(i).getClosePrice().doubleValue());
        if (newContract.isPresent()) {
            // Note: in sequential, we can't open new here as loop continues; simplified, just close
        }
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
