package com.mst.sentenceprocessing.services;

import com.mst.dao.RawReportFileDaoImpl;
import com.mst.jsonSerializers.HL7Parser;
import com.mst.model.HL7Details;
import com.mst.model.raw.RawReportFile;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.sentenceprocessing.interfaces.RawReportService;
import com.mst.services.mst_sentence_service.RequestsMongoDatastoreProvider;

import ca.uhn.hl7v2.HL7Exception;

public class RawReportServiceImpl implements RawReportService {

	private RawReportFileDaoImpl dao; 
	
    public RawReportServiceImpl() {
		dao = new RawReportFileDaoImpl();
		dao.setMongoDatastoreProvider(new RequestsMongoDatastoreProvider());	
	}

	@Override
	public String save(RawReportFile file) {
		return dao.save(file);
	}

	
	public SentenceTextRequest getSetentenceTextRequestFromRaw(HL7Details detail,RawReportFile file) throws HL7Exception{
		HL7Parser parser = new HL7Parser();
		return parser.run(detail,file.getContent(),file.getOrgId());
	}
	
}
