package com.mst.sentenceprocessing.services;

import java.time.LocalDate;

import org.eclipse.persistence.eis.EISObjectPersistenceXMLProject;

import com.mst.dao.HL7ParsedRequstDaoImpl;
import com.mst.dao.RawReportFileDaoImpl;
import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.dao.HL7ParsedRequstDao;
import com.mst.jsonSerializers.HL7Parser;
import com.mst.model.HL7Details;
import com.mst.model.raw.HL7ParsedRequst;
import com.mst.model.raw.ParseHl7Result;
import com.mst.model.raw.RawReportFile;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.sentenceprocessing.interfaces.RawReportService;
import com.mst.sentenceprocessing.models.RawFileSaveResult;
import com.mst.services.mst_sentence_service.RequestsMongoDatastoreProvider;

import ca.uhn.hl7v2.HL7Exception;

public class RawReportServiceImpl implements RawReportService {

	private RawReportFileDaoImpl dao; 
	private HL7ParsedRequstDaoImpl hl7ParsedRequstDao;
	
    public RawReportServiceImpl() {
    	MongoDatastoreProvider datastoreProvider = new RequestsMongoDatastoreProvider();
    	dao = new RawReportFileDaoImpl();
		dao.setMongoDatastoreProvider(datastoreProvider);	
		
		hl7ParsedRequstDao = new HL7ParsedRequstDaoImpl();
		hl7ParsedRequstDao.setMongoDatastoreProvider(datastoreProvider);
    }

	@Override
	public RawFileSaveResult save(SentenceTextRequest request,RawReportFile file) {
		LocalDate now = LocalDate.now();
		HL7ParsedRequst existingRequest = hl7ParsedRequstDao.filter(request);
		
		RawFileSaveResult result = new RawFileSaveResult();
		if(existingRequest !=null){
			file = dao.get(existingRequest.getRawFileId());
			file.getSubmittedDates().add(now);
			dao.save(file);
			result.setDuplicate(true);
			result.setFileId(existingRequest.getRawFileId());
			return result;
		}
		
		file.getSubmittedDates().add(now);
		String id =  dao.save(file);
		result.setFileId(id);
		return result;
	}

	public ParseHl7Result getSetentenceTextRequestFromRaw(HL7Details detail,RawReportFile file) throws HL7Exception{
		HL7Parser parser = new HL7Parser();
		return parser.run(detail,file.getContent(),file.getOrgName());
	}

	@Override
	public String saveParsed(String rawFileId, SentenceTextRequest request) {
		return hl7ParsedRequstDao.save(convertTOHl7Parsed(rawFileId,request));
	}
	
	private HL7ParsedRequst convertTOHl7Parsed(String rawFileId, SentenceTextRequest sentenceTextRequest){
		HL7ParsedRequst result = new HL7ParsedRequst();
		result.setDiscreteData(sentenceTextRequest.getDiscreteData());
		result.setPractice(sentenceTextRequest.getPractice());
		result.setRawFileId(rawFileId);
		result.setText(sentenceTextRequest.getText());
		result.setSource(sentenceTextRequest.getSource());
		result.setStudy(sentenceTextRequest.getStudy());
		result.setProcessedDate(LocalDate.now());
		return result;
	}
}
