package com.mst.sentenceprocessing.services;

import org.eclipse.persistence.internal.identitymaps.HardCacheWeakIdentityMap.ReferenceCacheKey;

import com.mst.dao.DiscreteDataDaoImpl;
import com.mst.dao.RejectedReportDaoImpl;
import com.mst.interfaces.DiscreteDataDao;
import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.dao.RejectedReportDao;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.requests.RejectedReport;
import com.mst.sentenceprocessing.interfaces.ReportsService;
import com.mst.sentenceprocessing.models.ReportSummaryRequest;
import com.mst.sentenceprocessing.models.ReportSummaryResponse;
import com.mst.services.mst_sentence_service.SentenceServiceMongoDatastoreProvider;

public class ReportsServiceImpl implements ReportsService {

	private RejectedReportDao rejectedReportDao; 
	private DiscreteDataDao discreteDataDao;
	private MongoDatastoreProvider  mongoProvider; 
	
	public ReportsServiceImpl(){
		mongoProvider = new SentenceServiceMongoDatastoreProvider();
		rejectedReportDao = new RejectedReportDaoImpl();
		rejectedReportDao.setMongoDatastoreProvider(mongoProvider);
		discreteDataDao = new DiscreteDataDaoImpl();
		discreteDataDao.setMongoDatastoreProvider(mongoProvider);
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
}
