package com.mst.sentenceprocessing.services;

import java.time.LocalDate;

import com.mst.dao.HL7ParsedRequstDaoImpl;
import com.mst.dao.RawReportFileDaoImpl;
import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.jsonSerializers.HL7Parser;
import com.mst.model.HL7Details;
import com.mst.model.raw.AllHl7Elements;
import com.mst.model.raw.HL7ParsedRequst;
import com.mst.model.raw.ParseHl7Result;
import com.mst.model.raw.RawReportFile;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.sentenceprocessing.interfaces.RawReportService;
import com.mst.sentenceprocessing.models.RawFileSaveResult;
import com.mst.services.mst_sentence_service.RequestsMongoDatastoreProvider;

import ca.uhn.hl7v2.HL7Exception;

public class RawReportServiceImpl implements RawReportService {
    private RawReportFileDaoImpl rawReportFileDao;
    private HL7ParsedRequstDaoImpl hl7ParsedRequestDao;

    public RawReportServiceImpl() {
        MongoDatastoreProvider datastoreProvider = new RequestsMongoDatastoreProvider();
        rawReportFileDao = new RawReportFileDaoImpl();
        rawReportFileDao.setMongoDatastoreProvider(datastoreProvider);
        hl7ParsedRequestDao = new HL7ParsedRequstDaoImpl();
        hl7ParsedRequestDao.setMongoDatastoreProvider(datastoreProvider);
    }

    @Override
    public RawFileSaveResult save(SentenceTextRequest request, RawReportFile file) {
        LocalDate now = LocalDate.now();
        HL7ParsedRequst existingRequest = hl7ParsedRequestDao.filter(request);
        RawFileSaveResult result = new RawFileSaveResult();
        if (existingRequest != null) {
            file = rawReportFileDao.get(existingRequest.getRawFileId());
            file.getSubmittedDates().add(now);
            rawReportFileDao.save(file);
            result.setDuplicate(true);
            result.setFileId(existingRequest.getRawFileId());
            return result;
        }
        file.getSubmittedDates().add(now);
        String id = rawReportFileDao.save(file);
        result.setFileId(id);
        return result;
    }

    public ParseHl7Result getSentenceTextRequestFromRaw(HL7Details detail, RawReportFile file, AllHl7Elements allElements) throws HL7Exception {
        HL7Parser parser = new HL7Parser();
        return parser.run(detail, file.getContent(), file.getOrgName(), allElements);
    }

    @Override
    public String saveParsed(String rawFileId, SentenceTextRequest request) {
        return hl7ParsedRequestDao.save(convertToHL7Parsed(rawFileId, request));
    }

    private HL7ParsedRequst convertToHL7Parsed(String rawFileId, SentenceTextRequest sentenceTextRequest) {
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
