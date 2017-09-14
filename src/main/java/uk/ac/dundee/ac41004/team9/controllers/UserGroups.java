package uk.ac.dundee.ac41004.team9.controllers;

import io.drakon.spark.autorouter.Routes;
import spark.Request;
import spark.Response;

import static uk.ac.dundee.ac41004.team9.Render.mustache;

public class UserGroups {
    @Routes.GET(path="/usergroups")
    public static Object userGroupsRoute(Request req, Response res) {
        return mustache("usergroups");
    }
}
