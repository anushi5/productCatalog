package service;

import client.SolrClientSingleton;
import models.DocumentNoResponse;
import models.SearchRequest;
import models.SearchResponse;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.solr.client.solrj.*;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static constants.Constants.*;

public class SearchService {

    SolrClient solrClient = SolrClientSingleton.getSolrClient();

    public List<SearchResponse> getSearchResult(SearchRequest searchRequest) {
        SolrQuery query = new SolrQuery();
        StringBuilder textRegex = getRegexBuilder(searchRequest.getText());

        StringBuilder colorRegex = new StringBuilder();
        if (searchRequest.getColor() != null && searchRequest.getColor().isPresent()) {
            colorRegex = getRegexBuilder(searchRequest.getColor().get());
        }
        StringBuilder sizeRegex = new StringBuilder();
        if(searchRequest.getSize() != null && searchRequest.getSize().isPresent()){
           sizeRegex = getRegexBuilder(searchRequest.getSize().get());
        }

        BooleanQuery.Builder finalBooleanQueryBuilder = new BooleanQuery.Builder();
        Query companyNameQuery = new RegexpQuery(new Term(COMPANY_NAME, searchRequest.getCompanyName()));
        finalBooleanQueryBuilder.add(companyNameQuery, BooleanClause.Occur.MUST);

        BooleanQuery.Builder textBooleanQueryBuilder = new BooleanQuery.Builder();
        Query titleRegexpQuery = new RegexpQuery(new Term(TITLE, textRegex.toString()));
        textBooleanQueryBuilder.add(titleRegexpQuery, BooleanClause.Occur.SHOULD);

        Query prodDescriptionRegexpQuery = new RegexpQuery(new Term(PROD_DESCRIPTION, textRegex.toString()));
        textBooleanQueryBuilder.add(prodDescriptionRegexpQuery, BooleanClause.Occur.SHOULD);

        Query skuRegexpQuery = new RegexpQuery(new Term(SKU_ID, textRegex.toString()));
        textBooleanQueryBuilder.add(skuRegexpQuery, BooleanClause.Occur.SHOULD);

        Query productCategoryRegexpQuery = new RegexpQuery(new Term(PRODUCT_CATEGORY, textRegex.toString()));
        textBooleanQueryBuilder.add(productCategoryRegexpQuery, BooleanClause.Occur.SHOULD);

        if(colorRegex.toString().isEmpty()){
            Query colorRegexpQuery = new RegexpQuery(new Term(COLOR, textRegex.toString()));
            textBooleanQueryBuilder.add(colorRegexpQuery, BooleanClause.Occur.SHOULD);
        }else{
            Query colorRegexpQuery = new RegexpQuery(new Term(COLOR, colorRegex.toString()));
            finalBooleanQueryBuilder.add(colorRegexpQuery, BooleanClause.Occur.MUST);
        }

        finalBooleanQueryBuilder.add(textBooleanQueryBuilder.build(),BooleanClause.Occur.MUST);

        if(!sizeRegex.toString().isEmpty()) {
            Query sizeRegexQuery = new RegexpQuery(new Term(SIZE, sizeRegex.toString()));
            finalBooleanQueryBuilder.add(sizeRegexQuery, BooleanClause.Occur.MUST);
        }

        Query finalQuery = finalBooleanQueryBuilder.build();
        query.set(Q, finalQuery.toString());
        query.setRows(ROW_LIMIT);
        query.setRequestHandler("/query");

        QueryResponse response = null;
        try {
            response = solrClient.query(query);
        } catch (SolrServerException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SolrDocumentList results = response.getResults();
        List<SearchResponse> searchResponseList = new ArrayList<>();
        for (SolrDocument doc : results) {
            String companyName = String.valueOf(doc.get(COMPANY_NAME));
            String skuId = String.valueOf(doc.get(SKU_ID));
            String color = String.valueOf(doc.get(COLOR));
            String imageLink = String.valueOf(doc.get(ADDITIONAL_IMAGE_LINK));
            String title = String.valueOf(doc.get(TITLE));
            String prodDescription = String.valueOf(doc.get(PROD_DESCRIPTION));
            String listPrice = String.valueOf(doc.get(PRICE)) + " " +String.valueOf(doc.get(CURRENCY));
            String size = String.valueOf(doc.get(SIZE));

            SearchResponse searchResponse = new SearchResponse(companyName, skuId, imageLink, color, size, title, prodDescription, listPrice);
            searchResponseList.add(searchResponse);
        }
        return searchResponseList;
    }

    private StringBuilder getRegexBuilder(String inputValue){
        StringBuilder regexBuilder = new StringBuilder();
        String[] strings = inputValue.split(REQUEST_SPLIT_REGEX);
        regexBuilder.append(ADD_DEFAULT_REGEX);
        for(String s : strings){
            regexBuilder.append(s).append(ADD_DEFAULT_REGEX);
        }
        return regexBuilder;
    }
    public DocumentNoResponse getDocument() {
        SolrQuery query1 = new SolrQuery();

        query1.setQuery(DOCUMENTS_REGEX);
        query1.setRows(10);
        query1.setRequestHandler("/query");

        QueryResponse responseq1 = null;
        try {
            responseq1 = solrClient.query(query1);
        } catch (SolrServerException | IOException e) {
            System.out.println("Query is unsuccessful.");
        }


        int numDocs1 = (int) responseq1.getResults().getNumFound();
        return new DocumentNoResponse(numDocs1);
    }
}
