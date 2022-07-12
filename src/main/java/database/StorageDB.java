package database;

import models.Group;
import models.Product;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/*
Create
Read
Update
Delete
List by criteria
 */
public class StorageDB {
    private Connection connection;

    public void closeConnection() throws SQLException {
        connection.close();
    }

    public void initialization(String name) {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + name);
            PreparedStatement st;

            st = connection.prepareStatement("CREATE TABLE IF NOT EXISTS 'products' " +
                    "('productName' text PRIMARY KEY, " +
                    "'productPrice' double, " +
                    "'productAmount' int, " +
                    "'productGroup' text, " +
                    "'productDescription' text, " +
                    "'productManufacturer' text)");
            st.executeUpdate();

            st = connection.prepareStatement("CREATE TABLE IF NOT EXISTS 'groups' " +
                    "('groupName' text PRIMARY KEY, " +
                    "'groupDescription' text)");
            st.executeUpdate();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Не знайшли драйвер JDBC");
        } catch (SQLException e) {
            throw new RuntimeException("Неправильний SQL запит");
        }
    }

    public void createGroup(String groupName, String groupDescription) throws SQLException {
        if (groupName == null || groupName.isBlank()) throw new RuntimeException("Group name must be not empty");
        PreparedStatement st = connection.prepareStatement("INSERT INTO groups VALUES (?, ?)");
        st.setString(1, groupName);
        st.setString(2, groupDescription);
        st.executeUpdate();
        st.close();
    }

    public void createGroup(String groupName) throws SQLException {
        createGroup(groupName, null);
    }

    public void deleteAllGroups() throws SQLException {
        PreparedStatement st = connection.prepareStatement("DELETE FROM groups");
        st.execute();
        st.close();
        deleteAllProducts();
    }

    public void deleteGroup(String groupName) throws SQLException {
        deleteProductsInGroup(groupName);
        PreparedStatement st = connection.prepareStatement("DELETE FROM groups WHERE groupName=?");
        st.setString(1, groupName);
        st.execute();
        st.close();
    }

    public Double getAllGroupCost(String groupName) throws SQLException {
        PreparedStatement st = connection.prepareStatement("SELECT productPrice, productAmount FROM products WHERE productGroup=?");
        st.setString(1, groupName);
        ResultSet res = st.executeQuery();
        double sum = 0;
        while (res.next()) {
            double price = res.getDouble(1);
            int amount = res.getInt(2);
            sum += price * amount;
        }
        res.close();
        st.close();
        return sum;
    }

    private void update(PreparedStatement st, Consumer<PreparedStatement> setter) throws SQLException {
        final boolean oldAutoCommit = st.getConnection().getAutoCommit();
        st.getConnection().setAutoCommit(false);
        try {
            setter.accept(st);
            st.execute();
            st.close();
        } catch (Exception e) {
            st.getConnection().rollback();
        } finally {
            st.getConnection().commit();
            st.getConnection().setAutoCommit(oldAutoCommit);
        }
    }

    public void updateGroupName(String groupName, String newName) throws SQLException {
        if (newName == null || newName.isBlank()) throw new RuntimeException("Group name must be not empty");
        PreparedStatement st = connection.prepareStatement("UPDATE groups SET groupName=? WHERE groupName=?");
        update(st, (statement -> {
            try {
                statement.setString(1, newName);
                statement.setString(2, groupName);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }));
        updateProductsGroupName(groupName, newName);
    }

    public void updateGroupDescription(String groupName, String newDescription) throws SQLException {
        PreparedStatement st = connection.prepareStatement("UPDATE groups SET groupDescription=? WHERE groupName=?");
        update(st, (statement -> {
            try {
                statement.setString(1, newDescription);
                statement.setString(2, groupName);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    public void updateGroup(String groupName, Group newGroup) throws SQLException {
        if (newGroup.getGroupName() == null || newGroup.getGroupName().isBlank()) throw new RuntimeException("Group name must be not empty");
        PreparedStatement st = connection.prepareStatement("UPDATE groups SET groupName=?, groupDescription=? WHERE groupName=?");
        update(st, (statement -> {
            try {
                statement.setString(1, newGroup.getGroupName());
                statement.setString(2, newGroup.getDescription());
                statement.setString(3, groupName);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    public boolean isGroupExistent(String groupName) throws SQLException {
        PreparedStatement st = connection.prepareStatement("SELECT EXISTS(SELECT * FROM groups WHERE groupName=?)");
        st.setString(1, groupName);
        return st.executeQuery().getBoolean(1);
    }

    public void createProduct(String productName, double price, int amount, String groupName, String productDescription, String productManufacturer) throws SQLException {
        if (productName == null || productName.isBlank()) throw new RuntimeException("Product name must be not empty");
        if (price < 0) throw new RuntimeException("Price must be above zero");
        if (amount < 0) throw new RuntimeException("Amount must be above zero");
        if (!isGroupExistent(groupName)) throw new RuntimeException("Group does not exist");

        PreparedStatement st = connection.prepareStatement("INSERT INTO products VALUES (?, ?, ?, ?, ?, ?)");
        st.setString(1, productName);
        st.setDouble(2, price);
        st.setInt(3, amount);
        st.setString(4, groupName);
        st.setString(5, productDescription);
        st.setString(6, productManufacturer);
        st.executeUpdate();
        st.close();
    }

    public void createProduct(Product product) throws SQLException {
        createProduct(product.getProductName(), product.getPrice(), product.getAmount(), product.getGroupName(), product.getDescription(), product.getManufacturer());
    }
    public void createProduct(String productName, double price, int amount, String groupName) throws SQLException {
        createProduct(productName, price, amount, groupName, "", "");
    }

    public void createProduct(String productName, double price, String groupName) throws SQLException {
        createProduct(productName, price, 0, groupName);
    }

    public void deleteAllProducts() throws SQLException {
        PreparedStatement st = connection.prepareStatement("DELETE FROM products");
        st.execute();
        st.close();
    }

    private void deleteProductsInGroup(String groupName) throws SQLException {
        if (!isGroupExistent(groupName)) throw new RuntimeException("Group does not exist");
        PreparedStatement st = connection.prepareStatement("DELETE FROM products WHERE productGroup=?");
        st.setString(1, groupName);
        st.execute();
        st.close();
    }

    public void deleteProduct(String productName) throws SQLException {
        PreparedStatement st = connection.prepareStatement("DELETE FROM products WHERE productName=?");
        st.setString(1, productName);
        st.execute();
        st.close();
    }

    private Product getProduct(ResultSet resultSet) throws SQLException {
        return new Product(
                resultSet.getString("productName"),
                resultSet.getDouble("productPrice"),
                resultSet.getInt("productAmount"),
                resultSet.getString("productGroup"),
                resultSet.getString("productDescription"),
                resultSet.getString("productManufacturer")
        );
    }

    public Product readProduct(String productName) throws SQLException {
        Product product = null;
        PreparedStatement st = connection.prepareStatement("SELECT * FROM products WHERE productName=?");
        st.setString(1, productName);
        ResultSet res = st.executeQuery();
        product = getProduct(res);
        res.close();
        st.close();
        return product;
    }

    public Double getAllProductCost(String productName) throws SQLException {
        PreparedStatement st = connection.prepareStatement("SELECT productPrice, productAmount FROM products WHERE productName=?");
        st.setString(1, productName);
        ResultSet res = st.executeQuery();
        double price = res.getDouble(1);
        int amount = res.getInt(2);
        res.close();
        st.close();
        return price * amount;
    }

    public void updateProductName(String productName, String newProductName) throws SQLException {
        if (newProductName == null || newProductName.isBlank()) throw new RuntimeException("Product name must be not empty");
        PreparedStatement st = connection.prepareStatement("UPDATE products SET productName=? WHERE productName=?");
        update(st, (statement -> {
            try {
                statement.setString(1, newProductName);
                statement.setString(2, productName);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }));
    }

    public void updateProductPrice(String productName, double newPrice) throws SQLException {
        if (newPrice < 0) throw new RuntimeException("Price must be above zero");
        PreparedStatement st = connection.prepareStatement("UPDATE products SET productPrice=? WHERE productName=?");
        update(st, (statement -> {
            try {
                statement.setDouble(1, newPrice);
                statement.setString(2, productName);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }));
    }

    public void updateProductAmount(String productName, int newAmount) throws SQLException {
        if (newAmount < 0) throw new RuntimeException("Amount must be above zero");
        PreparedStatement st = connection.prepareStatement("UPDATE products SET productAmount=? WHERE productName=?");
        update(st, (statement -> {
            try {
                statement.setInt(1, newAmount);
                statement.setString(2, productName);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

        }));
    }

    public void updateProductGroup(String productName, String newGroup) throws SQLException {
        if (!isGroupExistent(newGroup)) throw new RuntimeException("Group does not exist");

        PreparedStatement st = connection.prepareStatement("UPDATE products SET productGroup=? WHERE productName=?");
        update(st, (statement -> {
            try {
                statement.setString(1, newGroup);
                statement.setString(2, productName);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }));
    }

    public void updateProductDescription(String productName, String newDescription) throws SQLException {
        PreparedStatement st = connection.prepareStatement("UPDATE products SET productDescricpion=? WHERE productName=?");
        update(st, (statement -> {
            try {
                statement.setString(1, newDescription);
                statement.setString(2, productName);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }));
    }

    public void updateProductManufacturer(String productName, String newManufacturer) throws SQLException {
        PreparedStatement st = connection.prepareStatement("UPDATE products SET productManufacturer=? WHERE productName=?");
        update(st, (statement -> {
            try {
                statement.setString(1, newManufacturer);
                statement.setString(2, productName);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }));
    }

    private void updateProductsGroupName(String groupName, String newGroupName) throws SQLException {
        if (!isGroupExistent(newGroupName)) throw new RuntimeException("Group does not exist");

        PreparedStatement st = connection.prepareStatement("UPDATE products SET productGroup=? WHERE productGroup=?");
        update(st, (statement -> {
            try {
                statement.setString(1, newGroupName);
                statement.setString(2, groupName);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }));

    }

    public void updateProduct(String productName, Product newProduct) throws SQLException {
        if (newProduct.getProductName() == null || newProduct.getProductName().isBlank()) throw new RuntimeException("Product name must be not empty");
        if (newProduct.getPrice() < 0) throw new RuntimeException("Price must be above zero");
        if (newProduct.getAmount() < 0) throw new RuntimeException("Amount must be above zero");
        if (!isGroupExistent(newProduct.getGroupName())) throw new RuntimeException("Group does not exist");

        PreparedStatement st = connection.prepareStatement("UPDATE products " +
                "SET productName=?, " +
                "productPrice=?, " +
                "productAmount=?, " +
                "productGroup=?, " +
                "productDescription=?, " +
                "productManufacturer=? " +
                "WHERE productName=?");
        update(st, (statement -> {
            try {
                statement.setString(1, newProduct.getProductName());
                statement.setDouble(2, newProduct.getPrice());
                statement.setInt(3, newProduct.getAmount());
                statement.setString(4, newProduct.getGroupName());
                statement.setString(5, newProduct.getDescription());
                statement.setString(6, newProduct.getManufacturer());
                statement.setString(7, productName);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }));

    }

    public List<Product> filter(Criteria criteria) throws SQLException {

        LinkedList<Product> productList = new LinkedList<>();

        PreparedStatement st = connection.prepareStatement("SELECT * FROM products WHERE" +
                " (productName LIKE ?) AND" +
                " (productPrice BETWEEN ? AND ?) AND " +
                " (productAmount BETWEEN ? AND ?) AND " +
                " (productGroup LIKE ?) AND " +
                " (productDescription LIKE ?) AND " +
                " (productManufacturer LIKE ?) "
        );

        st.setString(1, criteria.getProductNameQuery() + "%");
        st.setDouble(2, criteria.getLowerBoundPrice());
        st.setDouble(3, criteria.getUpperBoundPrice());
        st.setInt(4, criteria.getLowerBoundAmount());
        st.setInt(5, criteria.getUpperBoundAmount());
        st.setString(6, criteria.getGroupNameQuery());
        st.setString(7, criteria.getDescriptionQuery() + "%");
        st.setString(8, criteria.getManufacturerQuery() + "%");

        ResultSet res = st.executeQuery();

        while (res.next()) {
            productList.add(getProduct(res));
        }

        st.execute();
        res.close();
        st.close();
        return productList;
    }
}
