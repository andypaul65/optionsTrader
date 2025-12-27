package com.optionstrader.ingestion;

import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.DecimalNum;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
            Map<String, Object> root = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            List<Map<String, Object>> results = (List<Map<String, Object>>) root.get("results");
            BarSeries series = new BaseBarSeries();
            for (Map<String, Object> result : results) {
                long timestampMs = ((Number) result.get("t")).longValue();
                double open = ((Number) result.get("o")).doubleValue();
                double high = ((Number) result.get("h")).doubleValue();
                double low = ((Number) result.get("l")).doubleValue();
                double close = ((Number) result.get("c")).doubleValue();
                long volume = ((Number) result.get("v")).longValue();
                ZonedDateTime zdt = Instant.ofEpochMilli(timestampMs).atZone(ZoneId.systemDefault());
                BaseBar bar = new BaseBar(
                    Duration.ofMinutes(1),
                    zdt,
                    DecimalNum.valueOf(open),
                    DecimalNum.valueOf(high),
                    DecimalNum.valueOf(low),
                    DecimalNum.valueOf(close),
                    DecimalNum.valueOf(volume),
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
