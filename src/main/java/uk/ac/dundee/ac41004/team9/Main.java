package uk.ac.dundee.ac41004.team9;

import static spark.Spark.*;

public class Main {

    public static void main(String[] argv) {
        get("/ping", (req, res) -> "Pong");
    }

}
