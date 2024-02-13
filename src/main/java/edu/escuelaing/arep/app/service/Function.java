package edu.escuelaing.arep.app.service;

import java.io.IOException;
import java.net.URI;

public interface Function {

    public String handle (URI path) throws IOException;
}
