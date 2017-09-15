package uk.ac.dundee.ac41004.team9.controllers;

import static uk.ac.dundee.ac41004.team9.Render.*;

import io.drakon.spark.autorouter.Routes;
import lombok.experimental.UtilityClass;
import spark.Request;
import spark.Response;

@UtilityClass
public class Dashboard {

    @Routes.GET(path="/")
    public static Object indexRoute(Request req, Response res) {
        return mustache("dashboard");
    }

}
