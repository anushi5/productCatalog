package models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequest {
    @NonNull
    String companyName;
    @NonNull
    String text;
    Optional<String> color;
    Optional<String> size;

}
