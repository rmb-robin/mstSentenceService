package com.mst.sentenceprocessing.interfaces;

import java.util.List;

import com.mst.model.autocomplete.AutoCompleteRequest;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.requests.RecommandationRequest;
import com.mst.model.sentenceProcessing.Sentence;

public interface RecommandationService {

	void reloadCache();
	List<String> getAutoComplete(AutoCompleteRequest autoCompleteRequest);
	void saveSentenceDiscoveryProcess(RecommandationRequest request) throws Exception;
}
