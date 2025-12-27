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
import java.time.Instant;
import java.time.ZoneId;
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
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new RuntimeException("Expected start of object");
            }
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.getCurrentName();
                if ("results".equals(fieldName)) {
                    parser.nextToken(); // START_ARRAY
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        long timestampMs = 0;
                        double open = 0, high = 0, low = 0, close = 0;
                        long volume = 0;
                        while (parser.nextToken() != JsonToken.END_OBJECT) {
                            String key = parser.getCurrentName();
                            parser.nextToken();
                            switch (key) {
                                case "t":
                                    timestampMs = parser.getLongValue();
                                    break;
                                case "o":
                                    open = parser.getDoubleValue();
                                    break;
                                case "h":
                                    high = parser.getDoubleValue();
                                    break;
                                case "l":
                                    low = parser.getDoubleValue();
                                    break;
                                case "c":
                                    close = parser.getDoubleValue();
                                    break;
                                case "v":
                                    volume = parser.getLongValue();
                                    break;
                            }
                        }
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
                } else {
                    parser.skipChildren();
                }
            }
            return series;
        } catch (Exception e) {
            throw new RuntimeException("Error in streaming parsing", e);
        }
    }
}
