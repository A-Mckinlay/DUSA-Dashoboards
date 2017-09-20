package uk.ac.dundee.ac41004.team9.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Common {

    static final String DT_FORMAT_JS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    static final Gson GSON = new GsonBuilder()
            .setDateFormat(DT_FORMAT_JS)
            .setPrettyPrinting()
            .create();

}
