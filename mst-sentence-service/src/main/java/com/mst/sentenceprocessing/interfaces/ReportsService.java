package com.mst.sentenceprocessing.interfaces;

import com.mst.model.requests.RejectedReport;
import com.mst.sentenceprocessing.models.ReportSummaryRequest;
import com.mst.sentenceprocessing.models.ReportSummaryResponse;

public interface ReportsService {

	void saveRejectedReport(RejectedReport rejectedReport);
	ReportSummaryResponse getReportSummary(ReportSummaryRequest reportSummaryRequest);
} 
