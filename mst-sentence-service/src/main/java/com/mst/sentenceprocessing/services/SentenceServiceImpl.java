package com.mst.sentenceprocessing.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.mst.dao.*;
import com.mst.filter.BusinessRuleFilterImpl;
import com.mst.filter.RemoveNonQueryEdgesFromResult;
import com.mst.interfaces.dao.BusinessRuleDao;
import com.mst.model.SentenceQuery.*;
import com.mst.model.businessRule.BusinessRule;
import org.bson.types.ObjectId;
import org.glassfish.hk2.api.PreDestroy;

import com.mst.filter.NotAndAllRequestFactoryImpl;
import com.mst.interfaces.DiscreteDataDao;
import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.sentenceprocessing.DiscreteDataDuplicationIdentifier;
import com.mst.interfaces.sentenceprocessing.DiscreteDataInputProcessor;
import com.mst.interfaces.sentenceprocessing.SentenceProcessingController;
import com.mst.interfaces.SentenceProcessingMetaDataInputFactory;
import com.mst.interfaces.dao.SentenceDao;
import com.mst.interfaces.dao.SentenceQueryDao;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.metadataTypes.DiscreteDataBucketIdenticationType;
import com.mst.model.raw.HL7ParsedRequst;
import com.mst.model.requests.SentenceRequest;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.SentenceProcessingFailures;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;
import com.mst.model.sentenceProcessing.SentenceProcessingResult;
import com.mst.sentenceprocessing.DiscreteDataDuplicationIdentifierImpl;
import com.mst.sentenceprocessing.DiscreteDataInputProcessorImpl;
import com.mst.sentenceprocessing.SentenceConverter;
import com.mst.sentenceprocessing.SentenceProcessingControllerImpl;
import com.mst.sentenceprocessing.dao.SentenceProcessingDbMetaDataInputFactory;
import com.mst.sentenceprocessing.interfaces.SentenceService;
import com.mst.sentenceprocessing.models.Edges;
import com.mst.sentenceprocessing.models.SaveSentenceTextResponse;
import com.mst.services.mst_sentence_service.RequestsMongoDatastoreProvider;
import com.mst.services.mst_sentence_service.SentenceServiceMongoDatastoreProvider;

import static com.mst.model.businessRule.BusinessRule.RuleType.MODIFY_SENTENCE_QUERY_INPUT;
import static com.mst.model.businessRule.BusinessRule.RuleType.MODIFY_SENTENCE_QUERY_RESULT;


public class SentenceServiceImpl implements SentenceService, PreDestroy {
    private SentenceQueryDao sentenceQueryDao;
    private SentenceDao sentenceDao;
    private MongoDatastoreProvider mongoProvider;
    private SentenceProcessingMetaDataInputFactory sentenceProcessingDbMetaDataInputFactory;
    private SentenceProcessingController controller;
    private DiscreteDataInputProcessor discreteDataInputProcessor;
    private DiscreteDataDao discreteDataDao;
    private DiscreteDataDuplicationIdentifier discreteDataDuplicationIdentifier;
    private final HL7ParsedRequstDaoImpl hl7RawDao;
    private BusinessRuleDao businessRuleDao;
    private static SentenceProcessingMetaDataInput metaDataInput;

    public SentenceServiceImpl() {
        mongoProvider = new SentenceServiceMongoDatastoreProvider();
        sentenceQueryDao = new SentenceQueryDaoImpl();
        sentenceQueryDao.setMongoDatastoreProvider(mongoProvider);
        sentenceDao = new SentenceDaoImpl();
        sentenceDao.setMongoDatastoreProvider(mongoProvider);
        sentenceProcessingDbMetaDataInputFactory = new SentenceProcessingDbMetaDataInputFactory(mongoProvider);
        controller = new SentenceProcessingControllerImpl();
        discreteDataInputProcessor = new DiscreteDataInputProcessorImpl(mongoProvider);
        discreteDataDao = new DiscreteDataDaoImpl();
        discreteDataDao.setMongoDatastoreProvider(mongoProvider);
        discreteDataDuplicationIdentifier = new DiscreteDataDuplicationIdentifierImpl();
        hl7RawDao = new HL7ParsedRequstDaoImpl();
        hl7RawDao.setMongoDatastoreProvider(new RequestsMongoDatastoreProvider());
        businessRuleDao = new BusinessRuleDaoImpl(BusinessRule.class);
        businessRuleDao.setMongoDatastoreProvider(mongoProvider);
    }

