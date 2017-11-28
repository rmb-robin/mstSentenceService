package com.mst.sentenceprocessing.interfaces;

import com.mst.model.HL7Details;
import com.mst.model.raw.RawReportFile;
import com.mst.model.requests.SentenceTextRequest;

import ca.uhn.hl7v2.HL7Exception;

public interface RawReportService {

	String save(RawReportFile file);
	 SentenceTextRequest getSetentenceTextRequestFromRaw(HL7Details detail,RawReportFile file) throws HL7Exception;
}
