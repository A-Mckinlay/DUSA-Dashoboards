package uk.ac.dundee.ac41004.team9.filters;

import io.drakon.spark.autorouter.Routes;
import lombok.experimental.UtilityClass;
import spark.Request;
import spark.Response;

/**
 * Header-manipulating filters. Sets Server and Content-Encoding headers (the latter enables GZIP).
 */
@UtilityClass
public class HeadersFilter {

    @Routes.After
    public static Object after(Request req, Response res) {
        res.header("Server", "Dashoboards Server (Spark Java)");
        res.header("Content-Encoding", "gzip");
        return null;
    }

}
