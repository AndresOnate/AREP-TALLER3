package edu.escuelaing.arep.app;


import edu.escuelaing.arep.app.controller.APIController;
import edu.escuelaing.arep.app.controller.MovieAPI;
import edu.escuelaing.arep.app.model.ResponseBuilder;
import edu.escuelaing.arep.app.service.Function;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * The `HttpServer` class represents a simple HTTP server that listens on port 35000 and handles incoming HTTP requests.
 * It utilizes a basic API for retrieving movie information and responds with HTML content based on the request URI.
 *
 * It supports requests related to movie information and provides a default HTML response.
 */
public class MySpark
{
    /**
     * Represents the API controller for fetching movie information.
     */
    private static MovieAPI myMoviesAPI = new APIController();
    private static MySpark _instance = new MySpark();
    private static String location = "public";

    private static HashMap<String, Function> services = new HashMap<String, Function>();

    private MySpark(){}

    public static MySpark getInstance(){
        return _instance;
    }


    /**
     * The main method that serves as the entry point for the HTTP server.
     * This method sets up a server socket on port 35000 and continuously listens for incoming client connections.
     * When a client connection is accepted, it spawns a new thread to handle the client request.
     * @param args The command line arguments passed to the program.
     * @throws Exception If an error occurs during the execution of the server.
     */
    public void runServer( String[] args ) throws Exception
    {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        Boolean running = true;
        while (running) {
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
                handleClientRequest(clientSocket);
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
        }
        serverSocket.close();
    }

    /**
     * Handles incoming client requests on the server.
     * Reads client requests, processes the request, and sends a response back to the client.
     * @param clientSocket The client socket through which communication with the server occurs.
     * @throws IOException If an I/O error occurs while reading from or writing to the client socket.
     * @throws URISyntaxException If an error occurs while parsing the URI of the request.
     */
    private void handleClientRequest(Socket clientSocket) throws IOException, URISyntaxException {
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        clientSocket.getInputStream()));
        String inputLine, outputLine = null;
        boolean firstLine = true;
        String uriStr = "";
        while ((inputLine = in.readLine()) != null) {
            if(firstLine){
                uriStr = inputLine.split(" ")[1];
                firstLine = false;
            }
            System.out.println("Received: " + inputLine);
            if (!in.ready()) {
                break;
            }
        }
        URI requestURI = new URI(uriStr);
        outputLine = outputLine = ResponseBuilder.httpError(requestURI);
        String path = requestURI.getPath();
        System.out.println("======Request======:" + requestURI.getPath());

        try {
            if(requestURI.getPath().contains(".")){
                if(uriStr.contains("png") || uriStr.contains("jpg") || uriStr.contains("ico")){
                    handleImageRequest(requestURI, clientSocket.getOutputStream());
                }else{
                    outputLine = httpResponseFile(requestURI);
                }
            }else if(services.containsKey(path)){
                outputLine = callService(path, requestURI);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        out.println(outputLine);
        out.println();
        out.close();
        in.close();
        clientSocket.close();
    }

    /**
     * Generates an HTTP response for a requested file.
     * This method reads the content of the requested file from the server's filesystem,
     * constructs an HTTP response header with the appropriate content type, and returns
     * the HTTP response as a string.
     * @param requestedURI The URI of the requested file.
     * @return The HTTP response string containing the content of the requested file.
     * @throws IOException If an I/O error occurs while reading the file or constructing the response.
     */
    public static String httpResponseFile(URI requestedURI) throws IOException{
        String path = requestedURI.getPath();
        if (!path.contains(".")) {
            path = "/index.html";
        }
        Path file = Paths.get("target/classes/" + location + path);
        String extension = path.substring(path.lastIndexOf('.') + 1);
        String contentType = ResponseBuilder.getContentType(extension);
        String outputLine = ResponseBuilder.httpOkHeader(contentType);
        Charset charset = Charset.forName("UTF-8");
        BufferedReader reader = Files.newBufferedReader(file, charset);
        String line = null;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            outputLine = outputLine + line;
        }
        return  outputLine;
    }

    /**
     * Handles an image request by reading the requested image file from the server's filesystem
     * and writing it to the provided output stream along with an HTTP response header.
     * @param requestedURI The URI of the requested image file.
     * @param out The output stream to which the image content and HTTP response header will be written.
     * @throws IOException If an I/O error occurs while reading the image file or writing to the output stream.
     */
    public static void handleImageRequest(URI requestedURI, OutputStream out) throws IOException {
        String path = requestedURI.getPath();
        if(path.contains("favicon.ico")){
            path = "/img/mySpark.png";
        }
        String extension = path.substring(path.lastIndexOf('.') + 1);
        String contentType = ResponseBuilder.getContentType(extension);
        String outputLine = ResponseBuilder.httpOkHeader(contentType);
        Path file = Paths.get("target/classes/" + location + path);
        byte[] fileArray;
        fileArray = Files.readAllBytes(file);
        out.write(outputLine.getBytes());
        out.write(fileArray, 0, fileArray.length);
        out.close();
    }

    public static void  get(String path, Function svc) throws Exception {
        services.put(path,svc);
    }

    public static void setLocation(String newLocation){
        location = newLocation;
    }

    private String callService(String path, URI requestURI) throws IOException {
        String output = "";
        Function service = services.get(path);
        try {
            output = service.handle(requestURI);
            System.out.println("==== Function Response: " + output + " =====");
        }catch (IOException e){
            e.printStackTrace();
            return ResponseBuilder.httpError(requestURI);
        }
        return ResponseBuilder.httpOkServiceCall() + output;
    }



}

