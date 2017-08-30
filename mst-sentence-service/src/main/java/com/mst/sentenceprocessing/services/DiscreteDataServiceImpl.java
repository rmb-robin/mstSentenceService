package com.mst.sentenceprocessing.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mst.dao.DiscreteDataDaoImpl;
import com.mst.dao.SentenceQueryDaoImpl;
import com.mst.interfaces.DiscreteDataDao;
import com.mst.interfaces.dao.SentenceQueryDao;
import com.mst.interfaces.sentenceprocessing.DiscreteDataDuplicationIdentifier;
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
	
	public DiscreteDataServiceImpl(){
		SentenceServiceMongoDatastoreProvider provider = new SentenceServiceMongoDatastoreProvider();
		dao = new DiscreteDataDaoImpl();
		dao.setMongoDatastoreProvider(provider);
		
		sentenceQueryDao = new SentenceQueryDaoImpl();
		sentenceQueryDao.setMongoDatastoreProvider(provider);
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
		List<SentenceDb> documents = sentenceQueryDao.getSentencesByDiscreteDataIds(discreteDataIds);
		List<Sentence> sentences = SentenceConverter.convertToSentence(documents,true,true,true);
		
		Map<String,List<Sentence>> sentencesByDiscreteDataId = SentenceByDiscreteDataMapper.groupSentencesByDiscretedata(sentences);
		
		for(DiscreteDataResult result: discreteDataResults){
			String key = result.getDiscreteData().getId().toString();
			if(sentencesByDiscreteDataId.containsKey(key)){
				List<Sentence> matchedSentences = sentencesByDiscreteDataId.get(key);
				result.setSentences(getSentenceText(matchedSentences));
			}
		}	
	}

	private List<String> getSentenceText(List<Sentence> sentences){
		List<String> result = new ArrayList<>();
		sentences.forEach(a-> result.add(a.getOrigSentence()));
		return result;
	}
	
	@Override
	public DiscreteDataResult getById(String id) {
		Set<String> ids = new HashSet<String>();
		ids.add(id);
		List<DiscreteData> discreteDatas = dao.getByIds(ids);
		if(discreteDatas==null || discreteDatas.isEmpty())return null;
		DiscreteData discreteData = discreteDatas.get(0);
		DiscreteDataResult result = new DiscreteDataResult();
		result.setDiscreteData(discreteData);
		List<SentenceDb> documents = sentenceQueryDao.getSentencesByDiscreteDataIds(ids);
		
		if(documents==null || documents.isEmpty()) return result;
		documents.forEach(a-> result.getSentences().add(a.getOrigSentence()));
		return result;
	}
	
	
	
}
 