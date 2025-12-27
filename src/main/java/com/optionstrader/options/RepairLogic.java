package com.optionstrader.options;

/**
 * Handles repair logic for trades hitting loss thresholds.
 */
public class RepairLogic {

    public String suggestRepair(TradeDecision trade, double currentPrice) {
        double pnl = (currentPrice - trade.contract().price()) / trade.contract().price();
        if (pnl >= -0.20) {
            return "NONE";
        }
        if ("CALL".equals(trade.contract().type())) {
            return "ROLL"; // Extend DTE for bullish position
        } else {
            return "HARD_STOP"; // Exit bearish position
        }
    }
}
