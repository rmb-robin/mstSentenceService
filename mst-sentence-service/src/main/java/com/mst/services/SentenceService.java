package com.mst.services;

import java.util.ArrayList;
import java.util.List;

import org.mongodb.morphia.Datastore;

import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.sentenceprocessing.SentenceConverter;
import com.mst.services.mst_sentence_service.Constants;

public class SentenceService {

	
	public List<SentenceDb> getSentences(){
		return getDatastore().createQuery(SentenceDb.class).asList();
	}
	
	private Datastore getDatastore() {
    	return Constants.MorphiaHelper.INSTANCE.getDatastore();
    }
	
	public void saveSentences(List<Sentence> sentences){
		List<SentenceDb> documents = new ArrayList<SentenceDb>();
		for(Sentence sentence: sentences){
			documents.add(SentenceConverter.convertToDocument(sentence));
		}
		getDatastore().save(documents);
	}
}