    @Override
    public List<SentenceQueryResult> querySentences(SentenceQueryInput input) throws Exception {
        if (input.getOrganizationId() == null)
            throw new Exception("Missing OrgId");
        if (input.getIsNotAndAll())
            input = new NotAndAllRequestFactoryImpl().create(input);
        List<BusinessRule> modifySentenceQueryInput = businessRuleDao.get(input.getOrganizationId(), MODIFY_SENTENCE_QUERY_INPUT);
        if (modifySentenceQueryInput != null)
            new BusinessRuleFilterImpl().modifySentenceQueryInput(input, modifySentenceQueryInput);
        List<SentenceQueryResult> results = sentenceQueryDao.getSentences(input);
        processQueryDiscreteData(results);
        List<BusinessRule> modifySentenceQueryResult = businessRuleDao.get(input.getOrganizationId(), MODIFY_SENTENCE_QUERY_RESULT);
        if (modifySentenceQueryResult != null)
            new BusinessRuleFilterImpl().modifySentenceQueryResult(results, modifySentenceQueryResult);
        new RemoveNonQueryEdgesFromResult().process(input, results);
        return results;
    }

    @Override
    public List<SentenceQueryResult> queryTextSentences(SentenceQueryTextInput input) {
        return null;
    }

    @Override
    public void saveSentences(List<Sentence> sentences, DiscreteData discreteData, SentenceProcessingFailures sentenceProcessingFailures, boolean isReprocess, String reprocessId, String resultType) {
        List<SentenceDb> documents = new ArrayList<>();
        for (Sentence sentence : sentences) {
            SentenceDb document = SentenceConverter.convertToSentenceDb(sentence, false);
            if (isReprocess) {
                document.setId(new ObjectId(sentence.getId()));
                document.setReprocessId(reprocessId);
            }
            documents.add(document);
        }
        if (!isReprocess) {
            discreteDataInputProcessor.processDiscreteData(discreteData, sentences, resultType);
            sentenceDao.saveSentences(documents, discreteData, sentenceProcessingFailures);
        } else {
            sentenceDao.saveReprocess(documents, sentenceProcessingFailures);
        }
    }

    @Override
    public String reprocessSentences(SentenceReprocessingInput input) {   //move to other project
        controller.setMetadata(getSentenceProcessingMetadata());
        String reprocessId = UUID.randomUUID().toString();
        input.setReprocessId(reprocessId);
        while (true) {
            List<SentenceDb> documents = this.getSentencesForReprocessing(input);
            if (documents.isEmpty()) return null;
            Set<String> discreteDataIds = reprocessSentenceBatch(documents, reprocessId);
            List<SentenceDb> sentencesForDiscreteData = sentenceQueryDao.getSentencesByDiscreteDataIds(discreteDataIds);
            List<Sentence> sentences = SentenceConverter.convertToSentence(sentencesForDiscreteData, true, true, false);
            Map<String, List<Sentence>> sentencesByDiscreteData = SentenceByDiscreteDataMapper.groupSentencesByDiscretedata(sentences);
            reprocessDiscreteData(sentencesByDiscreteData);
            if (documents.size() < input.getTakeSize())
                break;
        }
        return reprocessId;
    }

    @Override
    public List<Sentence> createSentences(SentenceRequest request) throws Exception {
        controller.setMetadata(getSentenceProcessingMetadata());
        return controller.processSentences(request);
    }

    @Override
    public SentenceProcessingMetaDataInput getSentenceProcessingMetadata() {
        if (metaDataInput == null)
            metaDataInput = sentenceProcessingDbMetaDataInputFactory.create();
        return metaDataInput;
    }

    @Override
    public SentenceProcessingResult createSentences(SentenceTextRequest request) throws Exception {
        controller.setMetadata(getSentenceProcessingMetadata());
        return controller.processText(request);
    }

    @Override
    public List<String> getEdgeNamesForTokens(List<String> tokens) {
        Edges edges = this.mongoProvider.getDefaultDb().createQuery(Edges.class).get();
        return edges.getNames();
    }

    @Override
    public List<SentenceDb> getSentencesForReprocessing(SentenceReprocessingInput input) {
        return sentenceQueryDao.getSentencesForReprocess(input);
    }

    @Override
    public void reprocessDiscreteData(String id) {
        Set<String> discreteDataIds = new HashSet<>();
        discreteDataIds.add(id);
        List<SentenceDb> sentencesForDiscreteData = sentenceQueryDao.getSentencesByDiscreteDataIds(discreteDataIds);
        List<Sentence> sentences = SentenceConverter.convertToSentence(sentencesForDiscreteData, true, true, false);
        Map<String, List<Sentence>> sentencesByDiscreteData = SentenceByDiscreteDataMapper.groupSentencesByDiscretedata(sentences);
        reprocessDiscreteData(sentencesByDiscreteData);
    }

