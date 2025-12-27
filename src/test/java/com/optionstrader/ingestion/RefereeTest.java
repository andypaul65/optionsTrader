package com.optionstrader.ingestion;

import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Referee Test to ensure 1:1 data parity across the three MassiveDataLoader implementations.
 */
public class RefereeTest {

    private String loadSampleJson() throws IOException {
        return Files.readString(Paths.get("src/test/resources/sample-bars.json"));
    }

    @Test
    public void testDataParity() throws IOException {
        String json = loadSampleJson();

        MassiveDataLoader standardLoader = new StandardMassiveDataLoader();
        MassiveDataLoader streamingLoader = new StreamingMassiveDataLoader();
        MassiveDataLoader recordLoader = new RecordBasedMassiveDataLoader();

        BarSeries standardSeries = standardLoader.loadData(json);
        BarSeries streamingSeries = streamingLoader.loadData(json);
        BarSeries recordSeries = recordLoader.loadData(json);

        // Compare Standard and Streaming
        assertEquals(standardSeries.getBarCount(), streamingSeries.getBarCount(), "Bar count mismatch between Standard and Streaming");
        for (int i = 0; i < standardSeries.getBarCount(); i++) {
            assertEquals(standardSeries.getBar(i), streamingSeries.getBar(i), "Bar " + i + " mismatch between Standard and Streaming");
        }

        // Compare Streaming and Record-based
        assertEquals(streamingSeries.getBarCount(), recordSeries.getBarCount(), "Bar count mismatch between Streaming and Record-based");
        for (int i = 0; i < streamingSeries.getBarCount(); i++) {
            assertEquals(streamingSeries.getBar(i), recordSeries.getBar(i), "Bar " + i + " mismatch between Streaming and Record-based");
        }

        // Compare Standard and Record-based (transitive)
        assertEquals(standardSeries.getBarCount(), recordSeries.getBarCount(), "Bar count mismatch between Standard and Record-based");
        for (int i = 0; i < standardSeries.getBarCount(); i++) {
            assertEquals(standardSeries.getBar(i), recordSeries.getBar(i), "Bar " + i + " mismatch between Standard and Record-based");
        }
    }
}
