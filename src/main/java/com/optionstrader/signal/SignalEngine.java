package com.optionstrader.signal;

import org.ta4j.core.BarSeries;
import java.util.List;

/**
 * Interface for signal generation engines.
 */
public interface SignalEngine {
    List<Signal> generateSignals(BarSeries series);
}
