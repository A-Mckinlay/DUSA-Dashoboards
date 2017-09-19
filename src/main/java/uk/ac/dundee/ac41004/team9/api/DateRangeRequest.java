package uk.ac.dundee.ac41004.team9.api;

import com.google.gson.JsonSyntaxException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Data
@Slf4j
class DateRangeRequest {
    // We use the *old* Date class, because Gson barfs on LocalDateTime.
    public final Date start;
    public final Date end;

    public static DateRangeRequest fromBody(String body) {
        try {
            return Common.GSON.fromJson(body, DateRangeRequest.class);
        } catch (JsonSyntaxException ex) {
            log.error("JSON parse error", ex);
            return null;
        }
    }

    public LocalDateTime getStartJ8() {
        return toDateTime(start);
    }

    public LocalDateTime getEndJ8() {
        return toDateTime(end);
    }

    private LocalDateTime toDateTime(Date src) {
        return LocalDateTime.ofInstant(src.toInstant(), ZoneOffset.UTC);
    }

}
