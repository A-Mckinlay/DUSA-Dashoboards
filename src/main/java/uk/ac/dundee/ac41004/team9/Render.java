package uk.ac.dundee.ac41004.team9;

import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.HashMap;
import java.util.Map;

public class Render {

    public static String mustache(String template, Map<String, Object> model) {
        return new MustacheTemplateEngine().render(new ModelAndView(model, template + ".mustache"));
    }

    public static String mustache(String template) {
        return mustache(template, new HashMap<>());
    }

}
