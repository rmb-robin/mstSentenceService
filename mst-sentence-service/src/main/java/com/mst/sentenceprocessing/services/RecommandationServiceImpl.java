package com.mst.sentenceprocessing.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mst.autocomplete.implementation.RecommendedTokenRelationshipAutocompleteQueryImpl;
import com.mst.autocomplete.interfaces.RecommendedTokenRelationshipAutocompleteQuery;
import com.mst.cache.implementation.RecommendedTokenRelationshipCacheManagerImpl;
import com.mst.cache.interfaces.RecommendedTokenRelationshipCacheManager;
import com.mst.dao.RecommendedTokenRelationshipDaoImpl;
import com.mst.dao.SentenceDiscoveryDaoImpl;
import com.mst.filter.FriendOfFriendServiceImpl;
import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.SentenceProcessingMetaDataInputFactory;
import com.mst.interfaces.dao.RecommendedTokenRelationshipDao;
import com.mst.interfaces.dao.SentenceDiscoveryDao;
import com.mst.interfaces.filter.FriendOfFriendService;
import com.mst.interfaces.sentenceprocessing.SentenceDiscoveryProcessor;
import com.mst.model.autocomplete.AutoCompleteRequest;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.RecommandedTokenRelationship;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.requests.RecommandationRequest;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.sentenceprocessing.RecommendationEdgesVerificationProcesser;
import com.mst.sentenceprocessing.SentenceDiscoveryProcessorImpl;
import com.mst.sentenceprocessing.dao.SentenceProcessingDbMetaDataInputFactory;
import com.mst.sentenceprocessing.interfaces.RecommandationService;
import com.mst.services.mst_sentence_service.SentenceServiceMongoDatastoreProvider;
import com.mst.util.RecommandedTokenRelationshipUtil;

public class RecommandationServiceImpl implements RecommandationService {

	private SentenceDiscoveryProcessor sentenceDiscoveryProcessor; 
	private SentenceDiscoveryDao dao; 
	private SentenceProcessingMetaDataInputFactory sentenceProcessingDbMetaDataInputFactory;
	private MongoDatastoreProvider  mongoProvider; 
	private RecommendedTokenRelationshipDao recommendedTokenRelationshipDao; 
	private RecommendationEdgesVerificationProcesser recommendationEdgesVerificationProcesser; 
	private RecommendedTokenRelationshipCacheManager cacheManger; 
	private RecommendedTokenRelationshipAutocompleteQuery recommendedTokenRelationshipAutocompleteQuery;
	private FriendOfFriendService friendOfFriendService;
	public RecommandationServiceImpl() {
		sentenceDiscoveryProcessor = new SentenceDiscoveryProcessorImpl();
		mongoProvider = new SentenceServiceMongoDatastoreProvider();
		dao = new SentenceDiscoveryDaoImpl();
		dao.setMongoDatastoreProvider(mongoProvider);
		recommendedTokenRelationshipDao = new RecommendedTokenRelationshipDaoImpl();
		recommendedTokenRelationshipDao.setMongoDatastoreProvider(mongoProvider);
		sentenceProcessingDbMetaDataInputFactory = new SentenceProcessingDbMetaDataInputFactory(mongoProvider);
		recommendationEdgesVerificationProcesser =new RecommendationEdgesVerificationProcesser();
		cacheManger = new RecommendedTokenRelationshipCacheManagerImpl();
		recommendedTokenRelationshipAutocompleteQuery = new RecommendedTokenRelationshipAutocompleteQueryImpl();
		friendOfFriendService = new FriendOfFriendServiceImpl();
	}

	public List<SentenceDiscovery> createSentenceDiscovery(RecommandationRequest request) throws Exception {
		sentenceDiscoveryProcessor.setMetadata(sentenceProcessingDbMetaDataInputFactory.create());
		return sentenceDiscoveryProcessor.process(request);
	}

	
	private void saveRecommandedTokenRelationships(List<SentenceDiscovery> sentenceDiscoveries,List<RecommandedTokenRelationship> existing) {
		recommendedTokenRelationshipDao.saveCollection(getAllRecommendTokenRelationships(sentenceDiscoveries,existing));
	}

	
	private List<RecommandedTokenRelationship> getAllRecommendTokenRelationships(List<SentenceDiscovery> sentenceDiscoveries, List<RecommandedTokenRelationship> existing){
		List<RecommandedTokenRelationship> result = new ArrayList<>();
		
		for(SentenceDiscovery sentenceDiscovery: sentenceDiscoveries){
			sentenceDiscovery.setWordEmbeddings(
					replaceSentenceDiscoveryRelationshipsWithExisting(sentenceDiscovery.getWordEmbeddings(), existing));
			applyTokenRelationshipLinkages(sentenceDiscovery.getWordEmbeddings());
			result.addAll(sentenceDiscovery.getWordEmbeddings());
		}
		return result;
	}
	
