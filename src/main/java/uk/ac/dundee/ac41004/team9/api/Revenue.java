package uk.ac.dundee.ac41004.team9.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import io.drakon.spark.autorouter.Routes;
import lombok.Data;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;
import uk.ac.dundee.ac41004.team9.data.DisbursalsRow;
import uk.ac.dundee.ac41004.team9.models.Disbursals;

import static uk.ac.dundee.ac41004.team9.util.CollectionUtils.immutableMapOf;

@UtilityClass
@Slf4j
@Routes.PathGroup(prefix = "/api/revenue")
public class Revenue {

    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

    @Data
    private static class RevenueRequest {
        // We use the *old* Date class, because Gson barfs on LocalDateTime.
        private final Date start;
        private final Date end;
    }

    @Routes.GET(path = "/top5", transformer = GSONResponseTransformer.class)
    public static Object topFive(Request req, Response res) {
        log.debug(req.body());
        RevenueRequest jsonReq;
        try {
            jsonReq = GSON.fromJson(req.body(), RevenueRequest.class);
        } catch (JsonSyntaxException ex) {
            log.error("JSON parse error", ex);
            res.status(400);
            return immutableMapOf("error", "invalid request");
        }

        LocalDateTime start = LocalDateTime.ofInstant(jsonReq.start.toInstant(), ZoneOffset.UTC);
        LocalDateTime end = LocalDateTime.ofInstant(jsonReq.end.toInstant(), ZoneOffset.UTC);
        List<DisbursalsRow> ls = Disbursals.getRowsBetween(start, end);
        Map<String, BigDecimal> outlets = new HashMap<>();

        if (ls == null) {
            res.status(500);
            return immutableMapOf("error", "internal server error");
        }

        ls.forEach(row -> {
            BigDecimal current = outlets.getOrDefault(row.getOutlet(),
                    new BigDecimal(0.0).setScale(2, BigDecimal.ROUND_UNNECESSARY));
            // TODO: Check whether this should be getCashSpent() instead.
            current = current.add(row.getTotalAmount());
            outlets.put(row.getOutlet(), current);
        });

        return outlets.entrySet().stream()
                .sorted((a,b) -> - (a.getValue().compareTo(b.getValue())))
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
