package com.mst.sentenceprocessing.services;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

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
import com.mst.interfaces.sentenceprocessing.RecommendedNounPhraseProcesser;
import com.mst.interfaces.sentenceprocessing.SentenceDiscoveryProcessor;
import com.mst.model.autocomplete.AutoCompleteRequest;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.requests.RecommandationRequest;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.sentenceprocessing.RecommendationEdgesVerificationProcesser;
import com.mst.sentenceprocessing.RecommendedNounPhraseProcesserImpl;
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
	private RecommendedNounPhraseProcesser nounPhraseProcesser;
	
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
		nounPhraseProcesser = new RecommendedNounPhraseProcesserImpl();
	}

	public List<SentenceDiscovery> createSentenceDiscovery(SentenceTextRequest request, SentenceProcessingMetaDataInput input) throws Exception {
		sentenceDiscoveryProcessor.setMetadata(input);
		return sentenceDiscoveryProcessor.process(request);
	}

	private void saveRecommandedTokenRelationships(List<SentenceDiscovery> sentenceDiscoveries,List<RecommendedTokenRelationship> existing, SentenceProcessingMetaDataInput input) {
		List<RecommendedTokenRelationship> allEdges = getAllRecommendTokenRelationships(sentenceDiscoveries,existing);
		nounPhraseProcesser.setNamedEdges(allEdges, input.getNounRelationshipsInput());
		recommendedTokenRelationshipDao.saveCollection(allEdges);
	}

	private List<RecommendedTokenRelationship> getAllRecommendTokenRelationships(List<SentenceDiscovery> sentenceDiscoveries, List<RecommendedTokenRelationship> existing){
		List<RecommendedTokenRelationship> result = new ArrayList<>();
		
		for(SentenceDiscovery sentenceDiscovery: sentenceDiscoveries){
			sentenceDiscovery.setWordEmbeddings(
					replaceSentenceDiscoveryRelationshipsWithExisting(sentenceDiscovery.getWordEmbeddings(), existing));
			applyTokenRelationshipLinkages(sentenceDiscovery.getWordEmbeddings());
			result.addAll(sentenceDiscovery.getWordEmbeddings());
		}
		return result;
	}
	
	private List<RecommendedTokenRelationship> replaceSentenceDiscoveryRelationshipsWithExisting
		(List<RecommendedTokenRelationship> relationships, List<RecommendedTokenRelationship> existing){
		
		List<RecommendedTokenRelationship> result = new ArrayList<RecommendedTokenRelationship>();
		Map<String,RecommendedTokenRelationship> existingMap = RecommandedTokenRelationshipUtil.getByUniqueKey(existing);
		for(RecommendedTokenRelationship recommandedTokenRelationship: relationships){
			if(existingMap.containsKey(recommandedTokenRelationship.getKey())){
					RecommendedTokenRelationship existingRelationship = existingMap.get(recommandedTokenRelationship.getKey());
					if(existingRelationship.getTokenRelationship().getEdgeName().equals(recommandedTokenRelationship.getTokenRelationship().getEdgeName())){
						result.add(existingRelationship);
						continue;
					}
			}
			result.add(recommandedTokenRelationship);
		}
		return result;
	}
	
	private void applyTokenRelationshipLinkages(List<RecommendedTokenRelationship> recommendedRelationships){
		List<TokenRelationship> tokenRelations =
				RecommandedTokenRelationshipUtil.getTokenRelationshipsFromRecommendedTokenRelationships(recommendedRelationships);
				
		for(RecommendedTokenRelationship r: recommendedRelationships){
			List<TokenRelationship> links = friendOfFriendService.getFriendOfFriendForBothTokens(tokenRelations, r.getTokenRelationship());
			if(links.size()==0)continue;
			
			for(TokenRelationship tokenRelationship: links){
				r.getTokenRelationship().getLinks().add(tokenRelationship.getUniqueIdentifier());
			}
		}
	}
	
	private void processingVerification(List<SentenceDiscovery> sentenceDiscoveries,List<RecommendedTokenRelationship> existing, DiscreteData discreteData) {
		
		List<RecommendedTokenRelationship> verified = new ArrayList<>();
		existing = filter(existing);
		for(SentenceDiscovery sentenceDiscovery: sentenceDiscoveries){
			sentenceDiscovery.setWordEmbeddings(recommendationEdgesVerificationProcesser.process(sentenceDiscovery, existing));
			verified.addAll(getVerifiedFromSentenceDiscovery(sentenceDiscovery));
		}
		dao.saveSentenceDiscoveries(sentenceDiscoveries,discreteData,null);
		updateVerifiedUniqueIds(verified);
		recommendedTokenRelationshipDao.saveCollection(verified);
		loadRecommandedTokenRelationshipIntoCach(verified);
	}

	private void updateVerifiedUniqueIds(List<RecommendedTokenRelationship> verified){
		for(RecommendedTokenRelationship r: verified){
			if(r.getTokenRelationship().getUniqueIdentifier()==null)
				r.getTokenRelationship().setUniqueIdentifier(UUID.randomUUID().toString());
		}
	}
	

	private List<RecommendedTokenRelationship> getVerifiedFromSentenceDiscovery(SentenceDiscovery discovery){
		List<RecommendedTokenRelationship> verified = new ArrayList<RecommendedTokenRelationship>();
		
		for(RecommendedTokenRelationship recommandedTokenRelationship: discovery.getWordEmbeddings()){
			if(recommandedTokenRelationship.getIsVerified())
				verified.add(recommandedTokenRelationship);
		}
		
		return verified;
		
	}

	private List<RecommendedTokenRelationship> filter(List<RecommendedTokenRelationship> existing){
		
		List<RecommendedTokenRelationship> result = new ArrayList<>();
		for(RecommendedTokenRelationship recommandedTokenRelationship: existing){
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
			result.addAll(RecommandedTokenRelationshipUtil.getKeyForSentenceDiscovery(sentenceDiscovery));
		}
		return result;
	}

	public void reloadCache() {
		List<RecommendedTokenRelationship> recommandedTokenRelationships =  this.recommendedTokenRelationshipDao.getVerified();
		loadRecommandedTokenRelationshipIntoCach(recommandedTokenRelationships);
	}

	private void loadRecommandedTokenRelationshipIntoCach(List<RecommendedTokenRelationship> recommandedTokenRelationships){
		Map<String, List<RecommendedTokenRelationship>> uniqueByToken = RecommandedTokenRelationshipUtil.getMapByDistinctToFrom(recommandedTokenRelationships);
		for(Entry<String, List<RecommendedTokenRelationship>> entry: uniqueByToken.entrySet()){
			cacheManger.reload(entry.getKey(), entry.getValue());
		}
	}
	
	@Override
	public List<String> getAutoComplete(AutoCompleteRequest autoCompleteRequest) {
		return recommendedTokenRelationshipAutocompleteQuery.getNextWord(autoCompleteRequest);
	}

	@Override
	public void saveSentenceDiscoveryProcess(SentenceTextRequest request) throws Exception {
		SentenceProcessingMetaDataInput input = sentenceProcessingDbMetaDataInputFactory.create();
		List<SentenceDiscovery> sentenceDiscoveries =  this.createSentenceDiscovery(request,input);
		List<RecommendedTokenRelationship> existing = recommendedTokenRelationshipDao.queryByKey(getkeys(sentenceDiscoveries));
		this.saveRecommandedTokenRelationships(sentenceDiscoveries,existing,input);
		this.processingVerification(sentenceDiscoveries,existing,request.getDiscreteData());
	}
}
