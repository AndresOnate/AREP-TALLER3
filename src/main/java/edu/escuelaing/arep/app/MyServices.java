package edu.escuelaing.arep.app;

import static edu.escuelaing.arep.app.MySpark.get;

public class MyServices {

    public static void main(String[] args) throws Exception {
        get("/hi", (req) -> "El query es:" + req);

        get("/nameServer", (req) -> "Hi " + req);
        MySpark.getInstance().runServer(args);
    }
}