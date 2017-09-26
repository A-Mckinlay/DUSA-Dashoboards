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

/**
 * Utilities useful in most/all API classes.
 */
@UtilityClass
@Slf4j
public class Common {

    /** The datetime formatter string for JS-style date objects. */
    static final String DT_FORMAT_JS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    /** A datetime formatter based on the JS-style format. */
    static final DateTimeFormatter DT_FORMATTER_JS = DateTimeFormatter.ofPattern(DT_FORMAT_JS);

    /** A common instance of Gson, with appropriate settings preconfigured. */
    static final Gson GSON = new GsonBuilder()
            .setDateFormat(DT_FORMAT_JS)
            .setPrettyPrinting()
            .create();

    /**
     * Gets the start and end datetime values from a request.
     *
     * @param req The Spark request.
     * @return A pair of start and end datetimes, or null if a parse error occurs.
     */
    static Pair<LocalDateTime, LocalDateTime> getStartEndFromRequest(Request req) {
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

    /**
     * Gets all datetimes within a range that are a multiple of the period (e.g. every day within the set)
     *
     * @param start The start of the range.
     * @param period Period between values.
     * @param end The end of the range.
     * @return A list of start-end datetime pairs for each duration.
     */
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
