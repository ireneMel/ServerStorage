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
    private Double price;
    private Integer amount;
    private String groupName;
    private String description;
    private String manufacturer;

    public Product(String productName, double price, String groupName) {
        this.productName = productName;
        this.price = price;
        this.groupName = groupName;
    }

    public boolean isAmountValid(){
        return amount != null && amount >= 0;
    }

    public boolean isPriceValid(){
        return price != null && price >= 0;
    }

    public boolean isGroupValid(){
        return groupName != null && !groupName.isBlank();
    }
    public boolean isNameValid(){
        return productName != null && !productName.isBlank();
    }
}

