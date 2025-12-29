package com.optionstrader.ingestion;

import org.ta4j.core.BarSeries;

import com.optionstrader.options.OptionChain;

/**
 * Interface for loading data from JSON.
 */
public interface MassiveDataLoader {
    BarSeries loadData(String json);
    OptionChain loadOptionChain(String json);
}
