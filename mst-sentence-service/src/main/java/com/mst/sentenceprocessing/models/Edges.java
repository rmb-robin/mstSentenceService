package com.mst.sentenceprocessing.models;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("edges")
public class Edges {

	@Id
	private ObjectId id;
	
	private List<String> names;

	public List<String> getNames() {
		return names;
	}

	public void setNames(List<String> names) {
		this.names = names;
	} 
	
	
	
}
