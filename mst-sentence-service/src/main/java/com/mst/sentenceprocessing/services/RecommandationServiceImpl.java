package com.mst.sentenceprocessing.services;

import java.util.HashSet;
import java.util.List;

import com.mst.dao.RecommendedTokenRelationshipDaoImpl;
import com.mst.dao.SentenceDiscoveryDaoImpl;
import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.SentenceProcessingMetaDataInputFactory;
import com.mst.interfaces.dao.RecommendedTokenRelationshipDao;
import com.mst.interfaces.dao.SentenceDiscoveryDao;
import com.mst.interfaces.sentenceprocessing.SentenceDiscoveryProcessor;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.requests.RecommandationRequest;
import com.mst.model.sentenceProcessing.RecommandedTokenRelationship;
import com.mst.sentenceprocessing.RecommendationEdgesVerificationProcesser;
import com.mst.sentenceprocessing.SentenceDiscoveryProcessorImpl;
import com.mst.sentenceprocessing.dao.SentenceProcessingDbMetaDataInputFactory;
import com.mst.sentenceprocessing.interfaces.RecommandationService;
import com.mst.services.mst_sentence_service.SentenceServiceMongoDatastoreProvider;

public class RecommandationServiceImpl implements RecommandationService {

	private SentenceDiscoveryProcessor sentenceDiscoveryProcessor; 
	private SentenceDiscoveryDao dao; 
	private SentenceProcessingMetaDataInputFactory sentenceProcessingDbMetaDataInputFactory;
	private MongoDatastoreProvider  mongoProvider; 
	private RecommendedTokenRelationshipDao recommendedTokenRelationshipDao; 
	private RecommendationEdgesVerificationProcesser recommendationEdgesVerificationProcesser; 
	
	public RecommandationServiceImpl() {
		sentenceDiscoveryProcessor = new SentenceDiscoveryProcessorImpl();
		mongoProvider = new SentenceServiceMongoDatastoreProvider();
		dao = new SentenceDiscoveryDaoImpl();
		dao.setMongoDatastoreProvider(mongoProvider);
		recommendedTokenRelationshipDao = new RecommendedTokenRelationshipDaoImpl();
		recommendedTokenRelationshipDao.setMongoDatastoreProvider(mongoProvider);
		sentenceProcessingDbMetaDataInputFactory = new SentenceProcessingDbMetaDataInputFactory(mongoProvider);
		recommendationEdgesVerificationProcesser =new RecommendationEdgesVerificationProcesser();
	}

	@Override
	public List<SentenceDiscovery> createSentenceDiscovery(RecommandationRequest request) throws Exception {
		sentenceDiscoveryProcessor.setMetadata(sentenceProcessingDbMetaDataInputFactory.create());
		return sentenceDiscoveryProcessor.process(request);
	}

	@Override
	public void saveSentenceDiscoveries(List<SentenceDiscovery> sentenceDiscoveries) {
		dao.saveSentenceDiscovieries(sentenceDiscoveries);
	}

	@Override
	public void processingVerification(List<SentenceDiscovery> sentenceDiscoveries) {
		List<RecommandedTokenRelationship> existing = recommendedTokenRelationshipDao.queryByKey(getkeys(sentenceDiscoveries));
		for(SentenceDiscovery sentenceDiscovery: sentenceDiscoveries){
			recommendationEdgesVerificationProcesser.process(sentenceDiscovery, existing);
		}
	}
	
	private HashSet<String> getkeys(List<SentenceDiscovery> sentenceDiscoveries){
		HashSet<String> result = new HashSet<>();
		
		for(SentenceDiscovery sentenceDiscovery: sentenceDiscoveries){
			sentenceDiscovery.getWordEmbeddings().forEach(a-> result.add(a.getKey()));
		}
		return result;
	}
}
