package uk.ac.dundee.ac41004.team9;

import lombok.experimental.UtilityClass;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class Render {

    public static String mustache(String template, Map<String, Object> model) {
        return new MustacheTemplateEngine().render(new ModelAndView(model, template + ".mustache"));
    }

    public static String mustache(String template) {
        return mustache(template, new HashMap<>());
    }

}
