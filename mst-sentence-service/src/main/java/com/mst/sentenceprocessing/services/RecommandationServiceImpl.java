package com.mst.sentenceprocessing.services;

import java.util.List;

import com.mst.dao.SentenceDiscoveryDaoImpl;
import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.SentenceProcessingMetaDataInputFactory;
import com.mst.interfaces.dao.SentenceDiscoveryDao;
import com.mst.interfaces.sentenceprocessing.SentenceDiscoveryProcessor;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.requests.RecommandationRequest;
import com.mst.sentenceprocessing.SentenceDiscoveryProcessorImpl;
import com.mst.sentenceprocessing.dao.SentenceProcessingDbMetaDataInputFactory;
import com.mst.sentenceprocessing.interfaces.RecommandationService;
import com.mst.services.mst_sentence_service.SentenceServiceMongoDatastoreProvider;

public class RecommandationServiceImpl implements RecommandationService {

	private SentenceDiscoveryProcessor sentenceDiscoveryProcessor; 
	private SentenceDiscoveryDao dao; 
	private SentenceProcessingMetaDataInputFactory sentenceProcessingDbMetaDataInputFactory;
	private MongoDatastoreProvider  mongoProvider; 
	public RecommandationServiceImpl() {
		sentenceDiscoveryProcessor = new SentenceDiscoveryProcessorImpl();
		mongoProvider = new SentenceServiceMongoDatastoreProvider();
		dao = new SentenceDiscoveryDaoImpl();
		dao.setMongoDatastoreProvider(mongoProvider);
		sentenceProcessingDbMetaDataInputFactory = new SentenceProcessingDbMetaDataInputFactory(mongoProvider);
	}

	@Override
	public List<SentenceDiscovery> createSentenceDiscovery(RecommandationRequest request) throws Exception {
		sentenceDiscoveryProcessor.setMetadata(sentenceProcessingDbMetaDataInputFactory.create());
		return sentenceDiscoveryProcessor.process(request);
	}

	@Override
	public void saveSentenceDiscoveries(List<SentenceDiscovery> sentenceDiscoveries) {
		dao.saveCollection(sentenceDiscoveries);
	}
}
