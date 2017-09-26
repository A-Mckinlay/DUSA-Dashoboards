package uk.ac.dundee.ac41004.team9.controllers;

import io.drakon.spark.autorouter.Routes;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;
import uk.ac.dundee.ac41004.team9.SecurityManager;

import static uk.ac.dundee.ac41004.team9.Render.mustache;

@UtilityClass
public class Logout {

    @Routes.GET(path = "/logout")
    public static Object logout(Request req, Response res) {
        SecurityManager.getSyn().logoutUser(req.attribute("user"), req, res);
        return mustache(req, "logout");
    }

}
