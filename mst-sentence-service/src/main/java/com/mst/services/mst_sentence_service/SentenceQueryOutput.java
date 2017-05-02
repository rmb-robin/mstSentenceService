package com.mst.services.mst_sentence_service;

import java.util.List;

import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.sentenceProcessing.SentenceDb;

public class SentenceQueryOutput {

	private List<SentenceQueryResult> queryResults;

	public List<SentenceQueryResult> getSentenceQueryResults() {
		return queryResults;
	}

	public void setSentenceQueryResults(List<SentenceQueryResult> queryResults) {
		this.queryResults = queryResults;
	} 
}
