package com.mst.services.mst_sentence_service;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.mst.model.HL7Details;
import com.mst.model.requests.RejectedReport;
import com.mst.sentenceprocessing.interfaces.ReportsService;
import com.mst.sentenceprocessing.models.ReportSummaryRequest;
import com.mst.sentenceprocessing.models.ReportSummaryResponse;
import com.mst.sentenceprocessing.services.ReportsServiceImpl;
 


@Path("reports")
public class ReportsController {

	
	private ReportsService reportsService; 
	
	public ReportsController() {
		reportsService = new ReportsServiceImpl();
	}
	
	@GET
	@Path("getHl7Details/{orgName}")
    public Response getUserById(@PathParam("orgName") String orgName) {
		try{
			List<HL7Details> details =  reportsService.getHL7DetailsByOrgName(orgName);
			return Response.status(200).entity(details).build(); 
		}
		catch(Exception ex){
			return Response.status(500).entity(ex.getMessage()).build();
		}
	}

	@POST
	@Path("/saverejectedreport")
	public Response saveRejectedReport(RejectedReport report){
		try{
			reportsService.saveRejectedReport(report);
			return Response.status(200).entity("rejected Report Saved.").build(); 
		}
		catch(Exception ex){
			return Response.status(500).entity(ex.getMessage()).build();
		}
	}
	
	@POST
	@Path("/getreportsummary")
	public Response getReportSummary(ReportSummaryRequest request) {
	try{
			ReportSummaryResponse response  = reportsService.getReportSummary(request);
			return Response.status(200).entity(response).build(); 
		}
		catch(Exception ex){
			return Response.status(500).entity(ex.getMessage()).build();
		}
	}
}
