package com.mst.test;

import com.mst.model.HL7Details;
import com.mst.model.raw.AllHl7Elements;
import com.mst.model.raw.ParseHl7Result;
import com.mst.model.raw.RawReportFile;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.sentenceprocessing.interfaces.RawReportService;
import com.mst.sentenceprocessing.interfaces.ReportsService;
import com.mst.sentenceprocessing.interfaces.SentenceService;
import com.mst.sentenceprocessing.models.RawFileSaveResult;
import com.mst.sentenceprocessing.services.RawReportServiceImpl;
import com.mst.sentenceprocessing.services.ReportsServiceImpl;
import com.mst.sentenceprocessing.services.SentenceServiceImpl;
import org.bson.types.ObjectId;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.*;

public class RawReportControllerTest {
    private ReportsService reportsService;
    private RawReportService rawReportService;
    private SentenceService sentenceService;

    public RawReportControllerTest() {
        reportsService = new ReportsServiceImpl();
        rawReportService = new RawReportServiceImpl();
        sentenceService = new SentenceServiceImpl();
    }

    @Test
    public void saveRawFile1() {
        RawReportFile file = new RawReportFile();
        file.setId(new ObjectId());
        file.setContent(getTest1());
        file.setProcessed(false);
        file.setOrgId("58c6f3ceaf3c420b90160803");
        file.setOrgName("rad");
        file.setIsProcessingtypeSentenceDiscovery(false);
        processRawFile(file);
    }

    @Test
    public void saveRawFile2() {
        RawReportFile file = new RawReportFile();
        file.setId(new ObjectId());
        file.setContent(getTest2());
        file.setProcessed(false);
        file.setOrgId("58c6f3ceaf3c420b90160803");
        file.setOrgName("rad");
        file.setIsProcessingtypeSentenceDiscovery(false);
        processRawFile(file);
    }

    @Test
    public void saveRawFile3() {
        RawReportFile file = new RawReportFile();
        file.setId(new ObjectId());
        file.setContent(getTest3());
        file.setProcessed(false);
        file.setOrgId("58c6f3ceaf3c420b90160803");
        file.setOrgName("rad");
        file.setIsProcessingtypeSentenceDiscovery(false);
        processRawFile(file);
    }

    @Test
    public void saveRawFile4() {
        RawReportFile file = new RawReportFile();
        file.setId(new ObjectId());
        file.setContent(getTest4());
        file.setProcessed(false);
        file.setOrgId("58c6f3ceaf3c420b90160803");
        file.setOrgName("rad");
        file.setIsProcessingtypeSentenceDiscovery(false);
        processRawFile(file);
    }

    private void processRawFile(RawReportFile file) {
        try {
            List<HL7Details> detailsList = reportsService.getHL7DetailsByOrgName(file.getOrgName());
            assertTrue(!detailsList.isEmpty());
            HL7Details details = detailsList.get(0);
            AllHl7Elements allHl7Elements = reportsService.getAllHl7Elements();
            ParseHl7Result parsedResult = rawReportService.getSentenceTextRequestFromRaw(details, file, allHl7Elements);
            SentenceTextRequest request = parsedResult.getSentenceTextRequest();
            request.getDiscreteData().setOrganizationId(file.getOrgId());
            request.getDiscreteData().setAllAvailableFields(parsedResult.getAllFields());
            RawFileSaveResult rawFileSaveResult = rawReportService.save(request, file);
            String fileId = rawFileSaveResult.getFileId();
            assertTrue(!rawFileSaveResult.isDuplicate());
            request.getDiscreteData().setRawFileId(fileId);
            String parsedId = rawReportService.saveParsed(fileId, request);
            request.getDiscreteData().setParseReportId(parsedId);
            request.getDiscreteData().getAllAvailableFields().clear();
            sentenceService.processSentenceTextRequest(request);
        } catch (Exception ex) {
            System.out.println();
        }
    }

    private String getTest1() { //2.3
        return "MSH|^~\\&||MG||Martin General|20180531000042||ORU^R01|2660359|P|2.3|29096256|||||||\n" +
                "PID|||47023MG||RAHMAN^JEAN^R||19390309|F|||PO BOX 1555^1107 FEGGINS ROAD^ROBERSONVILLE^NC^27871||(252)916-2242|||||5642184^^^20180530225200||||||||||||\n" +
                "PV1||O|ED^E108^01^MG||||536^ROCCI^CHARLES^^^^ALLEN|536^ROCCI^CHARLES^^^^ALLEN|||||||||536^ROCCI^CHARLES^^^^ALLEN|E|5642184|||||||||||||||||||||||||20180530223200||||||||\n" +
                "ORC|RE|||||||||||536^ROCCI^CHARLES^^^^ALLEN|||||MG||\n" +
                "OBR|1|56421840000200MG|56421840000200MG|RPID6415^XR KNEE 3 VIEWS|||20180530225200|||||||||536^ROCCI^CHARLES^^^^ALLEN||56421840000200MG||1||20180530235935||CR|F|F|^^^20180530225200||||Edema|1710195276^Hendrix^Christopher|1710195276^Hendrix^Christopher||||||||||\n" +
                "ZPS||||{\"pid3\":\"47023\",\"ord\":\"200\",\"accession\":\"56421840000200\",\"obr41\":\"KNEE\",\"obr42\":\"LE-KNEE 3V CR\",\"obr43\":\"RAD\",\"obr276\":\"S\"}|20180530235935|||||\n" +
                "OBX|1|FT|RPID6415&BODY||EXAM DESCRIPTION: Right knee, 3 views    CLINICAL HISTORY: Edema    COMPARISON: None.    FINDINGS: 3 views of the right knee. No acute fracture or  dislocation. Normal osseous mineralization. Moderate 3  compartment joint space narrowing and marginal osteophytosis.  Moderate joint effusion.    IMPRESSION:    1. No acute fracture identified.    2. Moderate joint effusion.    3. Moderate 3 compartment osteoarthritic change.      Electronically signed by:  Christopher Hendrix  5/30/2018 11:59  PM CDT Workstation: 103-1151||||||F";
    }

    private String getTest2() {
        return "";
        }

    private String getTest3() {
        return "";
    }

    private String getTest4() {
        return "";
    }
}
