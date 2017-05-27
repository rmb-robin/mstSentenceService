package com.mst.sentenceprocessing.services;

import java.util.ArrayList;
import java.util.List;

import com.mst.dao.DisceteDataComplianceDisplayFieldsDaoImpl;
import com.mst.dao.SentenceDaoImpl;
import com.mst.dao.SentenceQueryDaoImpl;
import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.sentenceprocessing.DiscreteDataBucketIdentifier;
import com.mst.interfaces.sentenceprocessing.DiscreteDataNormalizer;
import com.mst.interfaces.sentenceprocessing.SentenceProcessingController;
import com.mst.interfaces.SentenceProcessingMetaDataInputFactory;
import com.mst.interfaces.dao.DisceteDataComplianceDisplayFieldsDao;
import com.mst.interfaces.dao.SentenceDao;
import com.mst.interfaces.dao.SentenceQueryDao;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.discrete.DisceteDataComplianceDisplayFields;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.requests.SentenceRequest;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.sentenceprocessing.DiscreteDataBucketIdentifierImpl;
import com.mst.sentenceprocessing.DiscreteDataNormalizerImpl;
import com.mst.sentenceprocessing.SentenceConverter;
import com.mst.sentenceprocessing.SentenceProcessingControllerImpl;
import com.mst.sentenceprocessing.dao.SentenceProcessingDbMetaDataInputFactory;
import com.mst.sentenceprocessing.interfaces.SentenceService;
import com.mst.services.mst_sentence_service.SentenceServiceMongoDatastoreProvider;

public class SentenceServiceImpl implements SentenceService {

	private SentenceQueryDao sentenceQueryDao; 
	private SentenceDao sentenceDao;
	private MongoDatastoreProvider  mongoProvider; 
	private SentenceProcessingMetaDataInputFactory sentenceProcessingDbMetaDataInputFactory;
	private SentenceProcessingController controller; 
	private DiscreteDataNormalizer discreteDataNormalizer; 
	private DiscreteDataBucketIdentifier bucketIdentifier;
	private DisceteDataComplianceDisplayFieldsDao complianceDisplayFieldsDao;
	
	
	public SentenceServiceImpl(){
		mongoProvider = new SentenceServiceMongoDatastoreProvider();
		sentenceQueryDao = new SentenceQueryDaoImpl();
		sentenceQueryDao.setMongoDatastoreProvider(mongoProvider);
		sentenceDao = new SentenceDaoImpl();
		sentenceDao.setMongoDatastoreProvider(mongoProvider);
		sentenceProcessingDbMetaDataInputFactory = new SentenceProcessingDbMetaDataInputFactory(mongoProvider);
		controller = new SentenceProcessingControllerImpl();
		discreteDataNormalizer = new DiscreteDataNormalizerImpl();
		bucketIdentifier = new DiscreteDataBucketIdentifierImpl();
		complianceDisplayFieldsDao = new DisceteDataComplianceDisplayFieldsDaoImpl();
		complianceDisplayFieldsDao.setMongoDatastoreProvider(mongoProvider);
	}
	
	public List<SentenceQueryResult> querySentences(SentenceQueryInput input){
		return sentenceQueryDao.getSentences(input);
	}

	public void saveSentences(List<Sentence> sentences, DiscreteData discreteData){
		List<SentenceDb> documents = new ArrayList<SentenceDb>();
		for(Sentence sentence: sentences){
			documents.add(SentenceConverter.convertToDocument(sentence));
		}
		
		discreteData = discreteDataNormalizer.process(discreteData);
		DisceteDataComplianceDisplayFields fields = complianceDisplayFieldsDao.getbyOrgname(discreteData.getOrganizationName());
		discreteData.setBucketName(bucketIdentifier.getBucket(discreteData, sentences, fields));
		sentenceDao.saveSentences(documents, discreteData);
	}
	
	public List<Sentence> createSentences(SentenceRequest request) throws Exception{
    	controller.setMetadata(sentenceProcessingDbMetaDataInputFactory.create());
    	return controller.processSentences(request);
	}
	
	public SentenceProcessingMetaDataInput getSentenceProcessingMetadata(){
		return sentenceProcessingDbMetaDataInputFactory.create();
	}

	public List<Sentence> createSentences(SentenceTextRequest request) throws Exception {
		controller.setMetadata(sentenceProcessingDbMetaDataInputFactory.create());
		return controller.processText(request);
	}

	
	public List<String> getEdgeNamesForTokens(List<String> tokens) {
		return sentenceQueryDao.getEdgeNamesByTokens(tokens);
	}
}
