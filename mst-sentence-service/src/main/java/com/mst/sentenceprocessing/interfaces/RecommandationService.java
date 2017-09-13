package com.mst.sentenceprocessing.interfaces;

import java.util.List;

import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.requests.RecommandationRequest;
import com.mst.model.sentenceProcessing.Sentence;

public interface RecommandationService {

	List<SentenceDiscovery> createSentenceDiscovery(RecommandationRequest request) throws Exception;
	void saveSentenceDiscoveries(List<SentenceDiscovery> sentenceDiscoveries);
}
