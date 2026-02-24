package org.example.tudubem.llm.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

@Component
public class DefaultTools {

    private static final Map<String, String> CITY_ZONE_MAP = Map.ofEntries(
            Map.entry("seoul", "Asia/Seoul"),
            Map.entry("tokyo", "Asia/Tokyo"),
            Map.entry("london", "Europe/London"),
            Map.entry("paris", "Europe/Paris"),
            Map.entry("berlin", "Europe/Berlin"),
            Map.entry("newyork", "America/New_York"),
            Map.entry("new york", "America/New_York"),
            Map.entry("losangeles", "America/Los_Angeles"),
            Map.entry("los angeles", "America/Los_Angeles"),
            Map.entry("chicago", "America/Chicago"),
            Map.entry("singapore", "Asia/Singapore"),
            Map.entry("sydney", "Australia/Sydney")
    );

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    @Tool(description = "Return current date and time for the given city. You can pass a city name (e.g., Seoul, New York) or a timezone ID (e.g., Asia/Seoul).")
    public String getDateTime(String city) {
        if (city == null || city.isBlank()) {
            return "City is required. Example: Seoul or Asia/Seoul";
        }

        String input = city.trim();
        String normalized = input.toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("\\s+", " ");
        String compact = normalized.replace(" ", "");

        String zoneId = CITY_ZONE_MAP.getOrDefault(normalized, CITY_ZONE_MAP.get(compact));
        if (zoneId == null) {
            zoneId = input;
        }

        try {
            ZoneId zone = ZoneId.of(zoneId);
            ZonedDateTime now = ZonedDateTime.now(zone);
            return FORMATTER.format(now) + " (" + zone + ")";
        } catch (DateTimeException e) {
            return "Unknown city/timezone: " + city + ". Example: Seoul, New York, Asia/Seoul";
        }
    }
}
