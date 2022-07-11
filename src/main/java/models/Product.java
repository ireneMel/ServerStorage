package models;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Product {
    private String productName;
    private double price;
    private int amount;
    private String groupName;
    private String description;
    private String manufacturer;

    public Product(String productName, double price, String groupName) {
        this.productName = productName;
        this.price = price;
        this.groupName = groupName;
    }
}

