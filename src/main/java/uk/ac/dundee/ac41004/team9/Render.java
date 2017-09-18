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
        HashMap<String, Object> attrs = new HashMap<>();
        req.attributes().forEach(k -> attrs.put(k, req.attribute(k)));
        model.putAll(attrs);
        return new MustacheTemplateEngine().render(new ModelAndView(model, template + ".mustache"));
    }

    public static String mustache(Request req, String template) {
        return mustache(req, template, new HashMap<>());
    }

}
