package com.mst.sentenceprocessing.services;

import java.util.ArrayList;
import java.util.List;

import org.mongodb.morphia.Datastore;

import com.mst.dao.SentenceDaoImpl;
import com.mst.dao.SentenceQueryDaoImpl;
import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.sentenceprocessing.SentenceProcessingController;
import com.mst.interfaces.SentenceProcessingMetaDataInputFactory;
import com.mst.interfaces.dao.SentenceDao;
import com.mst.interfaces.dao.SentenceQueryDao;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.sentenceprocessing.SentenceConverter;
import com.mst.sentenceprocessing.SentenceProcessingControllerImpl;
import com.mst.sentenceprocessing.dao.SentenceProcessingDbMetaDataInputFactory;
import com.mst.sentenceprocessing.interfaces.SentenceService;
import com.mst.services.mst_sentence_service.Constants;
import com.mst.services.mst_sentence_service.SentenceRequest;
import com.mst.services.mst_sentence_service.SentenceServiceMongoDatastoreProvider;

public class SentenceServiceImpl implements SentenceService {

	private SentenceQueryDao sentenceQueryDao; 
	private SentenceDao sentenceDao;
	private MongoDatastoreProvider  mongoProvider; 
	private SentenceProcessingMetaDataInputFactory sentenceProcessingDbMetaDataInputFactory;
	private SentenceProcessingController controller; 
	
	public SentenceServiceImpl(){
		mongoProvider = new SentenceServiceMongoDatastoreProvider();
		sentenceQueryDao = new SentenceQueryDaoImpl();
		sentenceQueryDao.setMongoDatastoreProvider(mongoProvider);
		sentenceDao = new SentenceDaoImpl();
		sentenceDao.setMongoDatastoreProvider(mongoProvider);
		sentenceProcessingDbMetaDataInputFactory = new SentenceProcessingDbMetaDataInputFactory(mongoProvider);
		controller = new SentenceProcessingControllerImpl();
	}
	
	public List<SentenceQueryResult> querySentences(SentenceQueryInput input){
		return sentenceQueryDao.getSentences(input);
	}

	public void saveSentences(List<Sentence> sentences){
		List<SentenceDb> documents = new ArrayList<SentenceDb>();
		for(Sentence sentence: sentences){
			documents.add(SentenceConverter.convertToDocument(sentence));
		}
		sentenceDao.saveSentences(documents);
	}
	
	public List<Sentence> createSentences(SentenceRequest request) throws Exception{
    	controller.setMetadata(sentenceProcessingDbMetaDataInputFactory.create());
    	return controller.processSentences(request.getSenteceTexts());
	}
	
	public SentenceProcessingMetaDataInput getSentenceProcessingMetadata(){
		return sentenceProcessingDbMetaDataInputFactory.create();
	}

	public List<Sentence> createSentences(com.mst.model.sentenceProcessing.TextInput textInput) throws Exception {
		controller.setMetadata(sentenceProcessingDbMetaDataInputFactory.create());
		return controller.processText(textInput);
	}
}
