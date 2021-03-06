package com.mst.services.mst_sentence_service;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import com.mongodb.MongoClient;



public class Constants {
	
	public enum MorphiaHelper {
		INSTANCE;
		private Datastore datastore;
		
		MorphiaHelper() {
			try {
		    	Morphia morphia = new Morphia();
		    	
		    	//morphia.mapPackage("com.mst.model.morphia");
		
		    	datastore = morphia.createDatastore(new MongoClient("10.210.192.4"), "test");
		    	datastore.ensureIndexes();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		public Datastore getDatastore() {
			return this.datastore;
		}
    }

	public static int reprocessBatchSize(){
		return 1000;
	}
	
	public static int getJwtTimeout(){
		return 30;
	}
}
