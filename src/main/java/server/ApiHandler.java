package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import database.StorageDB;
import server.handlers.ApiDeleteHandler;
import server.handlers.ApiGetHandler;
import server.handlers.ApiPostHandler;
import server.handlers.ApiPutHandler;

import java.io.IOException;

public class ApiHandler implements HttpHandler {
    private ApiPutHandler apiPutHandler;
    private ApiPostHandler apiPostHandler;
    private ApiDeleteHandler apiDeleteHandler;
    private ApiGetHandler apiGetHandler;

    public ApiHandler(StorageDB db) {
        apiPutHandler = new ApiPutHandler(db);
        apiPostHandler = new ApiPostHandler(db);
        apiDeleteHandler = new ApiDeleteHandler(db);
        apiGetHandler = new ApiGetHandler(db);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("PUT".equals(exchange.getRequestMethod())) {
            apiPutHandler.handle(exchange);
        } else if ("POST".equals(exchange.getRequestMethod())) {
            apiPostHandler.handle(exchange);
        } else if ("DELETE".equals(exchange.getRequestMethod())) {
            apiDeleteHandler.handle(exchange);
        } else if ("GET".equals(exchange.getRequestMethod())) {
            apiGetHandler.handle(exchange);
        }
    }
}
