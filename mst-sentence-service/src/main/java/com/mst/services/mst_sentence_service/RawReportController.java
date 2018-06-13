package com.mst.services.mst_sentence_service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.mst.model.HL7Details;
import com.mst.model.raw.AllHl7Elements;
import com.mst.model.raw.ParseHl7Result;
import com.mst.model.raw.RawReportFile;
import com.mst.model.requests.RejectedReport;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.sentenceprocessing.interfaces.RawReportService;
import com.mst.sentenceprocessing.interfaces.RecommendationService;
import com.mst.sentenceprocessing.interfaces.ReportsService;
import com.mst.sentenceprocessing.interfaces.SentenceService;
import com.mst.sentenceprocessing.models.RawFileSaveResult;
import com.mst.sentenceprocessing.models.TextResponse;
import com.mst.sentenceprocessing.services.RawReportServiceImpl;
import com.mst.sentenceprocessing.services.RecommandationServiceImpl;
import com.mst.sentenceprocessing.services.ReportsServiceImpl;
import com.mst.sentenceprocessing.services.SentenceServiceImpl;

@Path("rawreport")
public class RawReportController {

	private RawReportService service = new RawReportServiceImpl();
	private ReportsService reportsService = new ReportsServiceImpl();
	private RecommendationService recommendationService = new RecommandationServiceImpl();
	private SentenceService sentenceService = new SentenceServiceImpl(); 
	
	
	private static HashSet<String> reprocessLocations = populateReprocessLocations();
	
	private static HashSet<String> populateReprocessLocations(){
		HashSet<String> result = new HashSet<>();
		result.add("CRCR"); 
		result.add("FELC");
		result.add("FELE");
		result.add("FMS");
		result.add("FSJH");
		result.add("FYMN");
		result.add("RSRS");
		result.add("SDMC");
		result.add("SMDO");
		result.add("SMDY");
		result.add("SMHA");
		
		result.add("SPCP");
		result.add("SPFP");
		result.add("SPP1");
		return result;
	}

	private boolean needsreprocessOnLocation(SentenceTextRequest request){
		if(request==null) return false;
		if(request.getDiscreteData()==null)return false; 
		if(request.getDiscreteData().getReadingLocation()==null) return false; 
		
		String readingLocation = request.getDiscreteData().getReadingLocation();
		if(reprocessLocations.contains(readingLocation))return true;
		return false; 
	}
	
	@POST 
	@Path("/reprocess")
	public Response reprocess(RawReportFile file){
		try{
			List<HL7Details> detailsList =  reportsService.getHL7DetailsByOrgName(file.getOrgName()); 
			if(detailsList.isEmpty()) throw new Exception("Missing Hl7 Details");
			HL7Details details = detailsList.get(0);	
			
			AllHl7Elements allHl7Elements= reportsService.getAllHl7Elements();
			
			ParseHl7Result parsedResult = service.getSetentenceTextRequestFromRaw(details, file,allHl7Elements);
			SentenceTextRequest request= parsedResult.getSentenceTextRequest();
	
			if(!needsreprocessOnLocation(request))
				return Response.status(200).entity("Location Does not need reprocess").build(); 

			
			request.getDiscreteData().setOrganizationId(file.getOrgId());
			request.getDiscreteData().setAllAvailableFields(parsedResult.getAllFields());
			RawFileSaveResult rawFileSaveResult = service.save(request,file);
			String fileId = rawFileSaveResult.getFileId();
			
			if(rawFileSaveResult.isDuplicate()) 
				return Response.status(200).entity("Report already existed.").build();
			
			request.getDiscreteData().setRawFileId(fileId);
			String parsedId = service.saveParsed(fileId,request);
			request.getDiscreteData().setParseReportId(parsedId);
			
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

			request.getDiscreteData().getAllAvailableFields().clear();
			sentenceService.processSentenceTextRequest(request);		
			
			return Response.status(200).entity("Report Saved Successfully").build(); 
		}
		catch(Exception ex){ 
			TextResponse response = new TextResponse();
			response.setResult("error");
			response.setMessage(ex.getMessage());
			return Response.status(500).entity(response).build();
		}
	}
	
	
	
	@POST
	@Path("/save")
	public Response saveRawFile(RawReportFile file){
		try{
			List<HL7Details> detailsList =  reportsService.getHL7DetailsByOrgName(file.getOrgName()); 
			if(detailsList.isEmpty()) throw new Exception("Missing Hl7 Details");
			HL7Details details = detailsList.get(0);	
			
			AllHl7Elements allHl7Elements= reportsService.getAllHl7Elements();
			
			ParseHl7Result parsedResult = service.getSetentenceTextRequestFromRaw(details, file,allHl7Elements);
			SentenceTextRequest request= parsedResult.getSentenceTextRequest();
	
			
			request.getDiscreteData().setOrganizationId(file.getOrgId());
			request.getDiscreteData().setAllAvailableFields(parsedResult.getAllFields());
			RawFileSaveResult rawFileSaveResult = service.save(request,file);
			String fileId = rawFileSaveResult.getFileId();
			
			if(rawFileSaveResult.isDuplicate()) 
				return Response.status(200).entity("Report already existed.").build();
			
			request.getDiscreteData().setRawFileId(fileId);
			String parsedId = service.saveParsed(fileId,request);
			request.getDiscreteData().setParseReportId(parsedId);
			
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

			request.getDiscreteData().getAllAvailableFields().clear();
			sentenceService.processSentenceTextRequest(request);		
			
			return Response.status(200).entity("Report Saved Successfully").build(); 
		}
		catch(Exception ex){ 
			TextResponse response = new TextResponse();
			response.setResult("error");
			response.setMessage(ex.getMessage());
			return Response.status(500).entity(response).build();
		}
	}
}
