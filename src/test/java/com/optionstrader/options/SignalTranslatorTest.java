package com.optionstrader.options;

import com.optionstrader.signal.Signal;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for SignalTranslator and RepairLogic.
 */
public class SignalTranslatorTest {

    @Test
    public void testTranslateSignals() {
        // Create mock option chain
        List<OptionContract> contracts = List.of(
            new OptionContract("TSLA", "CALL", 100.0, LocalDate.now().plusDays(35), 0.32, 35, 5.0),
            new OptionContract("TSLA", "PUT", 100.0, LocalDate.now().plusDays(40), -0.28, 40, 4.0)
        );
        OptionChain chain = new OptionChain("TSLA", contracts);

        SignalTranslator translator = new SignalTranslator();
        List<Signal> signals = List.of(Signal.BUY, Signal.SELL, Signal.HOLD);
        List<TradeDecision> decisions = translator.translateSignals(signals, chain);

        assertEquals(2, decisions.size());
        assertEquals("CALL", decisions.get(0).contract().type());
        assertEquals("BUY", decisions.get(0).action());
        assertEquals("PUT", decisions.get(1).contract().type());
        assertEquals("BUY", decisions.get(1).action());
    }

    @Test
    public void testRepairLogic() {
        RepairLogic repairLogic = new RepairLogic();

        OptionContract call = new OptionContract("TSLA", "CALL", 100.0, LocalDate.now().plusDays(35), 0.32, 35, 5.0);
        TradeDecision trade = new TradeDecision(call, "BUY");

        // Current price with >20% loss
        double currentPrice = 5.0 * 0.75; // 25% loss
        String suggestion = repairLogic.suggestRepair(trade, currentPrice);
        assertEquals("ROLL", suggestion);

        // For put
        OptionContract put = new OptionContract("TSLA", "PUT", 100.0, LocalDate.now().plusDays(40), -0.28, 40, 4.0);
        TradeDecision tradePut = new TradeDecision(put, "BUY");
        double currentPricePut = 4.0 * 0.75;
        String suggestionPut = repairLogic.suggestRepair(tradePut, currentPricePut);
        assertEquals("HARD_STOP", suggestionPut);

        // For no loss
        double currentPriceGood = 5.0 * 1.1;
        String suggestionGood = repairLogic.suggestRepair(trade, currentPriceGood);
        assertEquals("NONE", suggestionGood);
    }
}
