package uk.ac.dundee.ac41004.team9;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.resolver.FileSystemResolver;
import lombok.experimental.UtilityClass;
import spark.ModelAndView;
import spark.Request;
import spark.template.mustache.MustacheTemplateEngine;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class Render {

    private MustacheTemplateEngine getEngine() {
        File f = Config.getDevResourcesPath();
        if (f == null) {
            return new MustacheTemplateEngine();
        } else {
            f = new File(f.getPath() + File.separator + "templates");
            return new MustacheTemplateEngine(new DefaultMustacheFactory(new FileSystemResolver(f)));
        }
    }

    public static String mustache(Request req, String template, Map<String, Object> model) {
        HashMap<String, Object> attrs = new HashMap<>();
        req.attributes().forEach(k -> attrs.put(k, req.attribute(k)));
        model.putAll(attrs);
        return getEngine().render(new ModelAndView(model, template + ".mustache"));
    }

    public static String mustache(Request req, String template) {
        return mustache(req, template, new HashMap<>());
    }

}
