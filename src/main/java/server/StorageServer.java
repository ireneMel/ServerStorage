package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import database.StorageDB;
import models.Group;

import java.io.IOException;
import java.net.InetSocketAddress;

public class StorageServer {
    private HttpServer server;
    private StorageDB db;
    StorageServer(int port) throws IOException {
        db = new StorageDB();
        db.initialization("OurDb");
        server = HttpServer.create(new InetSocketAddress(port), 0);
        startServer();
    }

    private void startServer(){
        server.createContext("/api",new ApiHandler(db));

        server.setExecutor(null);
        server.start();
    }

    public static void main(String[] args) throws IOException {
        new StorageServer(8765).startServer();
    }
}
