package uk.ac.dundee.ac41004.team9.api;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.drakon.spark.autorouter.Routes;
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

    @Routes.GET(path = "/top5", transformer = GSONResponseTransformer.class)
    public static Object topFive(Request req, Response res) {
        DateRangeRequest jsonReq = DateRangeRequest.fromBody(req.body());
        if (jsonReq == null) {
            res.status(400);
            return immutableMapOf("error", "invalid request");
        }

        // TODO: Offload processing to DB better.
        List<DisbursalsRow> ls = Disbursals.getRowsBetween(jsonReq.getStartJ8(), jsonReq.getEndJ8());
        Map<String, BigDecimal> outlets = new HashMap<>();

        if (ls == null) {
            res.status(500);
            return immutableMapOf("error", "internal server error");
        }

        ls.forEach(row -> {
            BigDecimal current = outlets.getOrDefault(row.getOutlet(),
                    new BigDecimal(0.0).setScale(2, BigDecimal.ROUND_UNNECESSARY));
            current = current.add(row.getCashSpent());
            outlets.put(row.getOutlet(), current);
        });

        return outlets.entrySet().stream()
                .sorted((a,b) -> - (a.getValue().compareTo(b.getValue())))
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
