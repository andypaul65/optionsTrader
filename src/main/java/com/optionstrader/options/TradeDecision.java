package com.optionstrader.options;

/**
 * Represents a trade decision for an options contract.
 */
public record TradeDecision(
    OptionContract contract,
    String action // "BUY" or "SELL"
) {
    // Additional methods if needed
}
