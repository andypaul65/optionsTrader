package com.optionstrader.ingestion;

import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Referee Test to ensure 1:1 data parity across the three MassiveDataLoader implementations.
 */
public class RefereeTest {

    private static final Logger logger = LoggerFactory.getLogger(RefereeTest.class);

    private String loadSampleJson() throws IOException {
        return Files.readString(Paths.get("src/test/resources/golden-tsla-intraday.json"));
    }

    @Test
    public void testDataParity() throws IOException {
        logger.info("Starting Tri-Implementation Referee test with golden-tsla-intraday.json");
        String json = loadSampleJson();
        logger.info("Loaded JSON data, length: {} characters", json.length());

        MassiveDataLoader standardLoader = new StandardMassiveDataLoader();
        MassiveDataLoader streamingLoader = new StreamingMassiveDataLoader();
        MassiveDataLoader recordLoader = new RecordBasedMassiveDataLoader();

        logger.info("Parsing with StandardMassiveDataLoader");
        BarSeries standardSeries = standardLoader.loadData(json);
        logger.info("Standard series created with {} bars", standardSeries.getBarCount());

        logger.info("Parsing with StreamingMassiveDataLoader");
        BarSeries streamingSeries = streamingLoader.loadData(json);
        logger.info("Streaming series created with {} bars", streamingSeries.getBarCount());

        logger.info("Parsing with RecordBasedMassiveDataLoader");
        BarSeries recordSeries = recordLoader.loadData(json);
        logger.info("Record-based series created with {} bars", recordSeries.getBarCount());

        // Compare Standard and Streaming
        logger.info("Comparing Standard and Streaming series");
        logger.info("Standard bar count: {}, Streaming bar count: {}", standardSeries.getBarCount(), streamingSeries.getBarCount());
        for (int i = 0; i < Math.min(standardSeries.getBarCount(), streamingSeries.getBarCount()); i++) {
            if (!standardSeries.getBar(i).equals(streamingSeries.getBar(i))) {
                logger.error("First bar mismatch at index {}: Standard={}, Streaming={}", i, standardSeries.getBar(i), streamingSeries.getBar(i));
                break;
            }
        }
        assertEquals(standardSeries.getBarCount(), streamingSeries.getBarCount(), "Bar count mismatch between Standard and Streaming");
        for (int i = 0; i < standardSeries.getBarCount(); i++) {
            assertEquals(standardSeries.getBar(i), streamingSeries.getBar(i), "Bar " + i + " mismatch between Standard and Streaming");
        }

        // Compare Streaming and Record-based
        logger.info("Comparing Streaming and Record-based series");
        logger.info("Streaming bar count: {}, Record-based bar count: {}", streamingSeries.getBarCount(), recordSeries.getBarCount());
        for (int i = 0; i < Math.min(streamingSeries.getBarCount(), recordSeries.getBarCount()); i++) {
            if (!streamingSeries.getBar(i).equals(recordSeries.getBar(i))) {
                logger.error("First bar mismatch at index {}: Streaming={}, Record-based={}", i, streamingSeries.getBar(i), recordSeries.getBar(i));
                break;
            }
        }
        assertEquals(streamingSeries.getBarCount(), recordSeries.getBarCount(), "Bar count mismatch between Streaming and Record-based");
        for (int i = 0; i < streamingSeries.getBarCount(); i++) {
            assertEquals(streamingSeries.getBar(i), recordSeries.getBar(i), "Bar " + i + " mismatch between Streaming and Record-based");
        }

        // Compare Standard and Record-based (transitive)
        logger.info("Comparing Standard and Record-based series");
        logger.info("Standard bar count: {}, Record-based bar count: {}", standardSeries.getBarCount(), recordSeries.getBarCount());
        for (int i = 0; i < Math.min(standardSeries.getBarCount(), recordSeries.getBarCount()); i++) {
            if (!standardSeries.getBar(i).equals(recordSeries.getBar(i))) {
                logger.error("First bar mismatch at index {}: Standard={}, Record-based={}", i, standardSeries.getBar(i), recordSeries.getBar(i));
                break;
            }
        }
        assertEquals(standardSeries.getBarCount(), recordSeries.getBarCount(), "Bar count mismatch between Standard and Record-based");
        for (int i = 0; i < standardSeries.getBarCount(); i++) {
            assertEquals(standardSeries.getBar(i), recordSeries.getBar(i), "Bar " + i + " mismatch between Standard and Record-based");
        }

        logger.info("Tri-Implementation Referee test passed: All loaders produced identical BarSeries");
    }
}
