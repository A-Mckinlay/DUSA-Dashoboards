package uk.ac.dundee.ac41004.team9.api;

import spark.ResponseTransformer;

/**
 * Spark ResponseTransformer for GSON.
 */
public class GSONResponseTransformer implements ResponseTransformer {

    @Override
    public String render(Object model) throws Exception {
        return Common.GSON.toJson(model);
    }

}
