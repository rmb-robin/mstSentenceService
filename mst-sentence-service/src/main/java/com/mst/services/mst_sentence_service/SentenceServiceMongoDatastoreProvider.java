package com.mst.services.mst_sentence_service;

import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.model.util.MongoConnectionEntity;
import com.mst.util.MongoDatastoreProviderBase;

public class SentenceServiceMongoDatastoreProvider extends MongoDatastoreProviderBase implements MongoDatastoreProvider {

	public SentenceServiceMongoDatastoreProvider(){
		connectionEntity = new MongoConnectionEntity();
		connectionEntity.setDatabaseName("test_new");
		//connectionEntity.setIpAddress("10.210.192.4"); // dev.. 
		connectionEntity.setIpAddress("10.12.128.98"); // prod.. 

	}
}
