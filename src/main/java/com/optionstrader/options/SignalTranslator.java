package com.optionstrader.options;

import com.optionstrader.signal.Signal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Translates signals into options trade decisions.
 */
public class SignalTranslator {

    public List<TradeDecision> translateSignals(List<Signal> signals, OptionChain chain) {
        List<TradeDecision> decisions = new ArrayList<>();
        for (Signal signal : signals) {
            if (signal == Signal.BUY) {
                findBestCall(chain).ifPresent(c -> decisions.add(new TradeDecision(c, "BUY")));
            } else if (signal == Signal.SELL) {
                findBestPut(chain).ifPresent(c -> decisions.add(new TradeDecision(c, "BUY")));
            }
            // HOLD does nothing
        }
        return decisions;
    }

    private Optional<OptionContract> findBestCall(OptionChain chain) {
        return chain.contracts().stream()
            .filter(c -> "CALL".equals(c.type()) && c.dte() >= 30 && c.dte() <= 45)
            .min(Comparator.comparingDouble(c -> Math.abs(c.delta() - 0.30)));
    }

    private Optional<OptionContract> findBestPut(OptionChain chain) {
        return chain.contracts().stream()
            .filter(c -> "PUT".equals(c.type()) && c.dte() >= 30 && c.dte() <= 45)
            .min(Comparator.comparingDouble(c -> Math.abs(c.delta() + 0.30)));
    }
}
