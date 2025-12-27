package com.optionstrader.signal;

import com.optionstrader.ingestion.StandardMassiveDataLoader;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * SignalValidator test for integrity: Ensures TA4J and Manual implementations produce identical signals.
 */
public class SignalValidatorTest {

    private BarSeries loadSampleBarSeries() throws IOException {
        String json = Files.readString(Paths.get("src/test/resources/sample-bars.json"));
        StandardMassiveDataLoader loader = new StandardMassiveDataLoader();
        return loader.loadData(json);
    }

    @Test
    public void testSignalIntegrity() throws IOException {
        BarSeries series = loadSampleBarSeries();

        SignalEngine ta4jEngine = new Ta4jSignalEngine();
        SignalEngine manualEngine = new ManualSignalEngine();

        List<Signal> ta4jSignals = ta4jEngine.generateSignals(series);
        List<Signal> manualSignals = manualEngine.generateSignals(series);

        assertEquals(ta4jSignals, manualSignals, "TA4J and Manual signal engines must produce identical signals");
    }
}
