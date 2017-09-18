package uk.ac.dundee.ac41004.team9;

import lombok.experimental.UtilityClass;
import spark.ModelAndView;
import spark.Request;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class Render {

    public static String mustache(Request req, String template, Map<String, Object> model) {
        if (req.attribute("user") != null) model.put("user", req.attribute("user"));
        return new MustacheTemplateEngine().render(new ModelAndView(model, template + ".mustache"));
    }

    public static String mustache(Request req, String template) {
        return mustache(req, template, new HashMap<>());
    }

}
