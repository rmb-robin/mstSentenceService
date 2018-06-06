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
    public void saveRawFile() {
        RawReportFile file = getRawReportFile();
        try {
            List<HL7Details> detailsList = reportsService.getHL7DetailsByOrgName(file.getOrgName());
            assertTrue(!detailsList.isEmpty());
            HL7Details details = detailsList.get(0);
            AllHl7Elements allHl7Elements = reportsService.getAllHl7Elements();
            ParseHl7Result parsedResult = rawReportService.getSetentenceTextRequestFromRaw(details, file, allHl7Elements);
            SentenceTextRequest request = parsedResult.getSentenceTextRequest();
            request.getDiscreteData().setOrganizationId(file.getOrgId());
            request.getDiscreteData().setAllAvailableFields(parsedResult.getAllFields());
            RawFileSaveResult rawFileSaveResult = rawReportService.save(request, file);
            String fileId = rawFileSaveResult.getFileId();
            assertTrue(!rawFileSaveResult.isDuplicate());
            request.getDiscreteData().setRawFileId(fileId);
            String parsedId = rawReportService.saveParsed(fileId, request);
            request.getDiscreteData().setParseReportId(parsedId);
            assertTrue(parsedResult.getMissingFields().isEmpty());
            request.getDiscreteData().getAllAvailableFields().clear();
            sentenceService.processSentenceTextRequest(request);
        } catch (Exception ex) {
            System.out.println();
        }
    }

    private RawReportFile getRawReportFile() {
        RawReportFile file = new RawReportFile();
        file.setContent("MSH|^~\\\\&|PSCRIBE|SDI-AZELOC_00431_AC|RADIS1|BMG|20180320214120||ORU^R01|9624447|P|2.3|20084623\\\\rPID|1|324793703|02049554||SPENCE^PAMELA^M||19830530|F||||||||||AZE-8195379|629011955\\\\rPV1|1|O|AZELOC_00431_AC|||||1023422532^BOTTOMLEY^WILLIAM^|||||||||||AZE-8195379|||||||||||||||||||||||||20180320152807\\\\rORC|RE|10995017459||||\\\\rOBR|1|10995017459|SDI-10995017459|3266114633^IH Hip W+orW/O Pelvis 2-3 vws Lt|||201803201642|||||||||1023422532^BOTTOMLEY^WILLIAM||||||20180320213711||XA|F||^^^201803201642||||^left hip pain||RRWJACOBY^Jacoby||^^20180320213619\\\\rOBX|1|FT|3266114633^IH Hip W+orW/O Pelvis 2-3 vws Lt||ZZZCOMPARISON:  None.ZZZZZZHISTORY:  left hip pain.ZZZZZZTECHNIQUE:  Pelvis and left hipZZZZZZFINDINGS:  Negative for acute fracture or malalignment. ZZZZZZIMPRESSION:  Negative for fracture. ZZZZZZInterpreted By: William Jacoby, M.D.ZZZ Signature Date:3/20/2018 9:37 PM MST ZZZZZZThe workstation used in generating this report was CRIPHX113944570.ZZZZZZ||||||F|||20180320213711||RRWJACOBY^Jacoby\\\\r");
        file.setProcessed(false);
        file.setOrgId("58c6f3ceaf3c420b90160803");
        file.setOrgName("rad");
        file.setIsProcessingtypeSentenceDiscovery(false);
        return file;
    }
}
