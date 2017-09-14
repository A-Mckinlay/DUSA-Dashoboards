package uk.ac.dundee.ac41004.team9.security;

import io.drakon.spark.autorouter.Routes;
import spark.Request;
import spark.Response;

import static uk.ac.dundee.ac41004.team9.Render.mustache;

public class Login {
    @Routes.GET(path="/login")
    public static Object loginRoute(Request req, Response res) {
        return mustache("login");
    }

    @Routes.POST(path = "/login")
    public static Object loginPost(Request req, Response res) {
        String username = req.attribute("username");
        return null; // TODO
    }
}
