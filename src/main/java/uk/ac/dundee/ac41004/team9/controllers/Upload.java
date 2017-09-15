package uk.ac.dundee.ac41004.team9.controllers;

import io.drakon.spark.autorouter.Routes;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;
import uk.ac.dundee.ac41004.team9.db.DBIngest;
import uk.ac.dundee.ac41004.team9.xssf.YoyoParseException;
import uk.ac.dundee.ac41004.team9.xssf.YoyoWeekSpreadsheetRow;
import uk.ac.dundee.ac41004.team9.xssf.YoyoXSSFParser;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static spark.Spark.halt;
import static uk.ac.dundee.ac41004.team9.Render.mustache;

@Slf4j
public class Upload {

    @Routes.GET(path="/upload")
    public static Object uploadPageRoute(Request req, Response res) {
        return mustache("upload");
    }

    @Routes.POST(path="/upload")
    public static Object uploadFileRoute(Request req, Response res) {
        log.info("Hello I am in the controller for Upload");
        req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
        try (InputStream is = req.raw().getPart("disbursal-file").getInputStream()) {
            List<YoyoWeekSpreadsheetRow> disbursalSheet;
            disbursalSheet = YoyoXSSFParser.parseSheet(is);
            log.info("in the controller doing the things");
            boolean success = DBIngest.uploadRowsToDB(disbursalSheet);
            if (!success) throw halt(500, "something went WRONG D:");
            return "upload complete"; // TODO: Proper success page
        } catch (IOException | ServletException e) {
            log.error("upload failed", e);
            res.status(500);
            return res;
        } catch (YoyoParseException e) {
            log.error("upload failed; parse error.", e);
            res.status(400);
            return "invalid file"; // TODO: Proper error page
        } catch (Exception ex) {
            log.error("error in upload; probably invalid file.", ex);
            res.status(400);
            return "invalid file"; // TODO: Proper error page
        }
    }
}
