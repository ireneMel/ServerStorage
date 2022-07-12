package server.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import database.Criteria;
import database.StorageDB;
import models.Group;
import models.Product;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiGetHandler implements HttpHandler {
    private final StorageDB db;
    private final ObjectMapper mapper;

    public ApiGetHandler(StorageDB db) {
        this.db = db;
        mapper = new ObjectMapper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String uri = exchange.getRequestURI().toString();
        String uriNoApi = uri.substring(uri.indexOf("api") + 4);

        //can return : price || product || group
        String identifier = uriNoApi.substring(0, uriNoApi.indexOf('/'));

        try {
            switch (identifier) {
                case "price":
                    identifier = uriNoApi.substring(uriNoApi.indexOf("price") + 6, uriNoApi.lastIndexOf('/'));
                    if ("product".equals(identifier)) {
                        String id = uriNoApi.substring(uriNoApi.lastIndexOf('/') + 1);
                        write200OK(exchange, db.getProductCost(id));
                    } else if ("group".equals(identifier)) {
                        String id = uriNoApi.substring(uriNoApi.lastIndexOf('/') + 1);
                        write200OK(exchange, db.getGroupCost(id));
                    } else {
                        String ret = "Incorrect operation";
                        exchange.sendResponseHeaders(409, ret.length());
                        exchange.getResponseBody().write(ret.getBytes());
                    }
                    break;
                case "product":
                    //examples of product uri:
                    // 1. product/id - get info about the product
                    // 2. product/all - get info about all products ()
                    // 3. product/id?key=value&key1=value... - get info using criteria

                    Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
                    if (params == null) { //product/id || product/all
                        String id = uriNoApi.substring(uriNoApi.lastIndexOf('/') + 1);
                        if ("all".equals(id)) {
                            List<Product> allProductsInfo = db.filter(Criteria.builder().build());
                            write200OK(exchange, allProductsInfo);
                        } else {
                            Product currentProduct = db.readProduct(id);
                            write200OK(exchange, currentProduct);
                        }
                    } else {
                        //product/id?key=value&key1=value...

                        String name = params.get("name");
                        String minPrice = params.get("minPrice");
                        String maxPrice = params.get("maxPrice");
                        String minAmount = params.get("minAmount");
                        String maxAmount = params.get("maxAmount");
                        String group = params.get("group");
                        String description = params.get("description");
                        String manufacturer = params.get("manufacturer");

                        Criteria.CriteriaBuilder criteriaBuilder = Criteria.builder();

                        if (name != null) criteriaBuilder.productNameQuery(name);
                        if (minPrice != null) criteriaBuilder.lowerBoundPrice(Double.parseDouble(minPrice));
                        if (maxPrice != null) criteriaBuilder.upperBoundPrice(Double.parseDouble(maxPrice));
                        if (minAmount != null) criteriaBuilder.lowerBoundAmount(Integer.parseInt(minAmount));
                        if (maxAmount != null) criteriaBuilder.upperBoundAmount(Integer.parseInt(maxAmount));
                        if (group != null) criteriaBuilder.groupNameQuery(group, false);
                        if (description != null) criteriaBuilder.descriptionQuery(description);
                        if (manufacturer != null) criteriaBuilder.manufacturerQuery(manufacturer);

                        List<Product> productToParams = db.filter(criteriaBuilder.build());

                        write200OK(exchange, productToParams);
                    }
                    break;
                case "group":
                    String id = uriNoApi.substring(uriNoApi.lastIndexOf('/'));
                    Group group = db.readGroup(id);
                    write200OK(exchange, group);
                    break;
                default:
                    String ret = "Incorrect operation";
                    exchange.sendResponseHeaders(409, ret.length());
                    exchange.getResponseBody().write(ret.getBytes());
                    break;
            }
        } catch (Exception e) {
            exchange.sendResponseHeaders(409, e.getMessage().length());
            exchange.getResponseBody().write(e.getMessage().getBytes());
        }
        exchange.close();
    }

    private void write200OK(HttpExchange exchange, Object object) throws IOException {
        exchange.sendResponseHeaders(200, 0);
        OutputStream os = exchange.getResponseBody();
        os.write(mapper.writeValueAsBytes(object));
        os.close();
    }

    private Map<String, String> queryToMap(String query) {
        if (query == null) return null;
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else result.put(entry[0], "");
        }
        return result;
    }
}
