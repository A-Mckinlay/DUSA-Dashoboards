package uk.ac.dundee.ac41004.team9.filters;

import io.drakon.spark.autorouter.Routes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

/**
 * Filters responsable for logging request/response events.
 */
public class LoggingFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Routes.After
    public static Object after(Request req, Response res) {
        log.debug("{} {} {} {}", req.ip(), req.requestMethod(), req.pathInfo(), res.status());
        return null;
    }

}
