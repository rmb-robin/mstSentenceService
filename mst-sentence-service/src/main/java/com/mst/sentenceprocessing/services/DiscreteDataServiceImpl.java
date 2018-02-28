package com.mst.sentenceprocessing.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mst.dao.DiscreteDataDaoImpl;
import com.mst.dao.HL7ParsedRequstDaoImpl;
import com.mst.dao.Hl7DetailsDaoImpl;
import com.mst.dao.SentenceQueryDaoImpl;
import com.mst.interfaces.DiscreteDataDao;
import com.mst.interfaces.dao.SentenceQueryDao;
import com.mst.interfaces.sentenceprocessing.DiscreteDataDuplicationIdentifier;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.raw.HL7ParsedRequst;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.sentenceprocessing.DiscreteDataDuplicationIdentifierImpl;
import com.mst.sentenceprocessing.SentenceConverter;
import com.mst.sentenceprocessing.interfaces.DiscreteDataService;
import com.mst.sentenceprocessing.models.DiscreteDataRequest;
import com.mst.sentenceprocessing.models.DiscreteDataResult;
import com.mst.services.mst_sentence_service.RequestsMongoDatastoreProvider;
import com.mst.services.mst_sentence_service.SentenceServiceMongoDatastoreProvider;

public class DiscreteDataServiceImpl implements DiscreteDataService {

	private DiscreteDataDao dao;
	private SentenceQueryDao sentenceQueryDao;
	private DiscreteDataDuplicationIdentifier discreteDataDuplicationIdentifier;
	private HL7ParsedRequstDaoImpl hl7RawDao; 
	
	
	public DiscreteDataServiceImpl(){
		SentenceServiceMongoDatastoreProvider provider = new SentenceServiceMongoDatastoreProvider();
		dao = new DiscreteDataDaoImpl();
		dao.setMongoDatastoreProvider(provider);
		
		sentenceQueryDao = new SentenceQueryDaoImpl();
		sentenceQueryDao.setMongoDatastoreProvider(provider);
		discreteDataDuplicationIdentifier = new DiscreteDataDuplicationIdentifierImpl();
		hl7RawDao = new HL7ParsedRequstDaoImpl();
		hl7RawDao.setMongoDatastoreProvider(new RequestsMongoDatastoreProvider());
	}
	
	@Override
	public List<DiscreteDataResult> getDiscreteDatas(DiscreteDataRequest request) {
		List<DiscreteData> discreteDatas =  dao.getDiscreteDatas(request.getDiscreteDataFilter(), request.getOrganizationId(),true);
		discreteDataDuplicationIdentifier.process(discreteDatas);
		List<DiscreteDataResult> results = new ArrayList<>();
		for(DiscreteData discreteData: discreteDatas){
			DiscreteDataResult result = new DiscreteDataResult();
			result.setDiscreteData(discreteData);
			if(request.isIncludeSentences())
				updateWithText(result);
			results.add(result);
		}
		return results;
	}
	
	private void updateWithText(DiscreteDataResult discreteDataResult){	
		String parsedFileId = discreteDataResult.getDiscreteData().getParseReportId();
		if(parsedFileId==null) return;
		HL7ParsedRequst parsedRequest =  hl7RawDao.get(parsedFileId);
		if(parsedRequest==null)return;
		discreteDataResult.setText(parsedRequest.getText());	
	}

	@Override
	public DiscreteDataResult getById(String id) {
		DiscreteData discreteData = dao.get(id);
		DiscreteDataResult result = new DiscreteDataResult();
		result.setDiscreteData(discreteData);
		updateWithText(result);
		
		return result;
	}
	
	
	
}
 