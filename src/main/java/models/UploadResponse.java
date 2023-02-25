package models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadResponse {
    private boolean isSuccessful;
    private String message;

    private List<Object> solrFailureList = new ArrayList<>();
    private List<Integer> mysqlFailureList = new ArrayList<>();

    private UploadResponse(UploadResponseBuilder builder){
        this.message = builder.message;
        this.isSuccessful = builder.isSuccessful;
        this.solrFailureList = builder.solrFailureList;
        this.mysqlFailureList = builder.mysqlFailureList;
    }

    public static class UploadResponseBuilder {
        private boolean isSuccessful;
        private String message;

        private List<Object> solrFailureList = new ArrayList<>();
        private List<Integer> mysqlFailureList = new ArrayList<>();
        public UploadResponseBuilder(String message, boolean isSuccessful) {
            this.message = message;
            this.isSuccessful = isSuccessful;
        }

        public UploadResponseBuilder setSolrFailureList(List<Object> solrFailureList) {
            this.solrFailureList = solrFailureList;
            return this;
        }

        public UploadResponseBuilder setMysqlFailureList(List<Integer> mysqlFailureList) {
            this.mysqlFailureList = mysqlFailureList;
            return this;
        }

        public UploadResponse build() {
            return new UploadResponse(this);
        }
    }
}
