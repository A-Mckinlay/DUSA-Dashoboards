package uk.ac.dundee.ac41004.team9.api;

import spark.ResponseTransformer;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateResponseTransformer implements ResponseTransformer {

    @Override
    public String render(Object model) throws Exception {
        if (model == null) return null;
        if (model instanceof Date) model = LocalDateTime.ofInstant(((Date) model).toInstant(), ZoneOffset.UTC);
        if (model instanceof LocalDateTime) {
            LocalDateTime ldt = (LocalDateTime)model;
            return ldt.format(DateTimeFormatter.ofPattern(Common.DT_FORMAT_JS));
        } else {
            throw new IllegalArgumentException("Not a Date or LocalDateTime!");
        }
    }

}
