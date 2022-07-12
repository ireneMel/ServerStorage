package database;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Criteria {
    @Builder.Default
    private String productNameQuery = "";
    private double lowerBoundPrice;
    @Builder.Default
    private double upperBoundPrice = Double.POSITIVE_INFINITY;
    private int lowerBoundAmount;
    @Builder.Default
    private int upperBoundAmount = Integer.MAX_VALUE;
//    @Builder.Default
    private String groupNameQuery = "";
    @Builder.Default
    private String descriptionQuery = "";
    @Builder.Default
    private String manufacturerQuery = "";


    public static class CriteriaBuilder {
        private String groupNameQuery = "";

        public void groupNameQuery(String groupNameQuery, boolean isExact) {
            if (isExact)
                this.groupNameQuery = groupNameQuery;
            else
                this.groupNameQuery = groupNameQuery + '%';
        }

        public String getGroupNameQuery() {
            return groupNameQuery;
        }
    }
}