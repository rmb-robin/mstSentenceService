package com.mst.sentenceprocessing.interfaces;

import java.util.List;

import com.mst.model.HL7Details;
import com.mst.model.requests.RejectedReport;
import com.mst.sentenceprocessing.models.ReportSummaryRequest;
import com.mst.sentenceprocessing.models.ReportSummaryResponse;

public interface ReportsService {

	void saveRejectedReport(RejectedReport rejectedReport);
	ReportSummaryResponse getReportSummary(ReportSummaryRequest reportSummaryRequest);
	List<HL7Details> getHL7DetailsByOrgName(String orgName);
} 
