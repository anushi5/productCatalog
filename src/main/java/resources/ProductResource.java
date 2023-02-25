package resources;

import models.*;
import service.SearchService;
import service.UploadService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/product")
public class ProductResource {

    SearchService searchService =  new SearchService();
    UploadService uploadService = new UploadService();
    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SearchResponse> searchProducts(SearchRequest searchRequest){
        return searchService.getSearchResult(searchRequest);
    }

    @POST
    @Path("/uploadData")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public UploadResponse uploadData(UploadRequest uploadRequest){
        UploadResponse uploadResponse = uploadService.uploadFileData(uploadRequest);
        return uploadResponse;
    }

    @GET
    @Path("/totalDocs")
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentNoResponse getTotalDocumentsFromSolr(){
        return searchService.getDocument();
    }
}
