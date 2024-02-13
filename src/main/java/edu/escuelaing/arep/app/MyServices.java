package edu.escuelaing.arep.app;

import edu.escuelaing.arep.app.controller.APIController;
import edu.escuelaing.arep.app.model.ResponseBuilder;

import static edu.escuelaing.arep.app.MySpark.get;

public class MyServices {

    public static void main(String[] args) throws Exception {

        // root is 'src/main/resources', so put files in 'src/main/resources/public
        MySpark.setLocation("/public");

        get("/hi", (req) -> { return "El query es:" + req.getPath();});

        get("/movies", (req) -> {
            ResponseBuilder.setResponseType("application/json");
            String title = req.getQuery().split("=")[1].toLowerCase();
            APIController apiMovies = new APIController();
            return  apiMovies.connectToMoviesAPI(title);
        });

        MySpark.getInstance().runServer(args);
    }
}