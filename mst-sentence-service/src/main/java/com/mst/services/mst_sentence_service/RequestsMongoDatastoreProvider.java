package com.mst.services.mst_sentence_service;

import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.model.util.MongoConnectionEntity;
import com.mst.util.MongoDatastoreProviderBase;

public class RequestsMongoDatastoreProvider extends MongoDatastoreProviderBase implements MongoDatastoreProvider {

	public RequestsMongoDatastoreProvider(){
		connectionEntity = new MongoConnectionEntity();
		connectionEntity.setDatabaseName("requests");
		connectionEntity.setIpAddress("10.210.192.4"); // dev.. 
		//connectionEntity.setIpAddress("10.12.128.98"); // prod.. 
		//add here for prod...

	}
}

