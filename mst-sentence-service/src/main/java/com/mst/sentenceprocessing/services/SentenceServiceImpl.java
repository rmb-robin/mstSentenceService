package com.mst.sentenceprocessing.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.mst.model.SentenceQuery.SentenceReprocessingInput;
import com.mst.model.discrete.DisceteDataComplianceDisplayFields;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.discrete.DiscreteDataBucketIdentifierResult;
import com.mst.model.requests.SentenceRequest;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.SentenceProcessingFailures;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.model.sentenceProcessing.SentenceProcessingResult;
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
	
	public List<SentenceQueryResult> querySentences(SentenceQueryInput input) throws Exception{
		if(input.getOrganizationId()==null)
			throw new Exception("Missing OrgId");
		return sentenceQueryDao.getSentences(input);
	}

	public void saveSentences(List<Sentence> sentences, DiscreteData discreteData, SentenceProcessingFailures sentenceProcessingFailures){
		List<SentenceDb> documents = new ArrayList<SentenceDb>();
		for(Sentence sentence: sentences){
			documents.add(SentenceConverter.convertToDocument(sentence));
		}
		
		discreteData = discreteDataNormalizer.process(discreteData);
		if(discreteData.getOrganizationId()!=null){
			DisceteDataComplianceDisplayFields fields = complianceDisplayFieldsDao.getbyOrgname(discreteData.getOrganizationId());
			if(fields!=null){
				DiscreteDataBucketIdentifierResult result =  bucketIdentifier.getBucket(discreteData, sentences, fields);
				if(result!=null){
					discreteData.setBucketName(result.getBucketName());
					discreteData.setIsCompliant(result.getIsCompliant());
				}
			}	
		}
		sentenceDao.saveSentences(documents, discreteData,sentenceProcessingFailures);
	}
	
	public void reprocessSentences(List<SentenceDb> sentenceDb) {
		controller.setMetadata(sentenceProcessingDbMetaDataInputFactory.create());
		List<Sentence> sentences = SentenceConverter.convertToSentence(sentenceDb);
    	for(Map.Entry<String, List<Sentence>> entry : groupSentencesByDiscretedata(sentences).entrySet()) {
    		SentenceProcessingResult result = controller.reprocessSentences(entry.getValue());
    		this.saveSentences(result.getSentences(),entry.getValue().get(0).getDiscreteData(),result.getFailures());
		}	
	}
	
	private Map<String, List<Sentence>>groupSentencesByDiscretedata(List<Sentence> sentences){
		Map<String, List<Sentence>> sentencesByDiscreteDataId = new HashMap<>();
		
		for(Sentence sentence: sentences){
			String key = sentence.getDiscreteData().getId().toString();
			if(!sentencesByDiscreteDataId.containsKey(key));
					sentencesByDiscreteDataId.put(key, new ArrayList<Sentence>());
			sentencesByDiscreteDataId.get(key).add(sentence);
		}
		return sentencesByDiscreteDataId;
	}

	public List<Sentence> createSentences(SentenceRequest request) throws Exception{
    	controller.setMetadata(sentenceProcessingDbMetaDataInputFactory.create());
    	return controller.processSentences(request);
	}
	
	public SentenceProcessingMetaDataInput getSentenceProcessingMetadata(){
		return sentenceProcessingDbMetaDataInputFactory.create();
	}

	public SentenceProcessingResult createSentences(SentenceTextRequest request) throws Exception {
		controller.setMetadata(sentenceProcessingDbMetaDataInputFactory.create());
		return controller.processText(request);
	}

	
	public List<String> getEdgeNamesForTokens(List<String> tokens) {
		return sentenceQueryDao.getEdgeNamesByTokens(tokens);
	}

	@Override
	public List<SentenceDb> getSentencesForReprocessing(SentenceReprocessingInput input) {
		return sentenceQueryDao.getSentencesByToken(input);
	}
}
