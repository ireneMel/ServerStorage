package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import database.StorageDB;

import java.io.IOException;

public class ApiDeleteHandler implements HttpHandler {
    private final StorageDB db;

    public ApiDeleteHandler(StorageDB db) {
        this.db = db;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String uri = exchange.getRequestURI().toString();
        //product or group
        String category = uri.substring(uri.indexOf("api") + 4, uri.lastIndexOf('/'));
        //name of product or group
        String nameToDelete = uri.substring(uri.lastIndexOf('/') + 1);

        if ("product".equals(category)) {
            try {
                if ("all".equals(nameToDelete)) {
                    db.deleteAllProducts();
                } else
                    db.deleteProduct(nameToDelete);
                exchange.sendResponseHeaders(200, 0);
            } catch (Exception e) {
                exchange.sendResponseHeaders(409, e.getMessage().length());
                exchange.getResponseBody().write(e.getMessage().getBytes());
            }
        } else if ("group".equals(category)) {
            try {
                if ("all".equals(nameToDelete)) {
                    db.deleteAllGroups();
                } else
                    db.deleteGroup(nameToDelete);
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
