package com.mst.sentenceprocessing.services;


import java.util.ArrayList;
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
import com.mst.interfaces.sentenceprocessing.RecommendedNounPhraseProcessor;
import com.mst.interfaces.sentenceprocessing.SentenceDiscoveryProcessor;
import com.mst.model.autocomplete.AutoCompleteRequest;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.sentenceprocessing.RecommendationEdgesVerificationProcessor;
import com.mst.sentenceprocessing.RecommendedNounPhraseProcessorImpl;
import com.mst.sentenceprocessing.SentenceDiscoveryProcessorImpl;
import com.mst.sentenceprocessing.dao.SentenceProcessingDbMetaDataInputFactory;
import com.mst.sentenceprocessing.interfaces.RecommendationService;
import com.mst.services.mst_sentence_service.SentenceServiceMongoDatastoreProvider;
import com.mst.util.RecommandedTokenRelationshipUtil;

public class RecommendationServiceImpl implements RecommendationService {
	private SentenceDiscoveryProcessor sentenceDiscoveryProcessor; 
	private SentenceDiscoveryDao dao; 
	private SentenceProcessingMetaDataInputFactory sentenceProcessingDbMetaDataInputFactory;
	private RecommendedTokenRelationshipDao recommendedTokenRelationshipDao;
	private RecommendationEdgesVerificationProcessor recommendationEdgesVerificationProcesser;
	private RecommendedTokenRelationshipCacheManager cacheManger; 
	private RecommendedTokenRelationshipAutocompleteQuery recommendedTokenRelationshipAutocompleteQuery;
	private FriendOfFriendService friendOfFriendService;
	private RecommendedNounPhraseProcessor nounPhraseProcessor;
	private static SentenceProcessingMetaDataInput input; 
	
	public RecommendationServiceImpl() {
		sentenceDiscoveryProcessor = new SentenceDiscoveryProcessorImpl();
		MongoDatastoreProvider mongoProvider = new SentenceServiceMongoDatastoreProvider();
		dao = new SentenceDiscoveryDaoImpl();
		dao.setMongoDatastoreProvider(mongoProvider);
		recommendedTokenRelationshipDao = new RecommendedTokenRelationshipDaoImpl();
		recommendedTokenRelationshipDao.setMongoDatastoreProvider(mongoProvider);
		sentenceProcessingDbMetaDataInputFactory = new SentenceProcessingDbMetaDataInputFactory(mongoProvider);
		recommendationEdgesVerificationProcesser =new RecommendationEdgesVerificationProcessor();
		cacheManger = new RecommendedTokenRelationshipCacheManagerImpl();
		recommendedTokenRelationshipAutocompleteQuery = new RecommendedTokenRelationshipAutocompleteQueryImpl();
		friendOfFriendService = new FriendOfFriendServiceImpl();
		nounPhraseProcessor = new RecommendedNounPhraseProcessorImpl();
	}

	@Override
	public List<String> getAutoComplete(AutoCompleteRequest autoCompleteRequest) {
		return recommendedTokenRelationshipAutocompleteQuery.getNextWord(autoCompleteRequest);
	}

	@Override
	public void saveSentenceDiscoveryProcess(SentenceTextRequest request) throws Exception {
		if(input == null)
			input = sentenceProcessingDbMetaDataInputFactory.create();
		List<SentenceDiscovery> sentenceDiscoveries =  this.createSentenceDiscovery(request,input);
		List<RecommendedTokenRelationship> existing = recommendedTokenRelationshipDao.queryByKey(getKeys(sentenceDiscoveries));
		this.saveRecommandedTokenRelationships(sentenceDiscoveries,existing,input);
		this.processingVerification(sentenceDiscoveries,existing,request.getDiscreteData());
	}

	private List<SentenceDiscovery> createSentenceDiscovery(SentenceTextRequest request, SentenceProcessingMetaDataInput input) throws Exception {
		sentenceDiscoveryProcessor.setMetadata(input);
		return sentenceDiscoveryProcessor.process(request);
	}

	private void saveRecommandedTokenRelationships(List<SentenceDiscovery> sentenceDiscoveries,List<RecommendedTokenRelationship> existing, SentenceProcessingMetaDataInput input) {
		List<RecommendedTokenRelationship> allEdges = getAllRecommendTokenRelationships(sentenceDiscoveries,existing);
		nounPhraseProcessor.setNamedEdges(allEdges, input.getNounRelationshipsInput());
		recommendedTokenRelationshipDao.saveCollection(allEdges);
	}

