package uk.ac.dundee.ac41004.team9.controllers;

import io.drakon.spark.autorouter.Routes;
import lombok.Lombok;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import spark.HaltException;
import spark.Request;
import spark.Response;
import uk.ac.dundee.ac41004.team9.Config;
import uk.ac.dundee.ac41004.team9.db.DBConnManager;
import uk.ac.dundee.ac41004.team9.db.DBIngest;
import uk.ac.dundee.ac41004.team9.xssf.YoyoParseException;
import uk.ac.dundee.ac41004.team9.xssf.YoyoWeekSpreadsheetRow;
import uk.ac.dundee.ac41004.team9.xssf.YoyoXSSFParser;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static spark.Spark.halt;
import static uk.ac.dundee.ac41004.team9.Render.mustache;

@Slf4j
@UtilityClass
public class Upload {

    private static final String FILE_LOCATION = "tmp";
    private static final int MAX_FILE_SIZE = 1024 * 1024; // 1MB in Bytes.
    private static final int MAX_REQUEST_SIZE = MAX_FILE_SIZE;
    private static final int FILE_SIZE_THRESHOLD = MAX_FILE_SIZE; // No files on disk.

    @Routes.GET(path="/upload")
    public static Object uploadPageRoute(Request req, Response res) {
        return mustache(req, "upload");
    }

    @Routes.POST(path="/upload")
    public static Object uploadFileRoute(Request req, Response res) {
        // Multipart config via https://groups.google.com/forum/#!msg/sparkjava/fjO64BP1UQw/CsxdNVz7qrAJ
        MultipartConfigElement multipartConfigElement;
        if (Config.isSecLimitUploadSize()) {
            multipartConfigElement = new MultipartConfigElement(FILE_LOCATION, MAX_FILE_SIZE, MAX_REQUEST_SIZE,
                    FILE_SIZE_THRESHOLD);
        } else {
            multipartConfigElement = new MultipartConfigElement(FILE_LOCATION, -1L, -1L, FILE_SIZE_THRESHOLD);
        }
        req.attribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
        try (InputStream is = req.raw().getPart("disbursal-file").getInputStream()) {
            List<YoyoWeekSpreadsheetRow> disbursalSheet = YoyoXSSFParser.parseSheet(is);
            if (alreadyExists(disbursalSheet)) {
                throw halt(400, "sample already uploaded");
            }
            boolean success = DBIngest.uploadRowsToDB(disbursalSheet);
            if (!success) throw halt(500, "something went WRONG D:");
            return "upload complete"; // TODO: Proper success page
        } catch (HaltException ex) {
            throw ex; // Propagate Spark halts
        } catch (IOException | ServletException e) {
            log.error("upload failed", e);
            res.status(500);
            return res;
        } catch (YoyoParseException e) {
            log.error("upload failed; parse error.", e);
            res.status(400);
            return "invalid or too large file"; // TODO: Proper error page
        } catch (SQLException ex) {
            log.error("SQL exception in upload.", ex);
            res.status(500);
            return "database access error";
        } catch (Exception ex) {
            log.error("error in upload; probably invalid file.", ex);
            res.status(400);
            return "invalid file"; // TODO: Proper error page
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static boolean alreadyExists(List<YoyoWeekSpreadsheetRow> data) throws SQLException {
        // Pick something from the middle.
        int sample = data.size() / 2;
        LocalDateTime dt = data.get(sample).getDateTime();
        return DBConnManager.runWithConnection(connection -> {
            try {
                PreparedStatement ps = connection.prepareStatement("SELECT * FROM disbursals WHERE datetime = ?");
                ps.setTimestamp(1, Timestamp.valueOf(dt));
                return ps.executeQuery().next();
            } catch (SQLException ex) {
                throw Lombok.sneakyThrow(ex);
            }
        });
    }
}
