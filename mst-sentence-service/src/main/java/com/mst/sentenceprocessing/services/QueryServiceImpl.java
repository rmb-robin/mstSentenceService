package com.mst.sentenceprocessing.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mst.dao.DiscreteDataDaoImpl;
import com.mst.dao.SentenceQueryDaoImpl;
import com.mst.filter.NotAndAllRequestFactoryImpl;
import com.mst.interfaces.DiscreteDataDao;
import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.dao.SentenceQueryDao;
import com.mst.interfaces.sentenceprocessing.DiscreteDataDuplicationIdentifier;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.SentenceQuery.SentenceQueryTextInput;
import com.mst.model.discrete.DiscreteData;
import com.mst.sentenceprocessing.DiscreteDataDuplicationIdentifierImpl;
import com.mst.sentenceprocessing.interfaces.QueryService;
import com.mst.services.mst_sentence_service.SentenceServiceMongoDatastoreProvider;

public class QueryServiceImpl implements QueryService {
	private final SentenceQueryDao sentenceQueryDao;

	private final MongoDatastoreProvider mongoProvider;

	private final DiscreteDataDao discreteDataDao;
	private final DiscreteDataDuplicationIdentifier discreteDataDuplicationIdentifier;

	
	public QueryServiceImpl() {
		mongoProvider = new SentenceServiceMongoDatastoreProvider();
		sentenceQueryDao = new SentenceQueryDaoImpl();
		sentenceQueryDao.setMongoDatastoreProvider(mongoProvider);

		discreteDataDao = new DiscreteDataDaoImpl();
		discreteDataDao.setMongoDatastoreProvider(mongoProvider);

		discreteDataDuplicationIdentifier = new DiscreteDataDuplicationIdentifierImpl();

	}

	
	private void processQueryDiscreteData(List<SentenceQueryResult> results){
		Map<String,DiscreteData> discreteDatasById = new HashMap<>();
		for(SentenceQueryResult result: results) {
			if ( result.getDiscreteData() != null ) { 
				String key = result.getDiscreteData().getId().toString();
				if(discreteDatasById.containsKey(key)) continue;
				discreteDatasById.put(key, result.getDiscreteData());
			} else {
				System.out.println("Why is descrite data null");
			}
		}
		
		List<DiscreteData> discretaDatas = new ArrayList<DiscreteData>(discreteDatasById.values());
		discreteDataDuplicationIdentifier.process(discretaDatas);
	}
	
	@Override
	public List<SentenceQueryResult> querySentences(SentenceQueryInput input) throws Exception{
		if(input.getOrganizationId()==null)
			throw new Exception("Missing OrgId");
	
		if(input.getIsNotAndAll())
			input = new NotAndAllRequestFactoryImpl().create(input);
	
		List<SentenceQueryResult> results =  sentenceQueryDao.getSentences(input);
		processQueryDiscreteData(results);
		return results;
	}


	@Override
	public List<SentenceQueryResult> queryTextSentences(SentenceQueryTextInput input) throws Exception {
		return null;
		//return sentenceQueryDao.getSentencesByText(input);
	}
	



}
