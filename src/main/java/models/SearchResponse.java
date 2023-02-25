package models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponse {
    String companyName;
    String skuId;
    String imageLink;
    String color;
    String size;
    String title;
    String prodDescription;
    String listPrice;
}
