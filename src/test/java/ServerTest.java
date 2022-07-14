import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import database.Criteria;
import lombok.SneakyThrows;
import models.Group;
import models.Product;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import server.StorageServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerTest {
    private HttpClient httpClient = HttpClient.newBuilder().build();
    private static String uri = "http://localhost:8765/api/";
    private final ObjectMapper mapper = new ObjectMapper();
    private static StorageServer server;
    private ExecutorService executorService = Executors.newFixedThreadPool(5);
    Group group = new Group("test-group", "");

    private final Product[] testGroupProducts = new Product[]{
            new Product("pr1", 10.0, 10, "test-group", "", ""),
            new Product("pr2", 20.0, 320, "test-group", "", ""),
            new Product("pr3", 30.0, 30, "test-group", "", ""),
            new Product("pr4", 40.0, 40, "test-group", "", "")
    };

    @BeforeAll
    public static void start() throws IOException {
        server = new StorageServer(8765);
        server.startServer();
    }

    @SneakyThrows
    @Test
    public void putRequestAddGroup() {
        //add group

//        //check that before the test group doesn`t exist
//        Assertions.assertEquals(new LinkedList<>(), server.getDb().filterGroup(Criteria.builder()
//                .groupNameQuery("test-group", true)
//                .build()));

        addGroup(group).thenApply(HttpResponse::body).join();

        //find group in storage
        List<Group> addedGroup = server.getDb().filterGroup(Criteria.builder()
                .groupNameQuery("test-group", true)
                .build());
        Assertions.assertEquals(group, addedGroup.get(0));

        //add products
        addProduct(testGroupProducts[0]).thenApply(HttpResponse::body).join();
        addProduct(testGroupProducts[1]).thenApply(HttpResponse::body).join();
        addProduct(testGroupProducts[2]).thenApply(HttpResponse::body).join();
        addProduct(testGroupProducts[3]).thenApply(HttpResponse::body).join();

        //find group in products list
        List<Product> addedGroupInProducts = server.getDb().filter(Criteria.builder()
                .groupNameQuery("test-group", true)
                .build());
        Assertions.assertIterableEquals(Arrays.asList(testGroupProducts), addedGroupInProducts);
    }

    @Test
    public void postIncrease() throws SQLException, IOException, InterruptedException {
//        addProduct(testGroupProducts[0]).thenApply(HttpResponse::body).join();
//        addGroup(group).thenApply(HttpResponse::body).join();
//
//        int amountBefore = server.getDb().readProduct(testGroupProducts[0].getProductName()).getAmount();
//
//        executorService.execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    increaseProduct(testGroupProducts[0].getProductName(), 10).thenApply(HttpResponse::body);
//                    increaseProduct(testGroupProducts[0].getProductName(), 20).thenApply(HttpResponse::body);
//                    increaseProduct(testGroupProducts[0].getProductName(), 50).thenApply(HttpResponse::body);
//                    increaseProduct(testGroupProducts[0].getProductName(), 90).thenApply(HttpResponse::body);
//                } catch (JsonProcessingException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        });
//
//        Product updated = server.getDb().readProduct(testGroupProducts[0].getProductName());
//
//        Assertions.assertEquals(amountBefore + 180, updated.getAmount());
    }

    @Test
    public void postDecrease() throws IOException, SQLException {
        addProduct(testGroupProducts[1]).thenApply(HttpResponse::body).join();
        addGroup(group).thenApply(HttpResponse::body).join();
        int beforeDecrease = server.getDb().readProduct(testGroupProducts[1].getProductName()).getAmount();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    decreaseProduct(testGroupProducts[1].getProductName(), 30).thenApply(HttpResponse::body);
                    decreaseProduct(testGroupProducts[1].getProductName(), 10).thenApply(HttpResponse::body);
                    decreaseProduct(testGroupProducts[1].getProductName(), 20).thenApply(HttpResponse::body);
                    decreaseProduct(testGroupProducts[1].getProductName(), 50).thenApply(HttpResponse::body);
                    decreaseProduct(testGroupProducts[1].getProductName(), 90).thenApply(HttpResponse::body);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        Product updated = server.getDb().readProduct(testGroupProducts[1].getProductName());

        Assertions.assertTrue(updated.checkAmountValid());
        Assertions.assertEquals(((beforeDecrease - 190 < 0) ? 0 : beforeDecrease), (int) updated.getAmount());
    }

    public void postGroup() {

    }

    @Test
    public void postProduct() throws JsonProcessingException {
        updateProduct(testGroupProducts[2].getProductName(), new Product("newnewpr", 10, "group-test"));
    }

    @AfterAll
    public static void deleteTestGroup() throws SQLException {
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(uri + "group/test-group"))
//                .DELETE()
//                .build();
//        HttpClient httpClient = HttpClient.newBuilder().build();
//        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body).join();
//
//        //assert that group is not in the database
//        Assertions.assertEquals(new LinkedList<>(), server.getDb().filterGroup(Criteria.builder()
//                .groupNameQuery("test-group", true)
//                .build()));
//        //assert that all product from the group were deleted
//        Assertions.assertEquals(new LinkedList<>(), server.getDb().filter(Criteria.builder()
//                .groupNameQuery("test-group", true)
//                .build()));
    }

    private CompletableFuture<HttpResponse<String>> addGroup(Group group) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri + "group"))
                .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(group)))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    private CompletableFuture<HttpResponse<String>> addProduct(Product product) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri + "product"))
                .PUT(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(product)))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    private CompletableFuture<HttpResponse<String>> updateProduct(String productName, Product product) throws JsonProcessingException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri + "product/" + productName))
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(product)))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> increaseProduct(String productName, int amount) throws JsonProcessingException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri + "increase/" + productName))
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(amount)))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public CompletableFuture<HttpResponse<String>> decreaseProduct(String productName, int amount) throws JsonProcessingException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri + "decrease/" + productName))
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(amount)))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
}

