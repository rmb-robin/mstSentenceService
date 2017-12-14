package com.mst.sentenceprocessing.dao;

import java.util.List;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.SentenceProcessingMetaDataInputFactory;
import com.mst.model.sentenceProcessing.DynamicEdgeCreationRule;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.services.mst_sentence_service.Constants;


public class SentenceProcessingDbMetaDataInputFactory implements SentenceProcessingMetaDataInputFactory {

	private MongoDatastoreProvider mongoProvider; 
	public SentenceProcessingDbMetaDataInputFactory(MongoDatastoreProvider mongoProvider){
		this.mongoProvider = mongoProvider;
	}

	@Override
	public SentenceProcessingMetaDataInput create() {
		Query<SentenceProcessingMetaDataInput> query = mongoProvider.getDefaultDb().createQuery(SentenceProcessingMetaDataInput.class);
		List<SentenceProcessingMetaDataInput> lst =  query.asList();		
		SentenceProcessingMetaDataInput meta =  lst.get(0);
		meta.setDynamicEdgeCreationRules(getRules());
		return meta;
	}
	
	private List<DynamicEdgeCreationRule> getRules(){
		Query<DynamicEdgeCreationRule> query = mongoProvider.getDefaultDb().createQuery(DynamicEdgeCreationRule.class);
		return query.asList();		
	}
}
