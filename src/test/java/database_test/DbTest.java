package database_test;

import database.Criteria;
import database.StorageDB;
import lombok.SneakyThrows;
import models.Group;
import models.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class DbTest {
    private static StorageDB storageDB = new StorageDB();
    private List<Product> expectedProducts;
    private List<Group> expectedGroups;
    String[] groupNames = new String[]{"Meat", "Vegetables", "Fruits", "Grains", "Berry"};
    Group[] allGroups = new Group[]{
            new Group("Meat", ""),
            new Group("Vegetables", ""),
            new Group("Fruits", ""),
            new Group("Grains", ""),
            new Group("Berry", "")
    };
    Product[] allProducts = new Product[]{
            new Product("Watermelon", 0, 0, groupNames[4], "", ""),
            new Product("Melon", 0, 0, groupNames[4], "", ""),
            new Product("Beef", 0, 0, groupNames[0], "", ""),
            new Product("Cucumber", 0, 0, groupNames[1], "", ""),
            new Product("Apple", 0, 0, groupNames[2], "", "")
    };

    @BeforeEach
    public void init() throws SQLException {
        storageDB.initialization("OurStorage");
        storageDB.deleteAllGroups();

        expectedProducts = new LinkedList<>(List.of(allProducts));
        expectedGroups = new LinkedList<>(List.of(allGroups));

        storageDB.createGroup(allGroups[0]);
        storageDB.createGroup(allGroups[1]);
        storageDB.createGroup(allGroups[2]);
        storageDB.createGroup(allGroups[3]);
        storageDB.createGroup(allGroups[4]);

        storageDB.createProduct(allProducts[0]);
        storageDB.createProduct(allProducts[1]);
        storageDB.createProduct(allProducts[2]);
        storageDB.createProduct(allProducts[3]);
        storageDB.createProduct(allProducts[4]);
    }

    @Test
    public void creation() throws SQLException {
        Assertions.assertIterableEquals(expectedProducts, storageDB.filter(Criteria.builder().build()));
        Assertions.assertIterableEquals(expectedGroups, storageDB.filterGroup(Criteria.builder().build()));
    }

    @Test
    public void updateProductName() throws SQLException {
        expectedProducts.get(0).setProductName("Lime");
        expectedProducts.get(1).setProductName("Lemon");
        expectedProducts.get(2).setProductName("Pork");
        storageDB.updateProductName("Watermelon", "Lime");
        storageDB.updateProductName("Melon", "Lemon");
        storageDB.updateProductName("Beef", "Pork");
        Assertions.assertIterableEquals(expectedProducts, storageDB.filter(Criteria.builder().build()), "Wrong name");
    }

    @Test
    public void updateProductAmount() throws SQLException {
        expectedProducts.get(3).setAmount(100);
        expectedProducts.get(4).setAmount(5);
        expectedProducts.get(2).setAmount(71);
        storageDB.updateProductAmount(expectedProducts.get(3).getProductName(), 100);
        storageDB.updateProductAmount(expectedProducts.get(4).getProductName(), 5);
        storageDB.updateProductAmount(expectedProducts.get(2).getProductName(), 71);
        Assertions.assertIterableEquals(expectedProducts, storageDB.filter(Criteria.builder().build()), "Wrong amount");
    }

    @Test
    public void updateProductPrice() throws SQLException {
        expectedProducts.get(3).setPrice(11.3);
        expectedProducts.get(4).setPrice(45.1);
        expectedProducts.get(2).setPrice(190.6);
        storageDB.updateProductPrice(expectedProducts.get(3).getProductName(), 11.3);
        storageDB.updateProductPrice(expectedProducts.get(4).getProductName(), 45.1);
        storageDB.updateProductPrice(expectedProducts.get(2).getProductName(), 190.6);

//        Assertions.assertThrows(new SQLException(), () -> {});

        Assertions.assertIterableEquals(expectedProducts, storageDB.filter(Criteria.builder().build()));

    }

    @Test
    public void updateProductDescription() throws SQLException {
        String description = "description1";
        expectedProducts.get(1).setDescription(description);
        expectedProducts.get(2).setDescription(description + "2");
        storageDB.updateProductDescription(expectedProducts.get(1).getProductName(), description);
        storageDB.updateProductDescription(expectedProducts.get(2).getProductName(), description + "2");
        Assertions.assertIterableEquals(expectedProducts, storageDB.filter(Criteria.builder().build()));
    }

    @Test
    public void updateProductManufacturer() throws SQLException {
        String manufacturer = "manufacturer1";
        expectedProducts.get(1).setManufacturer(manufacturer);
        expectedProducts.get(2).setManufacturer(manufacturer + "2");
        storageDB.updateProductManufacturer(expectedProducts.get(1).getProductName(), manufacturer);
        storageDB.updateProductManufacturer(expectedProducts.get(2).getProductName(), manufacturer + "2");
        Assertions.assertIterableEquals(expectedProducts, storageDB.filter(Criteria.builder().build()));
    }

    @Test
    public void updateAllFieldsOfProductAtOnce() throws SQLException {
        storageDB.updateProduct(expectedProducts.get(0).getProductName(),
                new Product(
                        "NewName",
                        666.66,
                        100,
                        "Berry",
                        "New product description",
                        "Zhytomyr"
                ));

        expectedProducts.get(0).setProductName("NewName");
        expectedProducts.get(0).setAmount(100);
        expectedProducts.get(0).setPrice(666.66);
        expectedProducts.get(0).setDescription("New product description");
        expectedProducts.get(0).setManufacturer("Zhytomyr");

        Assertions.assertIterableEquals(expectedProducts, storageDB.filter(Criteria.builder().build()));
    }

    @Test
    public void updateGroup() throws SQLException {
        //check in products table
        storageDB.updateGroupName(groupNames[4], "Vegan");
        expectedProducts.get(0).setGroupName("Vegan");
        expectedProducts.get(1).setGroupName("Vegan");

        expectedGroups.get(4).setGroupName("Vegan");

        Assertions.assertIterableEquals(expectedProducts, storageDB.filter(Criteria.builder().build()));

        //check in groups table
        expectedGroups.get(0).setDescription("Fresh meat for the customers");
        storageDB.updateGroupDescription("Meat", "Fresh meat for the customers");
        Assertions.assertEquals(expectedGroups, storageDB.filterGroup(Criteria.builder().build()));
    }

    @Test
    public void deleteProduct() throws SQLException {
        expectedProducts.remove(3);
        expectedProducts.remove(1);
        storageDB.deleteProduct("Cucumber");
        storageDB.deleteProduct("Cucumber");
        storageDB.deleteProduct("Melon");
        Assertions.assertIterableEquals(expectedProducts, storageDB.filter(Criteria.builder().build()));
    }

    @Test
    public void deleteGroup() throws SQLException {
        storageDB.deleteGroup(groupNames[4]);
        storageDB.deleteGroup(groupNames[1]);

        expectedProducts.remove(0);
        expectedProducts.remove(0);
        expectedProducts.remove(1);

        expectedGroups.remove(4);
        expectedGroups.remove(1);

        Assertions.assertIterableEquals(expectedProducts, storageDB.filter(Criteria.builder().build()));
        Assertions.assertIterableEquals(expectedGroups, storageDB.filterGroup(Criteria.builder().build()));
    }

    @Test
    public void deleteAllGroups() throws SQLException {
        storageDB.deleteAllGroups();
        Assertions.assertIterableEquals(new LinkedList<>(), storageDB.filter(Criteria.builder().build()));
        Assertions.assertIterableEquals(new LinkedList<>(), storageDB.filterGroup(Criteria.builder().build()));
    }

    @Test
    public void deleteAllProducts() throws SQLException {
        storageDB.deleteAllProducts();
        Assertions.assertIterableEquals(new LinkedList<>(), storageDB.filter(Criteria.builder().build()));
        Assertions.assertIterableEquals(expectedGroups, storageDB.filterGroup(Criteria.builder().build()));
    }

    @Test
    public void readProduct() throws SQLException {
        Assertions.assertEquals(allProducts[0], storageDB.readProduct("Watermelon"));
        Assertions.assertNotEquals(allProducts[1], storageDB.readProduct("Watermelon"));
        Assertions.assertEquals(allProducts[2], storageDB.readProduct("Beef"));
    }

    @Test
    public void getProductCost() throws SQLException {
        Assertions.assertEquals(0, storageDB.getProductCost(allProducts[0].getProductName()));
        storageDB.updateProductPrice(expectedProducts.get(1).getProductName(), 12.5);
        storageDB.updateProductAmount(expectedProducts.get(1).getProductName(), 120);
        Assertions.assertEquals(12.5 * 120, storageDB.getProductCost(expectedProducts.get(1).getProductName()));
        Assertions.assertNotEquals(12.5 * -120, storageDB.getProductCost(expectedProducts.get(1).getProductName()));
        Assertions.assertNotEquals(0, storageDB.getProductCost(expectedProducts.get(1).getProductName()));
    }

    @Test
    public void getGroupCost() throws SQLException {
        Assertions.assertEquals(0, storageDB.getGroupCost(allGroups[0].getGroupName()));
        storageDB.updateProductPrice(expectedProducts.get(1).getProductName(), 12.5);
        storageDB.updateProductAmount(expectedProducts.get(1).getProductName(), 120);
        Assertions.assertEquals(12.5 * 120, storageDB.getGroupCost(expectedProducts.get(1).getGroupName()));
        Assertions.assertNotEquals(12.5 * -120, storageDB.getGroupCost(expectedProducts.get(1).getGroupName()));
        Assertions.assertNotEquals(0, storageDB.getGroupCost(expectedProducts.get(1).getGroupName()));
    }


    @Test
    public void listByCriteriaProductName() throws SQLException {
        storageDB.createGroup("Diary");

        storageDB.createProduct("Beet", 12.5, 150, "Vegetables", "", "");
        storageDB.createProduct("Bean", 5.5, 300, "Vegetables", "", "");
        storageDB.createProduct("Blackberry", 172.0, 100, "Berry", "", "");
        storageDB.createProduct("Milk", 25.6, 110, "Diary", "", "");

        expectedProducts.clear();
        expectedProducts.add(new Product("Beef", 0, 0, groupNames[0], "", ""));
        expectedProducts.add(new Product("Beet", 12.5, 150, groupNames[1], "", ""));
        expectedProducts.add(new Product("Bean", 5.5, 300, groupNames[1], "", ""));
        expectedProducts.add(new Product("Blackberry", 172.0, 100, groupNames[4], "", ""));

        Assertions.assertIterableEquals(expectedProducts, storageDB.filter(Criteria.builder()
                .productNameQuery("B")
                .build()));
    }
//
//    @Test
//    public void listByCriteriaGroupName() {
//        storageDB.createProduct("Beet", "Vegetables", 12.5, 150);
//        storageDB.createProduct("Bean", "Vegetables", 5.5, 300);
//        storageDB.createProduct("Blackberry", "Berry", 172.0, 100);
//
//        expectedProducts.clear();
//        expectedProducts.add(new Product("Cucumber", groupNames[1], 0, 0));
//        expectedProducts.add(new Product("Beet", groupNames[1], 150, 12.5));
//        expectedProducts.add(new Product("Bean", groupNames[1], 300, 5.5));
//
//        Assertions.assertIterableEquals(expectedProducts, storageDB.filter(Criteria.builder()
//                .groupNameQuery("Vegetables")
//                .build()));
//    }
//
//    @Test
//    public void listByCriteriaPrice() {
//        storageDB.createProduct("Beet", "Vegetables", 12.5, 150);
//        storageDB.createProduct("Bean", "Vegetables", 5.5, 300);
//        storageDB.createProduct("Blackberry", "Berry", 172.0, 100);
//        storageDB.createProduct("Test1", "Berry", 49.0, 100);
//        storageDB.createProduct("Test2", "Berry", 17.0, 100);
//
//        expectedProducts.clear();
//        expectedProducts.add(new Product("Beet", groupNames[1], 150, 12.5));
//        expectedProducts.add(new Product("Test1", groupNames[4], 100, 49.0));
//        expectedProducts.add(new Product("Test2", groupNames[4], 100, 17.0));
//
//        Assertions.assertIterableEquals(expectedProducts, storageDB.filter(Criteria.builder()
//                .lowerBoundPrice(12.5)
//                .upperBoundPrice(50.0)
//                .build()));
//    }
//
//    @Test
//    public void listByCriteriaAmount() {
//        storageDB.createProduct("Beet", "Vegetables", 12.5, 150);
//        storageDB.createProduct("Bean", "Vegetables", 5.5, 300);
//        storageDB.createProduct("Blackberry", "Berry", 172.0, 100);
//        storageDB.createProduct("Test1", "Berry", 49.0, 70);
//        storageDB.createProduct("Test2", "Berry", 17.0, 80);
//
//        expectedProducts.clear();
//        expectedProducts.add(new Product("Beet", groupNames[1], 150, 12.5));
//        expectedProducts.add(new Product("Blackberry", groupNames[4], 100, 172.0));
//        expectedProducts.add(new Product("Test1", groupNames[4], 70, 49.0));
//        expectedProducts.add(new Product("Test2", groupNames[4], 80, 17.0));
//
//        Assertions.assertIterableEquals(expectedProducts, storageDB.filter(Criteria.builder()
//                .lowerBoundAmount(70)
//                .upperBoundAmount(150)
//                .build()));
//    }
//
//    @Test
//    public void listByCriteriaCompound() {
//        storageDB.createProduct("Test1", "Berry1", 49.0, 70);
//        storageDB.createProduct("Test2", "Berry", 15.0, 80);
//        storageDB.createProduct("Test3", "Berry", 7.0, 90);
//        storageDB.createProduct("Test4", "Berry", 127.0, 300);
//        storageDB.createProduct("Test5", "Berry", 17.0, 90);
//
//        expectedProducts.clear();
//        expectedProducts.add(new Product("Test2", groupNames[4], 80, 15.0));
//        expectedProducts.add(new Product("Test5", groupNames[4], 90, 17.0));
//
//        Assertions.assertIterableEquals(expectedProducts, storageDB.filter(Criteria.builder()
//                .productNameQuery("Test")
//                .groupNameQuery("Berry")
//                .lowerBoundAmount(80)
//                .upperBoundAmount(150)
//                .lowerBoundPrice(15.0)
//                .build()));
//    }

    @SneakyThrows
    @AfterEach
    public void deleteFile() {
//        storageDB.deleteAllGroups();
        storageDB.closeConnection();
        expectedProducts.clear();
    }

}
