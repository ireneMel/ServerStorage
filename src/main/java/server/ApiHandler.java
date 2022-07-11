package server;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import database.StorageDB;

import java.io.IOException;

public class ApiHandler implements HttpHandler {
    private StorageDB db;
    private ApiPutHandler apiPutHandler;
    public ApiHandler(StorageDB db) {
        this.db = db;
        apiPutHandler = new ApiPutHandler(db);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("PUT".equals(exchange.getRequestMethod()))
            apiPutHandler.handle(exchange);
    }
}
