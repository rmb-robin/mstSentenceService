package com.mst.sentenceprocessing.models;

import java.time.LocalDate;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mst.jsonSerializers.LocalDateDeserializer;

public class ReportSummaryRequest {

	@JsonDeserialize(using = LocalDateDeserializer.class)  
	private LocalDate reportDate; 
	private String organizationName;
	
	
	public String getOrganizationName() {
		return organizationName;
	}
	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}

	public LocalDate getReportDate() {
		return reportDate;
	}
	public void setReportDate(LocalDate reportDate) {
		this.reportDate = reportDate;
	}
}