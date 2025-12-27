package com.optionstrader.backtest;

import com.optionstrader.ingestion.MassiveDataLoader;
import com.optionstrader.ingestion.StandardMassiveDataLoader;
import com.optionstrader.options.OptionChain;
import com.optionstrader.options.OptionContract;
import com.optionstrader.signal.VolatilityOptimizedStrategy;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.BarSeries;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Referee Test for OptionBacktester Tri-Implementation parity.
 */
public class RefereeBacktestTest {

    private static final Logger logger = LoggerFactory.getLogger(RefereeBacktestTest.class);

    private BarSeries loadBarSeries() throws IOException {
        String json = Files.readString(Paths.get("src/test/resources/golden-tsla-intraday.json"));
        MassiveDataLoader loader = new StandardMassiveDataLoader();
        return loader.loadData(json);
    }

    private Map<LocalDate, OptionChain> loadOptionChains() {
        // Mock option chains for testing
        Map<LocalDate, OptionChain> chains = new HashMap<>();
        // For simplicity, create a chain for a few dates
        LocalDate date1 = LocalDate.of(2023, 1, 1);
        OptionContract contract1 = new OptionContract("TSLA", "CALL", 200.0, LocalDate.of(2023, 2, 1), 0.30, 31, 5.0, 0.01);
        OptionChain chain1 = new OptionChain("TSLA", List.of(contract1));
        chains.put(date1, chain1);
        // Add more as needed
        return chains;
    }

    @Test
    public void testBacktesterParity() throws IOException {
        logger.info("Starting Tri-Implementation Referee test for OptionBacktester");
        BarSeries series = loadBarSeries();
        logger.info("Loaded BarSeries with {} bars", series.getBarCount());

        Map<LocalDate, OptionChain> chains = loadOptionChains();
        org.ta4j.core.Strategy strategy = VolatilityOptimizedStrategy.build(series);
        logger.info("Built TA4J Strategy with unstable period {}", 200);

        Backtester sequential = new SequentialOptionBacktester();
        Backtester eventDriven = new EventDrivenOptionBacktester();
        Backtester streamBased = new StreamBasedOptionBacktester();

        logger.info("Running SequentialOptionBacktester");
        BacktestResult seqResult = sequential.runBacktest(series, strategy, chains);
        logger.info("Sequential result: {}", seqResult);

        logger.info("Running EventDrivenOptionBacktester");
        BacktestResult eventResult = eventDriven.runBacktest(series, strategy, chains);
        logger.info("Event-driven result: {}", eventResult);

        logger.info("Running StreamBasedOptionBacktester");
        BacktestResult streamResult = streamBased.runBacktest(series, strategy, chains);
        logger.info("Stream-based result: {}", streamResult);

        // Compare results
        assertResultsEqual(seqResult, eventResult, "Sequential vs Event-Driven");
        assertResultsEqual(eventResult, streamResult, "Event-Driven vs Stream-Based");
        assertResultsEqual(seqResult, streamResult, "Sequential vs Stream-Based");

        logger.info("Tri-Implementation Referee test passed: All backtesters produced identical results");
    }

    private void assertResultsEqual(BacktestResult r1, BacktestResult r2, String comparison) {
        logger.info("Comparing {}: {} vs {}", comparison, r1, r2);
        assertEquals(r1.totalNetProfit, r2.totalNetProfit, 0.0001, "Total Net Profit mismatch in " + comparison);
        assertEquals(r1.maxDrawdown, r2.maxDrawdown, 0.0001, "Max Drawdown mismatch in " + comparison);
        assertEquals(r1.tradeCount, r2.tradeCount, "Trade Count mismatch in " + comparison);
    }
}
