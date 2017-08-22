package com.mst.sentenceprocessing.interfaces;

import java.util.List;

import com.mst.model.discrete.DiscreteData;
import com.mst.sentenceprocessing.models.DiscreteDataRequest;
import com.mst.sentenceprocessing.models.DiscreteDataResult;

public interface DiscreteDataService {

	List<DiscreteDataResult> getDiscreteDatas(DiscreteDataRequest request);
	DiscreteDataResult getById(String id);
}
