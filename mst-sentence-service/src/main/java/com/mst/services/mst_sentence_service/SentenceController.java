package com.mst.services.mst_sentence_service;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.SentenceQuery.SentenceQueryTextInput;
import com.mst.model.SentenceQuery.SentenceReprocessingInput;
import com.mst.model.metadataTypes.DiscreteDataBucketIdenticationType;
import com.mst.model.requests.SentenceRequest;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.sentenceprocessing.interfaces.QueryService;
import com.mst.sentenceprocessing.interfaces.SentenceService;
import com.mst.sentenceprocessing.models.Edges;
import com.mst.sentenceprocessing.models.TextResponse;
import com.mst.sentenceprocessing.services.QueryServiceImpl;
import com.mst.sentenceprocessing.services.SentenceServiceImpl;

@Path("sentence")
public class SentenceController {
	private final SentenceService sentenceService;
	private final QueryService queryService;

    public SentenceController() {
		sentenceService = new SentenceServiceImpl();
		queryService = new QueryServiceImpl();
	}

	@POST
	@Path("/getedgesfortokens")
	public Response getEdgeNamesForTokens(List<String> tokens) {
		try {
			List<String> edges = sentenceService.getEdgeNamesForTokens(tokens);
			return Response.status(200).entity(edges).build();
		} catch (Exception ex) {
			return Response.status(500).entity(ex.getMessage()).build();
		}
	}

	@POST
	@Path("/saveedges")
	public Response getEdgeNamesForTokens(Edges edges) {
		try {
			sentenceService.saveEdges(edges);
			return Response.status(200).entity(edges).build();
		} catch (Exception ex) {
			return Response.status(500).entity(ex.getMessage()).build();
		}
	}

	@POST
	@Path("/save")
	public Response saveSentence(SentenceRequest request) {
		try {
			List<Sentence> sentences = sentenceService.createSentences(request);
			sentenceService.saveSentences(sentences, request.getDiscreteData(), null, false, null,
					DiscreteDataBucketIdenticationType.compliance);
			return Response.status(200).entity("sentences Saved successfully").build();
		} catch (Exception ex) {
			return Response.status(500).entity(ex.getMessage()).build();
		}
	}

	@POST
	@Path("/reprocess")
	public Response reprocess(SentenceReprocessingInput input) {
		try {
			String reprocessId = sentenceService.reprocessSentences(input);
			return Response.status(200).entity(reprocessId).build();
		} catch (Exception ex) {
			return Response.status(500).entity(ex.getMessage()).build();
		}
	}

	@GET
	@Path("/reprocessdiscretedata/{id}")
	public Response reprocessDiscreteData(@PathParam("id") String id) {
    	try{
    		sentenceService.reprocessDiscreteData(id);
    		return Response.status(200).entity("reprocessing successfully").build();
    	}
    	catch(Exception ex){
    		return Response.status(500).entity(ex.getMessage()).build();
    	}
    }
    
    @POST
    @Path("/savetext")
    public Response saveText(SentenceTextRequest sentenceTextRequest){
    	try{
			sentenceService.processSentenceTextRequest(sentenceTextRequest);
			TextResponse response = new TextResponse();
			response.setMessage("sentences Saved");
			response.setResult("successful");
			return Response.status(200).entity(response).build();	
    	}
    	catch(Exception ex){
    		TextResponse response = new TextResponse();
			response.setMessage(ex.getMessage());
			response.setResult("error");
			return Response.status(500).entity(response).build();	
    	}
    }
   
    @POST
    @Path("/query")
    public Response querySentences(SentenceQueryInput input){
    	try{
	    	input.setFilterByReport(true);
	    	input.setFilterByTokenSequence(true);
    		List<SentenceQueryResult> queryResults = sentenceService.querySentences(input);
    		sentenceService.RemoveNonDisplayEdges(queryResults);
	    	SentenceQueryOutput result = new SentenceQueryOutput();
	    	result.setSentenceQueryResults(queryResults);
	    	result.setSize(queryResults.size());
	    	return Response.status(200).entity(result).build(); 
    	}
    	catch(Exception ex){
    		ex.printStackTrace();
    		return Response.status(500).entity(ex.getMessage()).build();
    	}
    }
            
	@POST
	@Path("/querytext")
	public Response queryTextSentences(SentenceQueryTextInput input) {
		try {
			List<SentenceQueryResult> queryResults = queryService.queryTextSentences(input);
			SentenceQueryOutput result = new SentenceQueryOutput();
			result.setSentenceQueryResults(queryResults);
			result.setSize(queryResults.size());
			return Response.status(200).entity(result).build();
		} catch (Exception ex) {
			ex.printStackTrace();
			return Response.status(500).entity(ex.getMessage()).build();
		}
	}

    @GET
    @Path("/getsentencetextfordiscreteid/{id}")
    public Response getSentenceTextForDiscreteId(@PathParam("id") String id){
    	try{
	    	List<String> queryResults = sentenceService.getSentenceTextForDiscreteDataId(id);
	    	return Response.status(200).entity(queryResults).build(); 
    	}
    	catch(Exception ex){
    		return Response.status(500).entity(ex.getMessage()).build();
    	}
    }

	@GET
	@Path("processingdata")
	public Response getProcessingData() {
		SentenceProcessingMetaDataInput input = sentenceService.getSentenceProcessingMetadata();
		return Response.status(200).entity(input).build();
	}

	@POST
	@Path("/testandnotall")
	public Response testAndNotAll(SentenceQueryInput input) {
		try {
			// SentenceQueryInput result = new
			// NotAndAllRequestFactoryImpl().create(input);
			return Response.status(200).entity("").build();
		} catch (Exception ex) {
			return Response.status(500).entity(ex.getMessage()).build();
		}
	}
}
