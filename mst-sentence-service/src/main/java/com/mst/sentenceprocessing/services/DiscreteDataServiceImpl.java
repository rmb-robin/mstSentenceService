package com.mst.sentenceprocessing.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mst.dao.DiscreteDataDaoImpl;
import com.mst.dao.SentenceQueryDaoImpl;
import com.mst.dao.SentenceQueryResultDisplayFieldsDaoImpl;
import com.mst.interfaces.DiscreteDataDao;
import com.mst.interfaces.dao.SentenceQueryDao;
import com.mst.interfaces.dao.SentenceQueryResultDisplayFieldsDao;
import com.mst.interfaces.sentenceprocessing.DiscreteDataDuplicationIdentifier;
import com.mst.model.SentenceQuery.SentenceQueryResultDisplayFields;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.sentenceprocessing.DiscreteDataDuplicationIdentifierImpl;
import com.mst.sentenceprocessing.SentenceConverter;
import com.mst.sentenceprocessing.interfaces.DiscreteDataService;
import com.mst.sentenceprocessing.models.DiscreteDataRequest;
import com.mst.sentenceprocessing.models.DiscreteDataResult;
import com.mst.services.mst_sentence_service.SentenceServiceMongoDatastoreProvider;

public class DiscreteDataServiceImpl implements DiscreteDataService {

	private DiscreteDataDao dao;
	private SentenceQueryDao sentenceQueryDao;
	private DiscreteDataDuplicationIdentifier discreteDataDuplicationIdentifier;
	private SentenceQueryResultDisplayFieldsDao sentenceQueryResultDisplayFieldsdao; 
	public DiscreteDataServiceImpl(){
		SentenceServiceMongoDatastoreProvider provider = new SentenceServiceMongoDatastoreProvider();
		dao = new DiscreteDataDaoImpl();
		dao.setMongoDatastoreProvider(provider);
		
		sentenceQueryDao = new SentenceQueryDaoImpl();
		sentenceQueryDao.setMongoDatastoreProvider(provider);
		sentenceQueryResultDisplayFieldsdao = new SentenceQueryResultDisplayFieldsDaoImpl();
		sentenceQueryResultDisplayFieldsdao.setMongoDatastoreProvider(provider);
		discreteDataDuplicationIdentifier = new DiscreteDataDuplicationIdentifierImpl();
	}
	
	@Override
	public List<DiscreteDataResult> getDiscreteDatas(DiscreteDataRequest request) {
		List<DiscreteData> discreteDatas =  dao.getDiscreteDatas(request.getDiscreteDataFilter(), request.getOrganizationId(),true);
		discreteDataDuplicationIdentifier.process(discreteDatas);
		List<DiscreteDataResult> results = new ArrayList<>();
		for(DiscreteData discreteData: discreteDatas){
			DiscreteDataResult result = new DiscreteDataResult();
			result.setDiscreteData(discreteData);
			results.add(result);
		}
		
		if(!request.isIncludeSentences()) return results;
		updateWithSentences(results);
		return results;
	}
	
	private void updateWithSentences(List<DiscreteDataResult> discreteDataResults){
		Set<String> discreteDataIds = new HashSet<>();
		discreteDataResults.forEach(a-> discreteDataIds.add(a.getDiscreteData().getId().toString()));
		
		for(DiscreteDataResult result: discreteDataResults){
			List<SentenceDb> sentencedbs = sentenceQueryDao.getSentencesForDiscreteDataId(result.getDiscreteData().getId().toString());
			sentencedbs.forEach(a-> result.getSentences().add(a.getOrigSentence()));
		}	
	}

	private List<String> getSentenceText(List<Sentence> sentences){
		List<String> result = new ArrayList<>();
		sentences.forEach(a-> result.add(a.getOrigSentence()));
		return result;
	}
	
	@Override
	public DiscreteDataResult getById(String id) {
		DiscreteData discreteData = dao.get(id);
		DiscreteDataResult result = new DiscreteDataResult();
		result.setDiscreteData(discreteData);
		List<SentenceDb> documents = sentenceQueryDao.getSentencesForDiscreteDataId(id);
		
		if(documents==null || documents.isEmpty()) return result;
		documents.forEach(a-> result.getSentences().add(a.getOrigSentence()));
		return result;
	}

	@Override
	public void saveSentenceQueryResultDisplayFields(SentenceQueryResultDisplayFields fields) {
		sentenceQueryResultDisplayFieldsdao.save(fields);
	}

	@Override
	public SentenceQueryResultDisplayFields getSentenceQueryResultByOrgId(String orgId) {
		return sentenceQueryResultDisplayFieldsdao.getByOrgId(orgId);
	}
}
 