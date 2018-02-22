package com.mst.sentenceprocessing.interfaces;

import java.util.List;

import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.model.SentenceQuery.SentenceQueryResult;
import com.mst.model.SentenceQuery.SentenceQueryTextInput;

public interface QueryService {

	List<SentenceQueryResult> querySentences(SentenceQueryInput input) throws Exception;

	List<SentenceQueryResult> queryTextSentences(SentenceQueryTextInput input) throws Exception;
}
