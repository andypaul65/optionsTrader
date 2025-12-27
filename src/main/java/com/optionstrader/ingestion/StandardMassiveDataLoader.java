package com.optionstrader.ingestion;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.DecimalNum;

import java.time.Duration;
import java.util.List;

/**
 * Standard parsing implementation using Jackson ObjectMapper to deserialize JSON to POJOs.
 */
public class StandardMassiveDataLoader implements MassiveDataLoader {

    private final ObjectMapper objectMapper;

    public StandardMassiveDataLoader() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public BarSeries loadData(String json) {
        try {
            List<BarData> barDataList = objectMapper.readValue(json, new TypeReference<List<BarData>>() {});
            BarSeries series = new BaseBarSeries();
            for (BarData barData : barDataList) {
                Duration timePeriod = Duration.ofMinutes(1);
                BaseBar bar = new BaseBar(
                    timePeriod,
                    barData.getTimestamp(),
                    DecimalNum.valueOf(barData.getOpen()),
                    DecimalNum.valueOf(barData.getHigh()),
                    DecimalNum.valueOf(barData.getLow()),
                    DecimalNum.valueOf(barData.getClose()),
                    DecimalNum.valueOf(barData.getVolume()),
                    DecimalNum.valueOf(0) // trades
                );
                series.addBar(bar);
            }
            return series;
        } catch (Exception e) {
            throw new RuntimeException("Error in standard parsing", e);
        }
    }
}
