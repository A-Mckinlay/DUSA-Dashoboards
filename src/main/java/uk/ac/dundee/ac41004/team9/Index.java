package uk.ac.dundee.ac41004.team9;

import io.drakon.spark.autorouter.Routes;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.HashMap;
import java.util.Map;

public class Index {
    @Routes.GET(path="/")
    public static Object indexRoute(Request req, Response res){
        Map<String, Object> model = new HashMap<>();
        return new MustacheTemplateEngine().render(new ModelAndView(model, "index.mustache"));
    }
}
