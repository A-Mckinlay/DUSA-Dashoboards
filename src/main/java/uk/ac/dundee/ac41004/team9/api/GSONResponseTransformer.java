package uk.ac.dundee.ac41004.team9.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.ResponseTransformer;

/**
 * Spark ResponseTransformer for GSON.
 */
public class GSONResponseTransformer implements ResponseTransformer {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public String render(Object model) throws Exception {
        return GSON.toJson(model);
    }

}
