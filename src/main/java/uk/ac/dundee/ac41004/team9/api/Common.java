package uk.ac.dundee.ac41004.team9.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import uk.ac.dundee.ac41004.team9.util.Pair;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
@Slf4j
public class Common {

    static final String DT_FORMAT_JS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    static final DateTimeFormatter DT_FORMATTER_JS = DateTimeFormatter.ofPattern(DT_FORMAT_JS);

    static final Gson GSON = new GsonBuilder()
            .setDateFormat(DT_FORMAT_JS)
            .setPrettyPrinting()
            .create();

    public static Pair<LocalDateTime, LocalDateTime> getStartEndFromRequest(Request req) {
        String startStr = req.queryParams("start");
        String endStr = req.queryParams("end");
        if (startStr == null || endStr == null) return null;
        try {
            LocalDateTime start = LocalDateTime.from(DT_FORMATTER_JS.parse(startStr));
            LocalDateTime end = LocalDateTime.from(DT_FORMATTER_JS.parse(endStr));
            return new Pair<>(start, end);
        } catch (DateTimeParseException ex) {
            log.debug("start/end parse error.", ex);
            return null;
        }
    }

    static List<Pair<LocalDateTime, LocalDateTime>> getIntervalsBetween(LocalDateTime start,
                                                                        TemporalAmount period,
                                                                        LocalDateTime end) {
        List<Pair<LocalDateTime, LocalDateTime>> pairs = new ArrayList<>();
        LocalDateTime current = start;
        while (current.isBefore(end)) {
            LocalDateTime next = current.plus(period);
            pairs.add(new Pair<>(current, next));
            current = next;
        }
        return pairs;
    }
}
