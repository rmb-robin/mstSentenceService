package com.mst.sentenceprocessing.interfaces;

import java.util.List;

import com.mst.model.autocomplete.AutoCompleteRequest;
import com.mst.model.requests.SentenceTextRequest;

public interface RecommendationService {
	void reloadCache();
	List<String> getAutoComplete(AutoCompleteRequest autoCompleteRequest);
	void saveSentenceDiscoveryProcess(SentenceTextRequest request) throws Exception;
}
