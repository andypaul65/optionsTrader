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
import java.util.*;

/**
 * Event-driven implementation of option backtester.
 */
public class EventDrivenOptionBacktester implements Backtester {

    private static final Logger logger = LoggerFactory.getLogger(EventDrivenOptionBacktester.class);

    @Override
    public BacktestResult runBacktest(BarSeries series, Strategy strategy, Map<LocalDate, OptionChain> chains) {
        BacktestResult result = new BacktestResult();
        OptionPosition openPosition = null;
        List<Event> events = new ArrayList<>();

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

            // Process events for this bar
            List<Event> currentEvents = new ArrayList<>();
            currentEvents.add(new BarEvent(i, date, chain));

            for (Event event : currentEvents) {
                if (event instanceof BarEvent) {
                    BarEvent barEvent = (BarEvent) event;
                    // Update position if open
                    if (openPosition != null) {
                        updatePositionPnL(openPosition, series, barEvent.index);
                        if (shouldRepair(openPosition, strategy, barEvent.index)) {
                            if (isStillBullish(barEvent.index, sma50, sma200)) {
                                events.add(new RollEvent(openPosition, series, barEvent.index, result));
                            } else {
                                events.add(new HardStopEvent(openPosition, series, barEvent.index, result));
                            }
                            openPosition = null;
                        } else if (strategy.shouldExit(barEvent.index)) {
                            events.add(new ExitEvent(openPosition, series, barEvent.index, result));
                            openPosition = null;
                        }
                    }

                    // Check entry
                    if (openPosition == null && strategy.shouldEnter(barEvent.index)) {
                        Optional<OptionContract> contract = selectContract(barEvent.chain, series.getBar(barEvent.index).getClosePrice().doubleValue());
                        if (contract.isPresent()) {
                            openPosition = openPosition(contract.get(), series, barEvent.index);
                            logger.info("Opened position: Date={}, Contract={}, Strike={}, EntryDelta={}",
                                        barEvent.date, contract.get().type(), contract.get().strike(), contract.get().delta());
                        }
                    }
                } else if (event instanceof ExitEvent) {
                    ExitEvent exit = (ExitEvent) event;
                    closePosition(exit.position, exit.series, exit.index, exit.result);
                } else if (event instanceof RollEvent) {
                    RollEvent roll = (RollEvent) event;
                    closePosition(roll.position, roll.series, roll.index, roll.result);
                } else if (event instanceof HardStopEvent) {
                    HardStopEvent stop = (HardStopEvent) event;
                    closePosition(stop.position, stop.series, stop.index, stop.result);
                }
            }
        }

        // Close remaining
        if (openPosition != null) {
            closePosition(openPosition, series, series.getBarCount() - 1, result);
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

    private void closePosition(OptionPosition position, BarSeries series, int i, BacktestResult result) {
        double finalPnL = position.pnl;
        result.totalNetProfit += finalPnL;
        result.tradeCount++;
        if (finalPnL < 0) result.maxDrawdown = Math.min(result.maxDrawdown, finalPnL);
        logger.info("Closed position: Date={}, Strike={}, ExitReason=Exit, FinalPnL={}",
                    series.getBar(i).getEndTime().toLocalDate(), position.contract.strike(), finalPnL);
    }

    private interface Event {}

    private static class BarEvent implements Event {
        int index;
        LocalDate date;
        OptionChain chain;

        BarEvent(int index, LocalDate date, OptionChain chain) {
            this.index = index;
            this.date = date;
            this.chain = chain;
        }
    }

    private static class ExitEvent implements Event {
        OptionPosition position;
        BarSeries series;
        int index;
        BacktestResult result;

        ExitEvent(OptionPosition position, BarSeries series, int index, BacktestResult result) {
            this.position = position;
            this.series = series;
            this.index = index;
            this.result = result;
        }
    }

    private static class RollEvent implements Event {
        OptionPosition position;
        BarSeries series;
        int index;
        BacktestResult result;

        RollEvent(OptionPosition position, BarSeries series, int index, BacktestResult result) {
            this.position = position;
            this.series = series;
            this.index = index;
            this.result = result;
        }
    }

    private static class HardStopEvent implements Event {
        OptionPosition position;
        BarSeries series;
        int index;
        BacktestResult result;

        HardStopEvent(OptionPosition position, BarSeries series, int index, BacktestResult result) {
            this.position = position;
            this.series = series;
            this.index = index;
            this.result = result;
        }
    }

    private static class OptionPosition {
        OptionContract contract;
        double pnl = 0;

        public OptionPosition(OptionContract contract, double entryPrice) {
            this.contract = contract;
        }
    }
}
