package com.optionstrader.options;

import java.time.LocalDate;

/**
 * Represents an options contract.
 */
public record OptionContract(
    String underlying,
    String type, // "CALL" or "PUT"
    double strike,
    LocalDate expiration,
    double delta,
    int dte,
    double price
) {
    // Additional methods if needed
}
