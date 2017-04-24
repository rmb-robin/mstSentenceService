package com.mst.services;

import java.util.List;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.mst.interfaces.SentenceProcessingMetaDataInputFactory;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.services.mst_sentence_service.Constants;


public class SentenceProcessingDbMetaDataInputFactory implements SentenceProcessingMetaDataInputFactory {

	@Override
	public SentenceProcessingMetaDataInput create() {
		Query<SentenceProcessingMetaDataInput> query = getDatastore().createQuery(SentenceProcessingMetaDataInput.class);
		List<SentenceProcessingMetaDataInput> lst =  query.asList();		
		return lst.get(0);
	}
	
	private Datastore getDatastore() {
    	return Constants.MorphiaHelper.INSTANCE.getDatastore();
    }
}
