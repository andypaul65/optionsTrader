package com.optionstrader.signal;

import com.optionstrader.ingestion.MassiveDataLoader;
import com.optionstrader.ingestion.StandardMassiveDataLoader;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Signal Integrity Test to ensure TA4J and Manual implementations produce identical signals.
 */
public class SignalIntegrityTest {

    private static final Logger logger = LoggerFactory.getLogger(SignalIntegrityTest.class);

    @Test
    public void testTslaSignals() throws IOException {
        logger.info("Starting Signal Integrity test for TSLA");
        String json = Files.readString(Paths.get("src/test/resources/golden-tsla-intraday.json"));
        logger.info("Loaded golden-tsla-intraday.json, length: {} characters", json.length());

        MassiveDataLoader loader = new StandardMassiveDataLoader();
        BarSeries series = loader.loadData(json);
        logger.info("Series loaded with {} bars", series.getBarCount());

        SignalEngine ta4jEngine = new Ta4jSignalEngine();
        SignalEngine manualEngine = new ManualSignalEngine();

        logger.info("Generating signals with Ta4jSignalEngine");
        List<Signal> ta4jSignals = ta4jEngine.generateSignals(series);
        logger.info("TA4J generated {} signals", ta4jSignals.size());

        logger.info("Generating signals with ManualSignalEngine");
        List<Signal> manualSignals = manualEngine.generateSignals(series);
        logger.info("Manual generated {} signals", manualSignals.size());

        logger.info("Comparing TA4J and Manual signals");
        for (int i = 0; i < Math.min(ta4jSignals.size(), manualSignals.size()); i++) {
            if (!ta4jSignals.get(i).equals(manualSignals.get(i))) {
                logger.error("First signal mismatch at index {}: TA4J={}, Manual={}", i, ta4jSignals.get(i), manualSignals.get(i));
                break;
            }
        }
        assertEquals(ta4jSignals, manualSignals);
        logger.info("Signal Integrity test for TSLA passed: Identical signals from both engines");
    }

    @Test
    public void testPltrSignals() throws IOException {
        logger.info("Starting Signal Integrity test for PLTR");
        String json = Files.readString(Paths.get("src/test/resources/golden-pltr-intraday.json"));
        logger.info("Loaded golden-pltr-intraday.json, length: {} characters", json.length());

        MassiveDataLoader loader = new StandardMassiveDataLoader();
        BarSeries series = loader.loadData(json);
        logger.info("Series loaded with {} bars", series.getBarCount());

        SignalEngine ta4jEngine = new Ta4jSignalEngine();
        SignalEngine manualEngine = new ManualSignalEngine();

        logger.info("Generating signals with Ta4jSignalEngine");
        List<Signal> ta4jSignals = ta4jEngine.generateSignals(series);
        logger.info("TA4J generated {} signals", ta4jSignals.size());

        logger.info("Generating signals with ManualSignalEngine");
        List<Signal> manualSignals = manualEngine.generateSignals(series);
        logger.info("Manual generated {} signals", manualSignals.size());

        logger.info("Comparing TA4J and Manual signals");
        for (int i = 0; i < Math.min(ta4jSignals.size(), manualSignals.size()); i++) {
            if (!ta4jSignals.get(i).equals(manualSignals.get(i))) {
                logger.error("First signal mismatch at index {}: TA4J={}, Manual={}", i, ta4jSignals.get(i), manualSignals.get(i));
                break;
            }
        }
        assertEquals(ta4jSignals, manualSignals);
        logger.info("Signal Integrity test for PLTR passed: Identical signals from both engines");
    }

    @Test
    public void testNvdaSignals() throws IOException {
        logger.info("Starting Signal Integrity test for NVDA");
        String json = Files.readString(Paths.get("src/test/resources/golden-nvda-intraday.json"));
        logger.info("Loaded golden-nvda-intraday.json, length: {} characters", json.length());

        MassiveDataLoader loader = new StandardMassiveDataLoader();
        BarSeries series = loader.loadData(json);
        logger.info("Series loaded with {} bars", series.getBarCount());

        SignalEngine ta4jEngine = new Ta4jSignalEngine();
        SignalEngine manualEngine = new ManualSignalEngine();

        logger.info("Generating signals with Ta4jSignalEngine");
        List<Signal> ta4jSignals = ta4jEngine.generateSignals(series);
        logger.info("TA4J generated {} signals", ta4jSignals.size());

        logger.info("Generating signals with ManualSignalEngine");
        List<Signal> manualSignals = manualEngine.generateSignals(series);
        logger.info("Manual generated {} signals", manualSignals.size());

        logger.info("Comparing TA4J and Manual signals");
        for (int i = 0; i < Math.min(ta4jSignals.size(), manualSignals.size()); i++) {
            if (!ta4jSignals.get(i).equals(manualSignals.get(i))) {
                logger.error("First signal mismatch at index {}: TA4J={}, Manual={}", i, ta4jSignals.get(i), manualSignals.get(i));
                break;
            }
        }
        assertEquals(ta4jSignals, manualSignals);
        logger.info("Signal Integrity test for NVDA passed: Identical signals from both engines");
    }
}
