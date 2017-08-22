package com.mst.sentenceprocessing.models;

import javax.ws.rs.Path;

import com.mst.model.SentenceQuery.DiscreteDataFilter;


public class DiscreteDataRequest {

	private DiscreteDataFilter discreteDataFilter;
	public DiscreteDataFilter getDiscreteDataFilter() {
		return discreteDataFilter;
	}
	public void setDiscreteDataFilter(DiscreteDataFilter discreteDataFilter) {
		this.discreteDataFilter = discreteDataFilter;
	}

	private String organizationId;
	public String getOrganizationId() {
		return organizationId;
	}
	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}

	private boolean includeSentences;
	
 
	
	public boolean isIncludeSentences() {
		return includeSentences;
	}
	
	public void setIncludeSentences(boolean includeSentences) {
		this.includeSentences = includeSentences;
	}
}
