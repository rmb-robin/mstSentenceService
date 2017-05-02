package com.mst.sentenceprocessing.interfaces;

import java.util.List;

import javax.security.auth.callback.TextInputCallback;

import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.model.sentenceProcessing.TextInput;
import com.mst.services.mst_sentence_service.SentenceRequest;

public interface SentenceService {
	List<SentenceQueryResult> querySentences(SentenceQueryInput input);
	void saveSentences(List<Sentence> sentences);
	List<Sentence> createSentences(SentenceRequest request) throws Exception;
	List<Sentence> createSentences(TextInput textInput) throws Exception;
	SentenceProcessingMetaDataInput getSentenceProcessingMetadata();
}
