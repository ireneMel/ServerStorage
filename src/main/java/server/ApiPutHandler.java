package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import database.StorageDB;
import models.Group;
import models.Product;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class ApiPutHandler implements HttpHandler {
    StorageDB db;
    ObjectMapper mapper;

    public ApiPutHandler(StorageDB db) {
        this.db = db;
        mapper = new ObjectMapper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String uri = exchange.getRequestURI().toString();
        String id = uri.substring(uri.lastIndexOf('/') + 1);
        InputStream inputStream;
        if("product".equals(id)){
            inputStream = exchange.getRequestBody();
            try {
                Product product = mapper.readValue(inputStream.readAllBytes(), Product.class);
                db.createProduct(product);
                exchange.sendResponseHeaders(200,0);
            } catch (Exception e) {
                exchange.sendResponseHeaders(409,e.getMessage().length());
                exchange.getResponseBody().write(e.getMessage().getBytes());
            }
        } else if("group".equals(id)){
            inputStream = exchange.getRequestBody();
            try {
                Group group = mapper.readValue(inputStream.readAllBytes(), Group.class);
                db.createGroup(group.getGroupName(), group.getDescription());
                exchange.sendResponseHeaders(200,0);
            } catch (Exception e) {
                exchange.sendResponseHeaders(409,e.getMessage().length());
                exchange.getResponseBody().write(e.getMessage().getBytes());
            }
        } else {
            String ret = "Incorrect operation";
            exchange.sendResponseHeaders(409,ret.length());
            exchange.getResponseBody().write(ret.getBytes());
        }
        exchange.close();
    }
}
