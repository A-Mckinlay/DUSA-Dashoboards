package uk.ac.dundee.ac41004.team9;

import io.drakon.spark.autorouter.Autorouter;
import spark.Spark;
import uk.ac.dundee.ac41004.team9.db.DBConnManager;

public class Main {

    public static void main(String[] argv) {
        Config.init();
        DBConnManager.init();
        Spark.staticFiles.location("/static");
        SecurityManager.init();

        Autorouter autorouter = new Autorouter("uk.ac.dundee.ac41004.team9");
        autorouter.route();
    }

}
