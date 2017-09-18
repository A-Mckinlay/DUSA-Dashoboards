package uk.ac.dundee.ac41004.team9.controllers;

import io.drakon.spark.autorouter.Routes;
import lombok.experimental.UtilityClass;
import spark.Request;
import spark.Response;

import static uk.ac.dundee.ac41004.team9.Render.mustache;

@UtilityClass
public class Herds {

    @Routes.GET(path="/herds")
    public static Object userGroupsRoute(Request req, Response res) {
        return mustache(req, "herds");
    }

}
