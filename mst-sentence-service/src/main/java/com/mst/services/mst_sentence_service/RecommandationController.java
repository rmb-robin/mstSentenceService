package com.mst.services.mst_sentence_service;

 

import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.requests.RecommandationRequest;
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
	public Response saveRecommandation(RecommandationRequest request) throws Exception{
    	try{
    		List<SentenceDiscovery> sentenceDiscoveries =  service.createSentenceDiscovery(request);
    		service.saveSentenceDiscoveries(sentenceDiscoveries);
    		service.processingVerification(sentenceDiscoveries);
    		return Response.status(200).entity("discoveries Saved successfully").build();
    		
    	}
    	catch(Exception ex){
    		return Response.status(500).entity(ex.getMessage()).build();
    	}
    }
}
