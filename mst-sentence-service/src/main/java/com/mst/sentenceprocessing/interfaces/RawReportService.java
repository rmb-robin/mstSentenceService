package com.mst.sentenceprocessing.interfaces;

import com.mst.model.HL7Details;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.raw.ParseHl7Result;
import com.mst.model.raw.RawReportFile;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.sentenceprocessing.models.RawFileSaveResult;

import ca.uhn.hl7v2.HL7Exception;

public interface RawReportService {

	RawFileSaveResult save(SentenceTextRequest request,RawReportFile file);
	ParseHl7Result getSetentenceTextRequestFromRaw(HL7Details detail,RawReportFile file) throws HL7Exception;
	String saveParsed(String rawFileId, SentenceTextRequest request);	
}
