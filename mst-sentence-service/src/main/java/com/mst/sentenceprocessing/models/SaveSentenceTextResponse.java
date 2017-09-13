package com.mst.sentenceprocessing.models;

import java.util.ArrayList;
import java.util.List;

import com.mst.model.discrete.Followup;

public class SaveSentenceTextResponse {

	private String discreteDataMongoId; 
	private String durationFollowup; 
	private List<String> followupProcedures; 
	
	public SaveSentenceTextResponse(){
		followupProcedures = new ArrayList<>();
	}

	public String getDiscreteDataMongoId() {
		return discreteDataMongoId;
	}
	public void setDiscreteDataMongoId(String discreteDataMongoId) {
		this.discreteDataMongoId = discreteDataMongoId;
	}

	public String getDurationFollowup() {
		return durationFollowup;
	}

	public void setDurationFollowup(String durationFollowup) {
		this.durationFollowup = durationFollowup;
	}

	public List<String> getFollowupProcedures() {
		return followupProcedures;
	}

	public void setFollowupProcedures(List<String> followupProcedures) {
		this.followupProcedures = followupProcedures;
	}
	
}
