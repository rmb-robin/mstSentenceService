package com.mst.sentenceprocessing.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.mst.dao.RecommendedTokenRelationshipDaoImpl;
import com.mst.dao.SentenceDiscoveryDaoImpl;
import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.SentenceProcessingMetaDataInputFactory;
import com.mst.interfaces.dao.RecommendedTokenRelationshipDao;
import com.mst.interfaces.dao.SentenceDiscoveryDao;
import com.mst.interfaces.sentenceprocessing.SentenceDiscoveryProcessor;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
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
	public void saveRecommandedTokenRelationships(List<SentenceDiscovery> sentenceDiscoveries) {
		recommendedTokenRelationshipDao.saveCollection(getAllRecommendTokenRelationships(sentenceDiscoveries));
	}

	
	private List<RecommandedTokenRelationship> getAllRecommendTokenRelationships(List<SentenceDiscovery> sentenceDiscoveries){
		List<RecommandedTokenRelationship> result = new ArrayList<>();
		
		for(SentenceDiscovery sentenceDiscovery: sentenceDiscoveries){
			result.addAll(sentenceDiscovery.getWordEmbeddings());
		}
		return result;
	}
	
	@Override
	public void processingVerification(List<SentenceDiscovery> sentenceDiscoveries) {
		List<RecommandedTokenRelationship> existing = recommendedTokenRelationshipDao.queryByKey(getkeys(sentenceDiscoveries));
		existing = filter(existing);
		for(SentenceDiscovery sentenceDiscovery: sentenceDiscoveries){
			sentenceDiscovery.setWordEmbeddings(recommendationEdgesVerificationProcesser.process(sentenceDiscovery, existing));
		}
		dao.saveSentenceDiscovieries(sentenceDiscoveries);
	}
	
	//add filter for to-to...
	
	private List<RecommandedTokenRelationship> filter(List<RecommandedTokenRelationship> existing){
		List<RecommandedTokenRelationship> result = new ArrayList<>();
		for(RecommandedTokenRelationship recommandedTokenRelationship: existing){
			if(recommandedTokenRelationship.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.defaultEdge))
				result.add(recommandedTokenRelationship);
		}
		return result;
	}
	
	
	private HashSet<String> getkeys(List<SentenceDiscovery> sentenceDiscoveries){
		HashSet<String> result = new HashSet<>();
		
		for(SentenceDiscovery sentenceDiscovery: sentenceDiscoveries){
			result.addAll(getKeyForSentenceDiscovery(sentenceDiscovery));
		}
		return result;
	}
	
	private HashSet<String> getKeyForSentenceDiscovery(SentenceDiscovery discovery){
		HashSet<String> result = new HashSet<>();
		for(int i = 0; i < discovery.getModifiedWordList().size()-1;i++){
			String from = discovery.getModifiedWordList().get(i).getToken();
			for(int j = i+1; j<discovery.getModifiedWordList().size();j++){
				String key = from + discovery.getModifiedWordList().get(j).getToken(); 
				result.add(key);
			}
		}
		return result;
	}
}
