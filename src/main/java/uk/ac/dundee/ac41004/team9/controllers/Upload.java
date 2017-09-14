package uk.ac.dundee.ac41004.team9.controllers;

import io.drakon.spark.autorouter.Routes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import uk.ac.dundee.ac41004.team9.xssf.YoyoXSSFParser;

import java.io.InputStream;
import java.util.List;

import static jdk.nashorn.internal.runtime.regexp.joni.Syntax.Java;
import static uk.ac.dundee.ac41004.team9.Render.mustache;
import static uk.ac.dundee.ac41004.team9.xssf.YoyoXSSFParser.parseSheet;

public class Upload {

    private static final Logger log = LoggerFactory.getLogger(Upload.class);

    @Routes.GET(path="/upload")
    public static Object uploadPageRoute(Request req, Response res) {
        return mustache("upload");
    }

    @Routes.POST(path="/upload")
    public static Object uploadFileRoute(Request req, Response res){
        log.info("Hello I am in the controller for Upload");
        res = uploadDisbursalFile(req, res);
        return null;
    }

    private static Response uploadDisbursalFile(Request req, Response res){
        try(InputStream is = req.raw().getPart("disbursal-file").getInputStream()){
            List<YoyoXSSFParser.YoyoWeekSpreadsheetRow> disbursalSheet;
            disbursalSheet = YoyoXSSFParser.parseSheet(is);
            log.info("in the controller doing the things");
            //TODO: write upload codeszzzzz
            return null;
        }catch(java.io.IOException | javax.servlet.ServletException e){
            log.error("upload failed", e);
            res.status(500);
            return res;
        }
    }
}
