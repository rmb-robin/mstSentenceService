package com.mst.services.mst_sentence_service;

import java.util.List;

import com.mst.model.sentenceProcessing.SentenceDb;

public class SentenceResult {

	private List<SentenceDb> sentences;

	public List<SentenceDb> getSentences() {
		return sentences;
	}

	public void setSentences(List<SentenceDb> sentences) {
		this.sentences = sentences;
	} 
}