    @Override
    public SaveSentenceTextResponse processSentenceTextRequest(SentenceTextRequest sentenceTextRequest) throws Exception {
        String discreteDataResultType = DiscreteDataBucketIdenticationType.compliance;
        if (sentenceTextRequest.isNeedResult())
            discreteDataResultType = DiscreteDataBucketIdenticationType.followup;
        SentenceProcessingResult result = this.createSentences(sentenceTextRequest);
        this.saveSentences(result.getSentences(), sentenceTextRequest.getDiscreteData(), result.getFailures(), false, null, discreteDataResultType);
        if (sentenceTextRequest.isNeedResult()) {
            return SaveSentenceTextResponseFactory.create(sentenceTextRequest.getDiscreteData().getId().toString(), sentenceTextRequest.getDiscreteData().getExpectedFollowup());
        }
        return null;
    }

    @Override
    public void saveEdges(Edges edges) {
        this.mongoProvider.getDefaultDb().save(edges);
    }

    @Override
    public List<String> getSentenceTextForDiscreteDataId(String discreteDataId) {
        List<SentenceDb> sentences = sentenceQueryDao.getSentencesForDiscreteDataId(discreteDataId);
        List<String> result = new ArrayList<>();
        for (SentenceDb sentenceDb : sentences) {
            result.add(sentenceDb.getOrigSentence());
        }
        return result;
    }

    @Override
    public void RemoveNonDisplayEdges(List<SentenceQueryResult> queryResults) {
        for (SentenceQueryResult queryResult : queryResults) {
            List<SentenceQueryEdgeResult> edgeResults = queryResult.getSentenceQueryEdgeResults();
            edgeResults.removeIf(r -> !r.isDisplayEdge());
        }
    }

    @Override
    public void preDestroy() {
    }

    private String getSentenceTextForDiscreteDataIdByString(String parsedFileId) {
        if (parsedFileId == null)
            return "";
        HL7ParsedRequst parsedRequest = hl7RawDao.get(parsedFileId);
        if (parsedRequest == null)
            return "";
        return parsedRequest.getText();
    }

    private void reprocessDiscreteData(Map<String, List<Sentence>> sentencesByDiscreteData) {
        for (Map.Entry<String, List<Sentence>> entry : sentencesByDiscreteData.entrySet()) {
            List<Sentence> sentences = entry.getValue();
            if (sentences.isEmpty())
                continue;
            DiscreteData discreteData = sentences.get(0).getDiscreteData();
            cleanDiscreteDataForReprocess(discreteData);
            discreteDataInputProcessor.processDiscreteData(discreteData, sentences, DiscreteDataBucketIdenticationType.compliance);
            discreteDataDao.save(discreteData, true);
        }
    }

    private Set<String> reprocessSentenceBatch(List<SentenceDb> documents, String reprocessId) {
        List<Sentence> sentences = SentenceConverter.convertToSentence(documents, false, false, false);
        Set<String> discreteDataIds = new HashSet<>();
        for (Sentence sentence : sentences)
            discreteDataIds.add(sentence.getDiscreteData().getId().toString());
        SentenceProcessingResult result = controller.reprocessSentences(sentences);
        this.saveSentences(result.getSentences(), null, result.getFailures(), true, reprocessId, DiscreteDataBucketIdenticationType.compliance);
        return discreteDataIds;
    }

    private void cleanDiscreteDataForReprocess(DiscreteData discreteData) {
        discreteData.getCustomFields().clear();
        discreteData.setBucketName(null);
        discreteData.setIsCompliant(false);
    }

    private void processQueryDiscreteData(List<SentenceQueryResult> results) {
        HashMap<String, String> reportTextByIds = new HashMap<>();
        Map<String, DiscreteData> discreteDatasById = new HashMap<>();
        for (SentenceQueryResult result : results) {
            String key = result.getDiscreteData().getId().toString();
            processReportText(result, reportTextByIds);
            if (discreteDatasById.containsKey(key))
                continue;
            discreteDatasById.put(key, result.getDiscreteData());
        }
        List<DiscreteData> discretaDatas = new ArrayList<>(discreteDatasById.values());
        discreteDataDuplicationIdentifier.process(discretaDatas);
    }

    private void processReportText(SentenceQueryResult result, HashMap<String, String> reportTextByIds) {
        if (result == null || result.getDiscreteData() == null || result.getDiscreteData().getId() == null)
            return;
        String id = result.getDiscreteData().getParseReportId();
        if (reportTextByIds.containsKey(id)) {
            result.setReportText(reportTextByIds.get(id));
            return;
        }
        String text = this.getSentenceTextForDiscreteDataIdByString(id);
        reportTextByIds.put(id, text.trim());
        result.setReportText(reportTextByIds.get(id));
    }
}
