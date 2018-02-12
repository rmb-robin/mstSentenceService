package com.mst.services.mst_sentence_service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import com.mst.interfaces.sentenceprocessing.DiscreteDataNormalizer;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.SentenceQuery.SentenceQueryTextInput;
import com.mst.model.SentenceQuery.SentenceReprocessingInput;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.metadataTypes.DiscreteDataBucketIdenticationType;
import com.mst.model.requests.RejectedReport;
import com.mst.model.requests.SentenceRequest;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.model.sentenceProcessing.SentenceProcessingResult;
import com.mst.sentenceprocessing.DiscreteDataNormalizerImpl;
import com.mst.sentenceprocessing.interfaces.RecommandationService;
import com.mst.sentenceprocessing.interfaces.SentenceService;
import com.mst.sentenceprocessing.models.Edges;
import com.mst.sentenceprocessing.models.SaveSentenceTextResponse;
import com.mst.sentenceprocessing.services.RecommandationServiceImpl;
import com.mst.sentenceprocessing.services.SaveSentenceTextResponseFactory;
import com.mst.sentenceprocessing.services.SentenceServiceImpl;
import org.apache.log4j.Logger;

@Path("sentence")
public class SentenceController {

	private SentenceService sentenceService; 
	private RecommandationService recommandationService;
	
	public SentenceController() {
		sentenceService = new SentenceServiceImpl();
		recommandationService = new RecommandationServiceImpl();
	}

	@POST
	@Path("/getedgesfortokens")
	public Response getEdgeNamesForTokens(List<String> tokens){
		try{
			List<String> edges = sentenceService.getEdgeNamesForTokens(tokens);
			return Response.status(200).entity(edges).build();
		}
		catch(Exception ex){
    		return Response.status(500).entity(ex.getMessage()).build();
    	}
	}
	
	@POST
	@Path("/saveedges")
	public Response getEdgeNamesForTokens(Edges edges){
		try{
			 sentenceService.saveEdges(edges);
			return Response.status(200).entity(edges).build();
		}
		catch(Exception ex){
    		return Response.status(500).entity(ex.getMessage()).build();
    	}
	}
	
	
    @POST
	@Path("/save")
	public Response saveSentence(SentenceRequest request) throws Exception{
    	try{
	    	List<Sentence> sentences = sentenceService.createSentences(request);
	    	sentenceService.saveSentences(sentences, request.getDiscreteData(),null,false,null, DiscreteDataBucketIdenticationType.compliance);
		return Response.status(200).entity("sentences Saved successfully").build();
    	}
    	catch(Exception ex){
    		return Response.status(500).entity(ex.getMessage()).build();
    	}
    }

    @POST
	@Path("/reprocess")
	public Response reprocess(SentenceReprocessingInput input) throws Exception{
    	try{
    		String reprocessId = sentenceService.reprocessSentences(input);
    		return Response.status(200).entity(reprocessId).build();
    	}
    	catch(Exception ex){
    		return Response.status(500).entity(ex.getMessage()).build();
    	}
    }
   
    @GET
	@Path("/reprocessdiscretedata/{id}")
	public Response reprocessDiscreteData(@PathParam("id") String id) throws Exception{
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
    	//	sentenceService.processSentenceTextRequest(sentenceTextRequest);
    		recommandationService.saveSentenceDiscoveryProcess(sentenceTextRequest);
	    	return Response.status(200).entity("sentences Saved successfully").build();	
    	}
    	catch(Exception ex){
    		return Response.status(500).entity(ex.getMessage()).build();
    	}
    }
   
    @POST
    @Path("/query")
    public Response querySentences(SentenceQueryInput input){
    	try{
	    	List<SentenceQueryResult> queryResults = sentenceService.querySentences(input);
	    	SentenceQueryOutput result = new SentenceQueryOutput();
	    	result.setSentenceQueryResults(queryResults);
	    	result.setSize(queryResults.size());
    	return Response.status(200).entity(result).build(); 
    	}
    	catch(Exception ex){
    		return Response.status(500).entity(ex.getMessage()).build();
    	}
    }
    
    
    @POST
    @Path("/querytext")
    public Response queryTextSentences(SentenceQueryTextInput input){
    	try{
	    	List<SentenceQueryResult> queryResults = sentenceService.queryTextSentences(input);
	    	SentenceQueryOutput result = new SentenceQueryOutput();
	    	result.setSentenceQueryResults(queryResults);
	    	result.setSize(queryResults.size());
    	return Response.status(200).entity(result).build(); 
    	}
    	catch(Exception ex){
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
    public Response getProcessingData(){
    	SentenceProcessingMetaDataInput input = sentenceService.getSentenceProcessingMetadata();
    	return Response.status(200).entity(input).build();
    }
    
    @POST 
    @Path("/testandnotall")
    public Response testAndNotAll(SentenceQueryInput input){
    	try{
    		//SentenceQueryInput result = new NotAndAllRequestFactoryImpl().create(input);
    		return Response.status(200).entity("").build();
    	}
    	catch(Exception ex){
    		return Response.status(500).entity(ex.getMessage()).build();
    	}
    }
}
