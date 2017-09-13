package com.mst.sentenceprocessing.interfaces;

import java.util.List;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.SentenceQuery.SentenceReprocessingInput;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.requests.RejectedReport;
import com.mst.model.requests.SentenceRequest;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.SentenceProcessingFailures;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.model.sentenceProcessing.SentenceProcessingResult;


public interface SentenceService {
	List<SentenceQueryResult> querySentences(SentenceQueryInput input) throws Exception;
	void saveSentences(List<Sentence> sentences,DiscreteData discreteData, SentenceProcessingFailures failures,boolean isReprocess, String reprocessId,String resultType);
	List<Sentence> createSentences(SentenceRequest request) throws Exception;
	SentenceProcessingResult createSentences(SentenceTextRequest request) throws Exception;
	SentenceProcessingMetaDataInput getSentenceProcessingMetadata();
	List<String> getEdgeNamesForTokens(List<String> tokens);
	List<SentenceDb> getSentencesForReprocessing(SentenceReprocessingInput input);
	String reprocessSentences(SentenceReprocessingInput input);
	void reprocessDiscreteData(String id);
}
