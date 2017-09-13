package uk.ac.dundee.ac41004.team9.controllers;

import io.drakon.spark.autorouter.Routes;
import spark.Request;
import spark.Response;

import static uk.ac.dundee.ac41004.team9.Render.mustache;

public class Upload {
    @Routes.GET(path="/upload")
    public static Object uploadPageRoute(Request req, Response res) {
        return mustache("upload");
    }

    @Routes.POST(path="/upload")
    public static Object uploadFileRoute(Request req, Response res){
        return null; //TODO: Send file to parser
    }

}
