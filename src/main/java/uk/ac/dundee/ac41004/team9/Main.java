package uk.ac.dundee.ac41004.team9;

import io.drakon.spark.autorouter.Autorouter;
import spark.Spark;
import uk.ac.dundee.ac41004.team9.db.DBConnManager;

import java.io.File;

public class Main {

    public static void main(String[] argv) {
        Config.init();
        DBConnManager.init();

        if (Config.getDevResourcesPath() != null) {
            String dir = Config.getDevResourcesPath().getPath() + File.separator + "static";
            Spark.staticFiles.externalLocation(dir);
        } else {
            Spark.staticFiles.location("/static");
        }
        SecurityManager.init();

        Autorouter autorouter = new Autorouter("uk.ac.dundee.ac41004.team9");
        autorouter.route();
        if (Config.isDevRouteOverview()) autorouter.enableRouteOverview("/debug/routes");
    }

}
