package com.optionstrader.ingestion;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.num.DecimalNum;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * Streaming parsing implementation using Jackson JsonParser for incremental processing.
 */
public class StreamingMassiveDataLoader implements MassiveDataLoader {

    private final ObjectMapper objectMapper;

    public StreamingMassiveDataLoader() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public BarSeries loadData(String json) {
        try (JsonParser parser = objectMapper.getFactory().createParser(json)) {
            BarSeries series = new BaseBarSeries();
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new RuntimeException("Expected start of array");
            }
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                ZonedDateTime timestamp = null;
                double open = 0, high = 0, low = 0, close = 0;
                long volume = 0;
                while (parser.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = parser.getCurrentName();
                    parser.nextToken();
                    switch (fieldName) {
                        case "timestamp":
                            timestamp = parser.readValueAs(ZonedDateTime.class);
                            break;
                        case "open":
                            open = parser.getDoubleValue();
                            break;
                        case "high":
                            high = parser.getDoubleValue();
                            break;
                        case "low":
                            low = parser.getDoubleValue();
                            break;
                        case "close":
                            close = parser.getDoubleValue();
                            break;
                        case "volume":
                            volume = parser.getLongValue();
                            break;
                    }
                }
                if (timestamp != null) {
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
            }
            return series;
        } catch (Exception e) {
            throw new RuntimeException("Error in streaming parsing", e);
        }
    }
}
