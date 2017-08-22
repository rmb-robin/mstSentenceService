package com.mst.sentenceprocessing.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mst.model.sentenceProcessing.Sentence;

public class SentenceByDiscreteDataMapper {

	public static  Map<String, List<Sentence>>groupSentencesByDiscretedata(List<Sentence> sentences){
		Map<String, List<Sentence>> sentencesByDiscreteDataId = new HashMap<>();
		
		for(Sentence sentence: sentences){
			String key = sentence.getDiscreteData().getId().toString();
				
			if(!sentencesByDiscreteDataId.containsKey(key))
					sentencesByDiscreteDataId.put(key, new ArrayList<Sentence>());
			sentencesByDiscreteDataId.get(key).add(sentence);
		}
		return sentencesByDiscreteDataId;
	}
	
}