	private List<RecommandedTokenRelationship> replaceSentenceDiscoveryRelationshipsWithExisting
		(List<RecommandedTokenRelationship> relationships, List<RecommandedTokenRelationship> existing){
		
		List<RecommandedTokenRelationship> result = new ArrayList<RecommandedTokenRelationship>();
		Map<String,RecommandedTokenRelationship> existingMap = RecommandedTokenRelationshipUtil.getByUniqueKey(existing);
		for(RecommandedTokenRelationship recommandedTokenRelationship: relationships){
			if(existingMap.containsKey(recommandedTokenRelationship.getKey())){
					result.add(existingMap.get(recommandedTokenRelationship.getKey()));
					continue;
			}
			result.add(recommandedTokenRelationship);
		}
		return result;
	}
	
	private void applyTokenRelationshipLinkages(List<RecommandedTokenRelationship> recommendedRelationships){
		List<TokenRelationship> tokenRelations =
				RecommandedTokenRelationshipUtil.getTokenRelationshipsFromRecommendedTokenRelationships(recommendedRelationships);
				
		for(RecommandedTokenRelationship r: recommendedRelationships){
			List<TokenRelationship> links = friendOfFriendService.getFriendOfFriendForBothTokens(tokenRelations, r.getTokenRelationship());
			if(links.size()==0)continue;
			
			for(TokenRelationship tokenRelationship: links){
				r.getTokenRelationship().getLinks().add(tokenRelationship.getUniqueIdentifier());
			}
		}
	}
	
	private void processingVerification(List<SentenceDiscovery> sentenceDiscoveries,List<RecommandedTokenRelationship> existing) {
		
		List<RecommandedTokenRelationship> verified = new ArrayList<>();
		existing = filter(existing);
		for(SentenceDiscovery sentenceDiscovery: sentenceDiscoveries){
			sentenceDiscovery.setWordEmbeddings(recommendationEdgesVerificationProcesser.process(sentenceDiscovery, existing));
			verified.addAll(getVerifiedFromSentenceDiscovery(sentenceDiscovery));
		}
		dao.saveSentenceDiscovieries(sentenceDiscoveries);
		recommendedTokenRelationshipDao.saveCollection(verified);
		loadRecommandedTokenRelationshipIntoCach(verified);
	}
	

	private List<RecommandedTokenRelationship> getVerifiedFromSentenceDiscovery(SentenceDiscovery discovery){
		List<RecommandedTokenRelationship> verified = new ArrayList<RecommandedTokenRelationship>();
		
		for(RecommandedTokenRelationship recommandedTokenRelationship: discovery.getWordEmbeddings()){
			if(recommandedTokenRelationship.getIsVerified())
				verified.add(recommandedTokenRelationship);
		}
		
		return verified;
		
	}

	private List<RecommandedTokenRelationship> filter(List<RecommandedTokenRelationship> existing){
		
		List<RecommandedTokenRelationship> result = new ArrayList<>();
		for(RecommandedTokenRelationship recommandedTokenRelationship: existing){
			String edgeName = recommandedTokenRelationship.getTokenRelationship().getEdgeName();
			if(edgeName.equals(WordEmbeddingTypes.defaultEdge) || edgeName.equals(WordEmbeddingTypes.secondPrep) 
					|| edgeName.equals(WordEmbeddingTypes.secondVerb) || edgeName.equals(WordEmbeddingTypes.firstPrep) || edgeName.equals(WordEmbeddingTypes.firstVerb) )
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

	public void reloadCache() {
		List<RecommandedTokenRelationship> recommandedTokenRelationships =  this.recommendedTokenRelationshipDao.getVerified();
		loadRecommandedTokenRelationshipIntoCach(recommandedTokenRelationships);
	}

	private void loadRecommandedTokenRelationshipIntoCach(List<RecommandedTokenRelationship> recommandedTokenRelationships){
		Map<String, List<RecommandedTokenRelationship>> uniqueByToken = RecommandedTokenRelationshipUtil.getMapByDistinctToFrom(recommandedTokenRelationships);
		for(Entry<String, List<RecommandedTokenRelationship>> entry: uniqueByToken.entrySet()){
			cacheManger.reload(entry.getKey(), entry.getValue());
		}
	}
	
	@Override
	public List<String> getAutoComplete(AutoCompleteRequest autoCompleteRequest) {
		return recommendedTokenRelationshipAutocompleteQuery.getNextWord(autoCompleteRequest);
	}

	@Override
	public void saveSentenceDiscoveryProcess(RecommandationRequest request) throws Exception {
		List<SentenceDiscovery> sentenceDiscoveries =  this.createSentenceDiscovery(request);
		List<RecommandedTokenRelationship> existing = recommendedTokenRelationshipDao.queryByKey(getkeys(sentenceDiscoveries));
		this.saveRecommandedTokenRelationships(sentenceDiscoveries,existing);
		this.processingVerification(sentenceDiscoveries,existing);
	}
}
