package edu.escuelaing.arep.app.service;

import edu.escuelaing.arep.app.model.Request;

import java.io.IOException;
import java.net.URI;

public interface Function {

    public String handle (Request request) throws IOException;
}
