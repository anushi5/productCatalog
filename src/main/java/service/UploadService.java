package service;

import client.MySqlConnectionSingleton;
import client.SolrClientSingleton;
import helper.CsvHelper;
import models.UploadRequest;
import models.UploadResponse;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static constants.Constants.*;

public class UploadService {
    private MySqlConnectionSingleton mySqlConnectionSingleton = MySqlConnectionSingleton.getInstance();

    private final Connection connection = mySqlConnectionSingleton.getConnection();
    ;

    private SolrClient solrClient = SolrClientSingleton.getSolrClient();

    private CsvHelper csvHelper = new CsvHelper();

    private static final String SQL_QUERY_STRING = "INSERT INTO productData.product_table (company_name, sku_id, image, additional_image_link, style_id, class_id, color," +
            "color_code, color_family, size, size_id, department_id, dissection_code, hazmat, redline, promoted, tax_code," +
            "vendor,list_price,sale_price, sale_price_effective_date, currency, shoprunner_eligible, title,link,prod_description, start_date, featured_color, " +
            "featured_color_category, related_products,pre_order,handling_code, video,proportion, proportion_products, master_style, cannot_return, great_find, " +
            "web_exclusive, ny_deals, promo_tagline, partially_promoted, product_category, sort_order, quantity_sold, average_rating) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," +
            "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE list_price = VALUES(list_price), sale_price = VALUES(sale_price), sale_price_effective_date = VALUES(sale_price_effective_date), quantity_sold = VALUES(quantity_sold)";


    private String[] header;
    private List<SolrInputDocument> solrInputDocumentList = new ArrayList<>();
    private List<String[]> currentPreparedStatements = new ArrayList<>();
    private List<Object> failureSolrDocumentList = new ArrayList<>();
    private List<Integer> failureMysqlStatementsList = new ArrayList<>();
    String failureMessage = "";

    public UploadResponse uploadFileData(UploadRequest uploadRequest) {
        String filePath = uploadRequest.getFilePath();
        String csvFilePath = null;
        boolean isSuccessful = true;
        try {
            Optional<String> csvFilePathOptional = csvHelper.getCsvFilePath(filePath);
            if (csvFilePathOptional.isPresent()) {
                csvFilePath = csvFilePathOptional.get();
            }
        } catch (IOException e) {
            failureMessage = "File Doesn't Exist.";
            return new UploadResponse.UploadResponseBuilder(failureMessage, false).build();
        }
        if (csvFilePath == null) {
            failureMessage = "File is neither tsv nor csv, upload incorrect format";
            return new UploadResponse.UploadResponseBuilder(failureMessage, false).build();
        }
        BufferedReader bufferedReader = null;
        try {
            connection.setAutoCommit(false);
            bufferedReader = new BufferedReader(new FileReader(csvFilePath));
            header = bufferedReader.readLine().split(CSV_SPLIT_REGEX);
            String line;
            int lineCount = 0;
            PreparedStatement pstmt = connection.prepareStatement(SQL_QUERY_STRING);
            while ((line = bufferedReader.readLine()) != null) {
                populateSqlQueryAndSolrDocument(pstmt, uploadRequest.getCompanyName(), line);
                pstmt.addBatch();
                if (++lineCount % BATCH_SIZE == 0) {
                    executeBatch(pstmt, uploadRequest.getCompanyName());
                }
            }

            // Execute last batch which might be of size less than 1000.
            executeBatch(pstmt, uploadRequest.getCompanyName());
            if (!failureSolrDocumentList.isEmpty() || !failureMysqlStatementsList.isEmpty()) {
                isSuccessful = false;
            }

        } catch (IOException e) {
            failureMessage = "File is corrupt " + e.getMessage();
        } catch (SQLException e) {
            failureMessage = "Unexpected error from SQL " + e.getMessage();
        }
        String message = failureMessage;
        if (isSuccessful) {
            message = SUCCESS_MESSAGE;
        } else if (failureMessage.isEmpty()) {
            message = FAILURE_MESSAGE;
        }
        return new UploadResponse.UploadResponseBuilder(message, isSuccessful)
                .setSolrFailureList(failureSolrDocumentList)
                .setMysqlFailureList(failureMysqlStatementsList)
                .build();
    }

