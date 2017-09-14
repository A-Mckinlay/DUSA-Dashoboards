package uk.ac.dundee.ac41004.team9.controllers;

import io.drakon.spark.autorouter.Routes;
import spark.Request;
import spark.Response;

import static uk.ac.dundee.ac41004.team9.Render.mustache;

public class Activity {
    @Routes.GET(path="/activity")
    public static Object activityRoute(Request req, Response res) {
        return mustache("activity");
    }
}
