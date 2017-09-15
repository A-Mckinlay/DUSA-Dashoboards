package uk.ac.dundee.ac41004.team9.filters;

import io.drakon.spark.autorouter.Routes;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;

/**
 * Filters responsable for logging request/response events.
 */
@Slf4j
public class LoggingFilter {

    @Routes.After
    public static Object after(Request req, Response res) {
        log.debug("{} {} {} {}", req.ip(), req.requestMethod(), req.pathInfo(), res.status());
        return null;
    }

}
