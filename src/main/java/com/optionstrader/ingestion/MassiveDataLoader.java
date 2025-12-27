package com.optionstrader.ingestion;

import org.ta4j.core.BarSeries;

/**
 * Interface for loading bar data from JSON into BarSeries.
 */
public interface MassiveDataLoader {
    BarSeries loadData(String json);
}
