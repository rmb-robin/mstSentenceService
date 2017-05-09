package com.mst.sentenceprocessing.models;

import java.time.LocalDate;
import java.util.List;

import com.mst.model.requests.RejectedReport;

public class ReportSummaryResponse {

	private LocalDate date; 
	private long totalNonRejectedReports;
	
	
	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public long getTotalNonRejectedReports() {
		return totalNonRejectedReports;
	}

	public void setTotalNonRejectedReports(long totalNonRejectedReports) {
		this.totalNonRejectedReports = totalNonRejectedReports;
	}

	
	
	private List<RejectedReport> rejectedReports; 

	public List<RejectedReport> getRejectedReports() {
		return rejectedReports;
	}

	public void setRejectedReports(List<RejectedReport> rejectedReports) {
		this.rejectedReports = rejectedReports;
	}	
}
