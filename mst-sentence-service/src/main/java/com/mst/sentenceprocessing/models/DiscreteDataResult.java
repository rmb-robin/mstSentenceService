package com.mst.sentenceprocessing.models;

import java.util.ArrayList;
import java.util.List;

import com.mst.model.discrete.DiscreteData;
import com.mst.model.sentenceProcessing.Sentence;

public class DiscreteDataResult {

	private DiscreteData discreteData;
	private String text;

	public DiscreteData getDiscreteData() {
		return discreteData;
	}

	public void setDiscreteData(DiscreteData discreteData) {
		this.discreteData = discreteData;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
}
