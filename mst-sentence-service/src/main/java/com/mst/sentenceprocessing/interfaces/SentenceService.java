package com.mst.sentenceprocessing.interfaces;

import java.util.List;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.requests.RejectedReport;
import com.mst.model.requests.SentenceRequest;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;


public interface SentenceService {
	List<SentenceQueryResult> querySentences(SentenceQueryInput input);
	void saveSentences(List<Sentence> sentences,DiscreteData discreteData);
	List<Sentence> createSentences(SentenceRequest request) throws Exception;
	List<Sentence> createSentences(SentenceTextRequest request) throws Exception;
	SentenceProcessingMetaDataInput getSentenceProcessingMetadata();
}
