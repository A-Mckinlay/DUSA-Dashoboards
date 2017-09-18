package uk.ac.dundee.ac41004.team9.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;

import io.drakon.spark.autorouter.Routes;
import lombok.Lombok;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import spark.Request;
import spark.Response;
import uk.ac.dundee.ac41004.team9.Config;
import uk.ac.dundee.ac41004.team9.db.DBConnManager;
import uk.ac.dundee.ac41004.team9.db.DBIngest;
import uk.ac.dundee.ac41004.team9.xssf.YoyoParseException;
import uk.ac.dundee.ac41004.team9.xssf.YoyoWeekSpreadsheetRow;
import uk.ac.dundee.ac41004.team9.xssf.YoyoXSSFParser;

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
                return renderStatus(req, res, 400, "File Already Uploaded",
                        "This files data is already in the system.", "warning");
            }
            boolean success = DBIngest.uploadRowsToDB(disbursalSheet);
            if (!success) {
                return renderStatus(req, res, 500,
                        "Server Error", "The server was unable to upload your data to its database. Ask your system " +
                                "administrator for assistance if this persists.", "danger");
            }
            return renderStatus(req, res, 200, "Success",
                    "Your file has been uploaded. The new data should appear in your dashboards shortly.", "success");
        } catch (IOException | ServletException e) {
            log.error("upload failed", e);
            return renderStatus(req, res, 500,
                    "Upload Failed", "The server was unable to receive your file.", "danger");
        } catch (YoyoParseException e) {
            log.error("upload failed; parse error.", e);
            res.status(400);
            return renderStatus(req, res, 400,
                    "Invalid File", "The file you uploaded could not be parsed. Check the file you selected is a " +
                            "valid Yoyo transactions Excel file.", "warning");
        } catch (SQLException ex) {
            log.error("SQL exception in upload.", ex);
            return renderStatus(req, res, 500,
                    "Server Error", "The server was unable to upload your data to its database. Ask your system " +
                            "administrator for assistance if this persists.", "danger");
        } catch (IllegalStateException ex) {
            // File too large
            log.error("File too large!");
            return renderStatus(req, res, 400,
                    "File Too Large", "The file you selected is too large to upload through this page.", "warning");
        } catch (Exception ex) {
            log.error("error in upload; unrecognised exception type.", ex);
            return renderStatus(req, res, 500,
                    "Server Error", "The server encountered a problem handling your upload. Contact your system " +
                            "administrator for assistance.", "danger");
        }
    }

    private static Object renderStatus(Request req, Response res, int status, String renderTitle, String renderText,
                                       String renderClass) {
        res.status(status);
        HashMap<String, Object> model = new HashMap<>();
        model.put("renderTitle", renderTitle);
        model.put("renderText", renderText);
        model.put("renderClass", renderClass);
        return mustache(req, "upload", model);
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
