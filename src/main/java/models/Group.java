package models;

import lombok.*;

import java.util.List;
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Group {
    private String groupName;
    private String description = "";
    private List<Product> groupProducts;

    public Group(String groupName, String description) {
        this.groupName = groupName;
        this.description = description;
    }
}