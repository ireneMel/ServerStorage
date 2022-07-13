package database_test;

import database.StorageDB;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class DecreaseIncreaseTest {
    @Test
    public void decreaseProductAmount() throws SQLException {
        StorageDB db = new StorageDB();
        db.initialization("OurDb");
        db.decreaseProductAmount("prdoct14", 13);
        db.increaseProductAmount("prdoct14",10);
        db.increaseProductAmount("product15",10);
        db.increaseProductAmount("product111",10);
        db.increaseProductAmount("veg1",10);

        db.decreaseProductAmount("product15", 13);
        db.decreaseProductAmount("product15", 6);
        db.decreaseProductAmount("veg1", 90);
    }
}
