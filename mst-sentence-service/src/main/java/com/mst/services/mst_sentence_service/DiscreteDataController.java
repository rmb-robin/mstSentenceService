package com.mst.services.mst_sentence_service;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.mst.model.SentenceQuery.DiscreteDataFilter;
import com.mst.model.SentenceQuery.SentenceQueryResultDisplayFields;
import com.mst.model.discrete.DiscreteData;
import com.mst.sentenceprocessing.interfaces.DiscreteDataService;
import com.mst.sentenceprocessing.models.DiscreteDataRequest;
import com.mst.sentenceprocessing.models.DiscreteDataResult;
import com.mst.sentenceprocessing.models.TextResponse;
import com.mst.sentenceprocessing.services.DiscreteDataServiceImpl;


@Path("discretedata")
public class DiscreteDataController {

	private DiscreteDataService discreteDataService;

	public DiscreteDataController() {
		discreteDataService = new DiscreteDataServiceImpl();
	}
	
	@POST
    @Path("/query")
    public Response querySentences(DiscreteDataRequest request){
    	try{
	    	List<DiscreteDataResult> result = discreteDataService.getDiscreteDatas(request);
    	return Response.status(200).entity(result).build(); 
    	}
    	catch(Exception ex){
    		return Response.status(500).entity(ex.getMessage()).build();
    	}
    }
	
	
	@POST
    @Path("/saveSentenceQueryResultDisplayFields")
    public Response save(SentenceQueryResultDisplayFields fields){
    	try{
	     discreteDataService.saveSentenceQueryResultDisplayFields(fields);
    	return Response.status(200).entity("saved").build(); 
    	}
    	catch(Exception ex){
    		return Response.status(500).entity(ex.getMessage()).build();
    	}
    }
	
	@GET
	@Path("/getsentencequeryresultdisplayfieldsbyorg/{id}")
	public Response getSentenceQueryResultDisplayFields(@PathParam("id") String orgId){
		try{
			SentenceQueryResultDisplayFields fields = discreteDataService.getSentenceQueryResultByOrgId(orgId);
			return Response.status(200).entity(fields).build(); 
    	}
    	catch(Exception ex){
    		TextResponse response = new TextResponse(); 
    		response.setMessage(ex.getMessage());
    		response.setResult("error");
    		return Response.status(500).entity(response).build();
    	}
	}

	@GET
	@Path("/getbyid/{id}")
	public Response getById(@PathParam("id") String id){
    	try{
	    	DiscreteDataResult result = discreteDataService.getById(id);
    	return Response.status(200).entity(result).build(); 
    	}
    	catch(Exception ex){
    		return Response.status(500).entity(ex.getMessage()).build();
    	}
    }
	
	
	
    
	
}
