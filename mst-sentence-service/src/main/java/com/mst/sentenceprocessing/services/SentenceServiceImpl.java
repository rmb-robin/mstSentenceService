package com.mst.sentenceprocessing.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bson.types.ObjectId;

import com.mst.dao.DisceteDataComplianceDisplayFieldsDaoImpl;
import com.mst.dao.DiscreteDataDaoImpl;
import com.mst.dao.SentenceDaoImpl;
import com.mst.dao.SentenceQueryDaoImpl;
import com.mst.interfaces.DiscreteDataDao;
import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.sentenceprocessing.DiscreteDataBucketIdentifier;
import com.mst.interfaces.sentenceprocessing.DiscreteDataDuplicationIdentifier;
import com.mst.interfaces.sentenceprocessing.DiscreteDataNormalizer;
import com.mst.interfaces.sentenceprocessing.SentenceProcessingController;
import com.mst.interfaces.SentenceProcessingMetaDataInputFactory;
import com.mst.interfaces.dao.DisceteDataComplianceDisplayFieldsDao;
import com.mst.interfaces.dao.SentenceDao;
import com.mst.interfaces.dao.SentenceQueryDao;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.SentenceQuery.SentenceQueryTextInput;
import com.mst.model.SentenceQuery.SentenceReprocessingInput;
import com.mst.model.discrete.DisceteDataComplianceDisplayFields;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.discrete.DiscreteDataBucketIdentifierResult;
import com.mst.model.metadataTypes.DiscreteDataBucketIdenticationType;
import com.mst.model.requests.SentenceRequest;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.SentenceProcessingFailures;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.model.sentenceProcessing.SentenceProcessingResult;
import com.mst.sentenceprocessing.DiscreteDataBucketIdentifierImpl;
import com.mst.sentenceprocessing.DiscreteDataDuplicationIdentifierImpl;
import com.mst.sentenceprocessing.DiscreteDataNormalizerImpl;
import com.mst.sentenceprocessing.SentenceConverter;
import com.mst.sentenceprocessing.SentenceProcessingControllerImpl;
import com.mst.sentenceprocessing.dao.SentenceProcessingDbMetaDataInputFactory;
import com.mst.sentenceprocessing.interfaces.SentenceService;
import com.mst.services.mst_sentence_service.Constants;
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
	private DiscreteDataDao discreteDataDao;
	private DiscreteDataDuplicationIdentifier discreteDataDuplicationIdentifier;
	
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
		discreteDataDao = new DiscreteDataDaoImpl();
		discreteDataDao.setMongoDatastoreProvider(mongoProvider);
		discreteDataDuplicationIdentifier = new DiscreteDataDuplicationIdentifierImpl();
	}
	
	private void processQueryDiscreteData(List<SentenceQueryResult> results){
		Map<String,DiscreteData> discreteDatasById = new HashMap<>();
		for(SentenceQueryResult result: results){
			String key = result.getDiscreteData().getId().toString();
			if(discreteDatasById.containsKey(key)) continue;
			discreteDatasById.put(key, result.getDiscreteData());
		}
		
		List<DiscreteData> discretaDatas = new ArrayList<DiscreteData>(discreteDatasById.values());
		discreteDataDuplicationIdentifier.process(discretaDatas);
	}
	
	public List<SentenceQueryResult> querySentences(SentenceQueryInput input) throws Exception{
		if(input.getOrganizationId()==null)
			throw new Exception("Missing OrgId");
		List<SentenceQueryResult> results =  sentenceQueryDao.getSentences(input);
		processQueryDiscreteData(results);
		return results;
	}
	
	
	@Override
	public List<SentenceQueryResult> queryTextSentences(SentenceQueryTextInput input) throws Exception {
		if(input.getOrganizationId()==null)
			throw new Exception("Missing OrgId");
		List<SentenceQueryResult> results =  sentenceQueryDao.getSentencesByText(input);
		//processQueryDiscreteData(results);
		return results;
	}

	public void saveSentences(List<Sentence> sentences, DiscreteData discreteData, SentenceProcessingFailures sentenceProcessingFailures,boolean isReprocess, String reprocessId, String resultType){
		List<SentenceDb> documents = new ArrayList<SentenceDb>();
		for(Sentence sentence: sentences){
			SentenceDb document = SentenceConverter.convertToDocument(sentence);
			if(isReprocess){
				document.setId(new ObjectId(sentence.getId()));
				document.setReprocessId(reprocessId);
			}
			documents.add(document);
		}
		
		if(!isReprocess){ 
			processDiscreteData(discreteData, sentences,resultType);
			sentenceDao.saveSentences(documents, discreteData,sentenceProcessingFailures);
		}
		else
		{
			sentenceDao.saveReprocess(documents, sentenceProcessingFailures);
		}
	}
	
	private void processDiscreteData(DiscreteData discreteData, List<Sentence> sentences, String resultType){
		discreteData = discreteDataNormalizer.process(discreteData);
		
		if(discreteData.getOrganizationId()==null)return;
		DisceteDataComplianceDisplayFields fields = complianceDisplayFieldsDao.getbyOrgname(discreteData.getOrganizationId());
		if(fields==null) return; 
		DiscreteDataBucketIdentifierResult result =  bucketIdentifier.getBucket(discreteData,resultType, sentences, fields);
		if(result==null)return;
		
		discreteData.setBucketName(result.getBucketName());
		if(result.equals(DiscreteDataBucketIdenticationType.compliance))
			discreteData.setIsCompliant(result.getIsCompliant());
		else 
			discreteData.setExpectedFollowup(result.getExpectedFollowup());
	}
	
	public String reprocessSentences(SentenceReprocessingInput input) {
		controller.setMetadata(sentenceProcessingDbMetaDataInputFactory.create());
		String reprocessId = UUID.randomUUID().toString();
		input.setReprocessId(reprocessId);
		
		while(true){
			List<SentenceDb> documents = this.getSentencesForReprocessing(input);
			if(documents.isEmpty()) return null;
			Set<String> discreteDataIds =  reprocessSentenceBatch(documents,reprocessId);
			List<SentenceDb> sentencesForDiscreteData = sentenceQueryDao.getSentencesByDiscreteDataIds(discreteDataIds);
			List<Sentence> sentences = SentenceConverter.convertToSentence(sentencesForDiscreteData,true,true,false);
			Map<String, List<Sentence>> sentencesByDiscreteData = SentenceByDiscreteDataMapper.groupSentencesByDiscretedata(sentences);
			reprocessDiscreteData(sentencesByDiscreteData);
			if(documents.size()< input.getTakeSize()) break;
		}
		return reprocessId;
	}
	
	private void reprocessDiscreteData(Map<String, List<Sentence>> sentencesByDiscreteData){		
		for(Map.Entry<String,List<Sentence>> entry: sentencesByDiscreteData.entrySet()){
			List<Sentence> sentences = entry.getValue();
			if(sentences.isEmpty()) continue;
			DiscreteData discreteData = sentences.get(0).getDiscreteData();

			cleanDiscreteDataForReprocess(discreteData);
			processDiscreteData(discreteData, sentences, DiscreteDataBucketIdenticationType.compliance);
			discreteDataDao.save(discreteData, true);
		}
	}

	private Set<String> reprocessSentenceBatch(List<SentenceDb> documents, String reprocessId){

		List<Sentence> sentences = SentenceConverter.convertToSentence(documents,false,false,false);
		Set<String> discreteDataIds = new HashSet<>();
		
		for(Sentence sentence: sentences)
			discreteDataIds.add(sentence.getDiscreteData().getId().toString());
	
		SentenceProcessingResult result = controller.reprocessSentences(sentences);
		this.saveSentences(result.getSentences(),null,result.getFailures(),true,reprocessId, DiscreteDataBucketIdenticationType.compliance);
		return discreteDataIds;
	}
	
	private void cleanDiscreteDataForReprocess(DiscreteData discreteData){
		discreteData.getCustomFields().clear();
		discreteData.setBucketName(null);
		discreteData.setIsCompliant(false);
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
		return sentenceQueryDao.getSentencesForReprocess(input);
	}

	

	@Override
	public void reprocessDiscreteData(String id) {
		Set<String> discreteDataIds = new HashSet<String>();
		discreteDataIds.add(id);
		List<SentenceDb> sentencesForDiscreteData = sentenceQueryDao.getSentencesByDiscreteDataIds(discreteDataIds);
		List<Sentence> sentences = SentenceConverter.convertToSentence(sentencesForDiscreteData,true,true,false);
		Map<String, List<Sentence>> sentencesByDiscreteData = SentenceByDiscreteDataMapper.groupSentencesByDiscretedata(sentences);
		reprocessDiscreteData(sentencesByDiscreteData);
	}


}
