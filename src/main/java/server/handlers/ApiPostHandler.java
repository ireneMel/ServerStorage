package server.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import database.StorageDB;
import models.Group;
import models.Product;

import java.io.IOException;
import java.io.InputStream;

public class ApiPostHandler implements HttpHandler {
    private StorageDB db;
    private ObjectMapper mapper;

    public ApiPostHandler(StorageDB db) {
        this.db = db;
        mapper = new ObjectMapper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String uri = exchange.getRequestURI().toString();
        //product or group
        String category = uri.substring(uri.indexOf("api") + 4, uri.lastIndexOf('/'));
        //name of product or group
        String nameToUpdate = uri.substring(uri.lastIndexOf('/') + 1);
        InputStream inputStream;
        if ("product".equals(category)) {
            inputStream = exchange.getRequestBody();
            try {
                Product product = mapper.readValue(inputStream.readAllBytes(), Product.class);
                db.updateProduct(nameToUpdate, product);
                exchange.sendResponseHeaders(200, 0);
            } catch (Exception e) {
                exchange.sendResponseHeaders(409, e.getMessage().length());
                exchange.getResponseBody().write(e.getMessage().getBytes());
            }
        } else if ("group".equals(category)) {
            inputStream = exchange.getRequestBody();
            try {
                Group group = mapper.readValue(inputStream.readAllBytes(), Group.class);
                db.updateGroupName(nameToUpdate, group.getGroupName());
                if (group.getDescription() != null)
                    db.updateGroupDescription(nameToUpdate, group.getDescription());

                exchange.sendResponseHeaders(200, 0);
            } catch (Exception e) {
                exchange.sendResponseHeaders(409, e.getMessage().length());
                exchange.getResponseBody().write(e.getMessage().getBytes());
            }
        } else {
            String ret = "Incorrect operation";
            exchange.sendResponseHeaders(409, ret.length());
            exchange.getResponseBody().write(ret.getBytes());
        }
        exchange.close();
    }
}