	private List<RecommendedTokenRelationship> getAllRecommendTokenRelationships(List<SentenceDiscovery> sentenceDiscoveries, List<RecommendedTokenRelationship> existing){
		List<RecommendedTokenRelationship> result = new ArrayList<>();
		for(SentenceDiscovery sentenceDiscovery: sentenceDiscoveries){
			sentenceDiscovery.setWordEmbeddings(replaceSentenceDiscoveryRelationshipsWithExisting(sentenceDiscovery.getWordEmbeddings(), existing));
			applyTokenRelationshipLinkages(sentenceDiscovery.getWordEmbeddings());
			result.addAll(sentenceDiscovery.getWordEmbeddings());
		}
		return result;
	}
	
	private List<RecommendedTokenRelationship> replaceSentenceDiscoveryRelationshipsWithExisting
		(List<RecommendedTokenRelationship> relationships, List<RecommendedTokenRelationship> existing){
		List<RecommendedTokenRelationship> result = new ArrayList<>();
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
		List<TokenRelationship> tokenRelations = RecommandedTokenRelationshipUtil.getTokenRelationshipsFromRecommendedTokenRelationships(recommendedRelationships);
		for(RecommendedTokenRelationship r: recommendedRelationships){
			List<TokenRelationship> links = friendOfFriendService.getFriendOfFriendForBothTokens(tokenRelations, r.getTokenRelationship());
			if(links.size()==0)
				continue;
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
		loadRecommendedTokenRelationshipIntoCach(verified);
	}

	private void updateVerifiedUniqueIds(List<RecommendedTokenRelationship> verified){
		for(RecommendedTokenRelationship r: verified){
			if(r.getTokenRelationship().getUniqueIdentifier()==null)
				r.getTokenRelationship().setUniqueIdentifier(UUID.randomUUID().toString());
		}
	}

	private List<RecommendedTokenRelationship> getVerifiedFromSentenceDiscovery(SentenceDiscovery discovery){
		List<RecommendedTokenRelationship> verified = new ArrayList<>();
		for(RecommendedTokenRelationship recommandedTokenRelationship: discovery.getWordEmbeddings()){
			if(recommandedTokenRelationship.getIsVerified())
				verified.add(recommandedTokenRelationship);
		}
		return verified;
	}

	private List<RecommendedTokenRelationship> filter(List<RecommendedTokenRelationship> existing){
		List<RecommendedTokenRelationship> result = new ArrayList<>();
		for(RecommendedTokenRelationship recommendedTokenRelationship: existing){
			String edgeName = recommendedTokenRelationship.getTokenRelationship().getEdgeName();
			if(edgeName.equals(WordEmbeddingTypes.tokenToken) || edgeName.equals(WordEmbeddingTypes.prepMinus) || edgeName.equals(WordEmbeddingTypes.verbMinus) || edgeName.equals(WordEmbeddingTypes.prepPlus) || edgeName.equals(WordEmbeddingTypes.verbPlus) )
				result.add(recommendedTokenRelationship);
		}
		return result;
	}

	private HashSet<String> getKeys(List<SentenceDiscovery> sentenceDiscoveries){
		HashSet<String> result = new HashSet<>();
		for(SentenceDiscovery sentenceDiscovery: sentenceDiscoveries){
			result.addAll(RecommandedTokenRelationshipUtil.getKeyForSentenceDiscovery(sentenceDiscovery));
		}
		return result;
	}

	public void reloadCache() {
		List<RecommendedTokenRelationship> recommendedTokenRelationships = recommendedTokenRelationshipDao.getVerified();
		loadRecommendedTokenRelationshipIntoCach(recommendedTokenRelationships);
	}

	private void loadRecommendedTokenRelationshipIntoCach(List<RecommendedTokenRelationship> recommendedTokenRelationships){
		Map<String, List<RecommendedTokenRelationship>> uniqueByToken = RecommandedTokenRelationshipUtil.getMapByDistinctToFrom(recommendedTokenRelationships);
		for(Entry<String, List<RecommendedTokenRelationship>> entry: uniqueByToken.entrySet()){
			cacheManger.reload(entry.getKey(), entry.getValue());
		}
	}
}
