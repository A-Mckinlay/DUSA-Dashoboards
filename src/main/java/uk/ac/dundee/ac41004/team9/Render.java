package uk.ac.dundee.ac41004.team9;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.resolver.FileSystemResolver;
import lombok.experimental.UtilityClass;
import spark.ModelAndView;
import spark.Request;
import spark.template.mustache.MustacheTemplateEngine;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
@ParametersAreNonnullByDefault
public class Render {

    // Get a new template engine to use.
    private MustacheTemplateEngine getEngine() {
        File f = Config.getDevResourcesPath();
        if (f == null) {
            return new MustacheTemplateEngine();
        } else {
            f = new File(f.getPath() + File.separator + "templates");
            return new MustacheTemplateEngine(new DefaultMustacheFactory(new FileSystemResolver(f)));
        }
    }

    /**
     * Render a named Mustache template with a provided model map.
     *
     * @param req The current Spark request (for loading attrs into templates)
     * @param template The template file name, without the ".mustache" extension.
     * @param model The model to pass to the template.
     * @return The rendered view.
     */
    public static String mustache(Request req, String template, Map<String, Object> model) {
        HashMap<String, Object> attrs = new HashMap<>();
        req.attributes().forEach(k -> attrs.put(k, req.attribute(k)));
        model.putAll(attrs);
        return getEngine().render(new ModelAndView(model, template + ".mustache"));
    }

    /**
     * Render a named mustache template without a model.
     *
     * @param req The current Spark request (for loading attrs into templates)
     * @param template The template file name, without the ".mustache" extension.
     * @return The rendered view.
     */
    public static String mustache(Request req, String template) {
        return mustache(req, template, new HashMap<>());
    }

}
