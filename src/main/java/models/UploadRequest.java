package models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadRequest {
    @NonNull
    String filePath;
    @NonNull
    String companyName;

}
