package com.mst.sentenceprocessing.services;

import com.mst.model.discrete.Followup;
import com.mst.model.discrete.FollowupProcedure;
import com.mst.sentenceprocessing.models.SaveSentenceTextResponse;

public class SaveSentenceTextResponseFactory {

	public static SaveSentenceTextResponse create(String mongoId, Followup followup){
		SaveSentenceTextResponse response = new SaveSentenceTextResponse();
		response.setDiscreteDataMongoId(mongoId);
		
		if(followup==null)return response; 
		
		response.setDurationFollowup(followup.getDurationMeasure());
		for(FollowupProcedure followupProcedure: followup.getProcedures()){
			response.getFollowupProcedures().add(followupProcedure.getDisplayText());
		}
		return response; 
	}
}
