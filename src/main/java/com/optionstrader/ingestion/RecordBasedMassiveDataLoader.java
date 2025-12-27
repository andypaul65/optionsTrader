package com.optionstrader.ingestion;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.DecimalNum;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * Record-based parsing implementation using Jackson to deserialize JSON to maps.
 */
public class RecordBasedMassiveDataLoader implements MassiveDataLoader {

    private final ObjectMapper objectMapper;

    public RecordBasedMassiveDataLoader() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public BarSeries loadData(String json) {
        try {
            List<Map<String, Object>> records = objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
            BarSeries series = new BaseBarSeries();
            for (Map<String, Object> record : records) {
                ZonedDateTime timestamp = ZonedDateTime.parse((String) record.get("timestamp"));
                double open = ((Number) record.get("open")).doubleValue();
                double high = ((Number) record.get("high")).doubleValue();
                double low = ((Number) record.get("low")).doubleValue();
                double close = ((Number) record.get("close")).doubleValue();
                long volume = ((Number) record.get("volume")).longValue();
                BaseBar bar = new BaseBar(
                    Duration.ofMinutes(1),
                    timestamp,
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
            throw new RuntimeException("Error in record-based parsing", e);
        }
    }
}
