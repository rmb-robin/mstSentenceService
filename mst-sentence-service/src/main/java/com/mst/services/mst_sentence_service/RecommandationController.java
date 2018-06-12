package com.mst.services.mst_sentence_service;

 

import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.mst.model.autocomplete.AutoCompleteRequest;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.requests.RecommandationRequest;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.sentenceprocessing.interfaces.RecommandationService;
import com.mst.sentenceprocessing.services.RecommandationServiceImpl;
 
@Path("recommandation")
public class RecommandationController {

	private RecommandationService service; 
	
	public RecommandationController() {
		service = new RecommandationServiceImpl();
	}
	
	@POST
	@Path("/save")
	public Response saveRecommandation(SentenceTextRequest request) throws Exception{
    	try{
    		service.saveSentenceDiscoveryProcess(request);
    		return Response.status(200).entity("discoveries Saved successfully").build();
    		
    	}
    	catch(Exception ex){
    		return Response.status(500).entity(ex.getMessage()).build();
    	}
    }
	

	@POST 
	@Path("/reload")
	public Response reload(){
		try{
			service.reloadCache();
			return Response.status(200).entity("cache reload successful").build(); 
    	}
    	catch(Exception ex){
    		return Response.status(500).entity(ex.getMessage()).build();
    	}
	}
	
	@POST 
	@Path("/getautocomplete")
	public Response getAutoComplete(AutoCompleteRequest autoCompleteRequest){
		try{
			List<String> result = service.getAutoComplete(autoCompleteRequest);
			return Response.status(200).entity(result).build(); 
    	}
    	catch(Exception ex){
    		return Response.status(500).entity(ex.getMessage()).build();
    	}
	}
	
}
