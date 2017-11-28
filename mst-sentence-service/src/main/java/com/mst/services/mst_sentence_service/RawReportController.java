package com.mst.services.mst_sentence_service;

import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import com.mst.model.HL7Details;
import com.mst.model.raw.RawReportFile;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.sentenceprocessing.interfaces.RawReportService;
import com.mst.sentenceprocessing.interfaces.ReportsService;
import com.mst.sentenceprocessing.interfaces.SentenceService;
import com.mst.sentenceprocessing.services.RawReportServiceImpl;
import com.mst.sentenceprocessing.services.ReportsServiceImpl;
import com.mst.sentenceprocessing.services.SentenceServiceImpl;

@Path("rawreport")
public class RawReportController {

	private RawReportService service = new RawReportServiceImpl();
	private SentenceService sentenceService = new SentenceServiceImpl();
	private ReportsService reportsService = new ReportsServiceImpl();
	@POST
	@Path("/save")
	public Response saveRawFile(RawReportFile file){
		try{
			service.save(file);
			List<HL7Details> detailsList =  reportsService.getHL7DetailsByOrgName("rad"); 
			if(detailsList.isEmpty()) throw new Exception("Missing Hl7 Details");
			HL7Details details = detailsList.get(0);	//todo change org To come from file.	
			SentenceTextRequest request = service.getSetentenceTextRequestFromRaw(details, file);
			sentenceService.processSentenceTextRequest(request);
			return Response.status(200).entity("Report Saved Successfully").build(); 
		}
		catch(Exception ex){
			return Response.status(500).entity(ex.getMessage()).build();
		}
	}
}
