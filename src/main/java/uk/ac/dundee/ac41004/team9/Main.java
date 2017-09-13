package uk.ac.dundee.ac41004.team9;

import io.drakon.spark.autorouter.Autorouter;
import uk.ac.dundee.ac41004.team9.db.DBConnManager;

public class Main {

    public static void main(String[] argv) {
        Config.init();
        DBConnManager.init();

        Autorouter autorouter = new Autorouter("uk.ac.dundee.ac41004.team9");
        autorouter.route();
    }

}