    private void populateSqlQueryAndSolrDocument(PreparedStatement pstmt, String companyName, String line) throws SQLException {
        String[] values = line.split(CSV_SPLIT_REGEX);
        String[] finalValues = getValues(values, header.length);
        SolrInputDocument doc = new SolrInputDocument();
        pstmt.setString(1, companyName);
        for (int i = 0; i < header.length; i++) {
            if (finalValues[i] == null || finalValues[i].isEmpty() || finalValues[i].isBlank()) {
                doc.addField(header[i], DEFAULT_DATASTORE_VALUE);
            } else {
                doc.addField(header[i], finalValues[i]);
            }
            pstmt.setString(i + 2, finalValues[i]);
        }
        doc.addField(COMPANY_NAME, companyName);
        doc.addField(ID, finalValues[0]+companyName);
        solrInputDocumentList.add(doc);
        currentPreparedStatements.add(finalValues);
    }

    private void executeBatch(PreparedStatement pstmt, String companyName) {
        boolean sqlBatchSuccess = false;
        try {
            pstmt.executeBatch();
            connection.commit();
            sqlBatchSuccess = true;
        } catch (SQLException e) {
            executeLineByLineSql(pstmt, companyName);
        }
        if (sqlBatchSuccess) {
            try {
                solrClient.add(solrInputDocumentList);
                solrClient.commit();
            } catch (SolrServerException | IOException e) {
                executeLineByLineForSolr();
            }
        }
        solrInputDocumentList.clear();
        currentPreparedStatements.clear();
    }

    private void executeLineByLineForSolr() {
        for (SolrInputDocument solrInputDocument : solrInputDocumentList) {
            try {
                solrClient.add(solrInputDocument);
                solrClient.commit();
            } catch (SolrServerException | IOException ex) {
                // Still not successful, add it to failure list.
                failureSolrDocumentList.add(solrInputDocument.getFieldValue(SKU_ID));
            }
        }
        solrInputDocumentList.clear();
    }

    private void executeLineByLineSql(PreparedStatement pstmt, String companyName) {
        SolrInputDocument doc = new SolrInputDocument();
        for (String[] finalValues : currentPreparedStatements) {
            for (int i = 0; i < header.length; i++) {
                if (finalValues[i] == null || finalValues[i].isEmpty() || finalValues[i].isBlank()) {
                    doc.addField(header[i], DEFAULT_DATASTORE_VALUE);
                } else {
                    doc.addField(header[i], finalValues[i]);
                }
                try {
                    pstmt.setString(i + 2, finalValues[i]);
                } catch (SQLException e) {
                    failureMysqlStatementsList.add(Integer.parseInt(finalValues[0]));
                }
            }
            doc.addField(COMPANY_NAME, finalValues[0]+companyName);
            doc.addField(ID, finalValues[0]+companyName);
            try {
                pstmt.execute();
                connection.commit();
            } catch (SQLException e) {
                failureMysqlStatementsList.add(Integer.parseInt(finalValues[0]));
            }
            try {
                solrClient.add(doc);
                solrClient.commit();
            } catch (SolrServerException | IOException e) {
                failureSolrDocumentList.add(Integer.parseInt(finalValues[0]));
            }
        }
        currentPreparedStatements.clear();
        solrInputDocumentList.clear();
    }

    private String[] getValues(String[] value, int size) {
        String[] values = new String[size];
        int i = 0;
        for (String v : value) {
            if (v.isBlank() || v.isEmpty()) {
                values[i++] = null;
            } else {
                String val = v.replaceAll("(?<!,)\"(?!\\s*,)", "");
                values[i++] = val;
            }
        }
        if (i < size) {
            values[i++] = null;
        }
        return values;
    }
}