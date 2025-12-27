package com.optionstrader.options;

import java.util.List;

/**
 * Represents an options chain for an underlying asset.
 */
public record OptionChain(
    String underlying,
    List<OptionContract> contracts
) {
    // Additional methods if needed
}
