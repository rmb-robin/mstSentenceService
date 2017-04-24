package com.mst.services.mst_sentence_service;


import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import com.mst.interfaces.SentenceProcessingController;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.sentenceprocessing.SentenceProcessingControllerImpl;
import com.mst.services.SentenceProcessingDbMetaDataInputFactory;
import com.mst.services.SentenceService;


@Path("sentence")
public class SentenceController {

    @POST
	@Path("/save")
	public Response shareAdvancedSearch(SentenceRequest request) throws Exception{
    	SentenceProcessingController controller; 
    	controller = new SentenceProcessingControllerImpl();
    	controller.setMetadata(new SentenceProcessingDbMetaDataInputFactory().create());
    	List<Sentence> sentences = controller.processSentences(request.getSenteceTexts());
    	new SentenceService().saveSentences(sentences);
		return Response.status(200).entity("sentences Saved successfully").build();
    }
    
    @GET
    public Response getSentences() throws Exception{
   
    	List<SentenceDb> sentences = new SentenceService().getSentences();
    	SentenceResult result = new SentenceResult();
    	result.setSentences(sentences);
    	return Response.status(200).entity(result).build();
    }
    
}
