package com.mst.sentenceprocessing.models;

import java.util.ArrayList;
import java.util.List;

import com.mst.model.discrete.DiscreteData;
import com.mst.model.sentenceProcessing.Sentence;

public class DiscreteDataResult {

	private DiscreteData discreteData;
	private List<String> sentences;

	public DiscreteDataResult(){
		sentences = new ArrayList<>();
	}
	
	public DiscreteData getDiscreteData() {
		return discreteData;
	}
	
	public void setDiscreteData(DiscreteData discreteData) {
		this.discreteData = discreteData;
	}

	public List<String> getSentences() {
		return sentences;
	}

	public void setSentences(List<String> sentences) {
		this.sentences = sentences;
	}

	
}
