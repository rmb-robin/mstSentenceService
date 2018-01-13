package com.mst.services.mst_sentence_service;

import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import com.mst.model.HL7Details;
import com.mst.model.raw.ParseHl7Result;
import com.mst.model.raw.RawReportFile;
import com.mst.model.requests.RejectedReport;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.sentenceprocessing.SentenceConverter;
import com.mst.sentenceprocessing.interfaces.RawReportService;
import com.mst.sentenceprocessing.interfaces.RecommandationService;
import com.mst.sentenceprocessing.interfaces.ReportsService;
import com.mst.sentenceprocessing.interfaces.SentenceService;
import com.mst.sentenceprocessing.models.RawFileSaveResult;
import com.mst.sentenceprocessing.services.RawReportServiceImpl;
import com.mst.sentenceprocessing.services.RecommandationServiceImpl;
import com.mst.sentenceprocessing.services.ReportsServiceImpl;
import com.mst.sentenceprocessing.services.SentenceServiceImpl;

@Path("rawreport")
public class RawReportController {

	private RawReportService service = new RawReportServiceImpl();
	private ReportsService reportsService = new ReportsServiceImpl();
	private RecommandationService recommandationService = new RecommandationServiceImpl();
	
	@POST
	@Path("/save")
	public Response saveRawFile(RawReportFile file){
		try{
			List<HL7Details> detailsList =  reportsService.getHL7DetailsByOrgName(file.getOrgName()); 
			if(detailsList.isEmpty()) throw new Exception("Missing Hl7 Details");
			HL7Details details = detailsList.get(0);	
			
			ParseHl7Result parsedResult = service.getSetentenceTextRequestFromRaw(details, file);
			SentenceTextRequest request= parsedResult.getSentenceTextRequest();
			
			request.getDiscreteData().setOrganizationId(file.getOrgId());
			RawFileSaveResult rawFileSaveResult = service.save(request,file);
			String fileId = rawFileSaveResult.getFileId();
			if(!parsedResult.getMissingFields().isEmpty()) {
				RejectedReport rejectedReport = new RejectedReport();
				rejectedReport.setAccessionNumber(request.getDiscreteData().getAccessionNumber());
				rejectedReport.setOrganizationId(file.getOrgId());
				rejectedReport.setMissingFields(parsedResult.getMissingFields());
				rejectedReport.setReadingLocation(request.getDiscreteData().getReadingLocation());
				rejectedReport.setRawFileId(fileId);
				reportsService.saveRejectedReport(rejectedReport);
				return Response.status(400).entity("Rejected Report").build();
			}	
			
			if(rawFileSaveResult.isDuplicate()) 
				return Response.status(200).entity("Report already existed.").build(); 
			
			request.getDiscreteData().setRawFileId(fileId);
			service.saveParsed(fileId,request);
			recommandationService.saveSentenceDiscoveryProcess(request);
			
			
			return Response.status(200).entity("Report Saved Successfully").build(); 
		}
		catch(Exception ex){
			return Response.status(500).entity(ex.toString()).build();
		}
	}
}
