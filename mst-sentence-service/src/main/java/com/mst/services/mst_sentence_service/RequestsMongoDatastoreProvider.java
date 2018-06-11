package com.mst.services.mst_sentence_service;

import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.model.util.MongoConnectionEntity;
import com.mst.util.MongoDatastoreProviderBase;

public class RequestsMongoDatastoreProvider extends MongoDatastoreProviderBase implements MongoDatastoreProvider {

	public RequestsMongoDatastoreProvider(){
		connectionEntity = new MongoConnectionEntity();
		connectionEntity.setDatabaseName("requests");
	//	connectionEntity.setIpAddress("10.0.4.162"); // PROD radius.. 

		connectionEntity.setIpAddress("10.0.129.218"); // QA radius.. 
	}
}

