package com.mst.services.mst_sentence_service;


import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.mst.interfaces.sentenceprocessing.DiscreteDataNormalizer;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.requests.RejectedReport;
import com.mst.model.requests.SentenceRequest;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.sentenceprocessing.DiscreteDataNormalizerImpl;
import com.mst.sentenceprocessing.interfaces.SentenceService;
import com.mst.sentenceprocessing.services.SentenceServiceImpl;


@Path("sentence")
public class SentenceController {

	private SentenceService sentenceService; 
	
	public SentenceController() {
		sentenceService = new SentenceServiceImpl();
	}
	
	
    @POST
	@Path("/save")
	public Response saveSentence(SentenceRequest request) throws Exception{
    	try{
	    	List<Sentence> sentences = sentenceService.createSentences(request);
	    	DiscreteData discreteDate = new DiscreteDataNormalizerImpl().process(request.getDiscreteData());
	    	sentenceService.saveSentences(sentences, discreteDate);
		return Response.status(200).entity("sentences Saved successfully").build();
    	}
    	catch(Exception ex){
    		return Response.status(500).entity(ex.getMessage()).build();
    	}
    }
    
    @POST
    @Path("/savetext")
    public Response saveText(SentenceTextRequest sentenceTextRequest){
    	try{
	    	List<Sentence> sentences = sentenceService.createSentences(sentenceTextRequest);
	    	DiscreteData discreteDate = new DiscreteDataNormalizerImpl().process(sentenceTextRequest.getDiscreteData());
	    	sentenceService.saveSentences(sentences, discreteDate);
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
    	return Response.status(200).entity(result).build(); 
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
}
