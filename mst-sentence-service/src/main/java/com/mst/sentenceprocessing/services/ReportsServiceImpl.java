package com.mst.sentenceprocessing.services;

import java.util.List;

import org.eclipse.persistence.internal.identitymaps.HardCacheWeakIdentityMap.ReferenceCacheKey;

import com.mst.dao.DiscreteDataDaoImpl;
import com.mst.dao.Hl7DetailsDaoImpl;
import com.mst.dao.RejectedReportDaoImpl;
import com.mst.interfaces.DiscreteDataDao;
import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.dao.Hl7DetailsDao;
import com.mst.interfaces.dao.RejectedReportDao;
import com.mst.model.HL7Details;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.requests.RejectedReport;
import com.mst.sentenceprocessing.interfaces.ReportsService;
import com.mst.sentenceprocessing.models.ReportSummaryRequest;
import com.mst.sentenceprocessing.models.ReportSummaryResponse;
import com.mst.services.mst_sentence_service.SentenceServiceMongoDatastoreProvider;

public class ReportsServiceImpl implements ReportsService {

	private RejectedReportDao rejectedReportDao; 
	private DiscreteDataDao discreteDataDao;
	private Hl7DetailsDao hl7DetailsDao;
	private MongoDatastoreProvider  mongoProvider; 
	
	public ReportsServiceImpl(){
		mongoProvider = new SentenceServiceMongoDatastoreProvider();
		rejectedReportDao = new RejectedReportDaoImpl();
		rejectedReportDao.setMongoDatastoreProvider(mongoProvider);
		discreteDataDao = new DiscreteDataDaoImpl();
		discreteDataDao.setMongoDatastoreProvider(mongoProvider);
		hl7DetailsDao = new Hl7DetailsDaoImpl();
		hl7DetailsDao.setMongoDatastoreProvider(mongoProvider);
	}
	
	public void saveRejectedReport(RejectedReport rejectedReport) {
		//rejectedReport.setTimeStamps();
		rejectedReportDao.save(rejectedReport);
	}

	@Override
	public ReportSummaryResponse getReportSummary(ReportSummaryRequest reportSummaryRequest) {
		// TODO Auto-generated method stub
		ReportSummaryResponse reportSummaryResponse = new ReportSummaryResponse();
		reportSummaryResponse.setRejectedReports(
				rejectedReportDao.getByNameAndDate(reportSummaryRequest.getOrganizationName(),reportSummaryRequest.getReportDate()));
		
		reportSummaryResponse.setDate(reportSummaryRequest.getReportDate());
		reportSummaryResponse.setTotalNonRejectedReports(
				discreteDataDao.getCountByNameAndDate(reportSummaryRequest.getOrganizationName(), reportSummaryRequest.getReportDate()));
		
		return reportSummaryResponse;	
	}

	public List<HL7Details> getHL7DetailsByOrgName(String orgName) {
		return hl7DetailsDao.getByOrgName(orgName);
	}
}
