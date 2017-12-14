package com.mst.sentenceprocessing.models;

public class RawFileSaveResult {

	private String fileId; 
	private boolean isDuplicate;
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public boolean isDuplicate() {
		return isDuplicate;
	}
	public void setDuplicate(boolean isDuplicate) {
		this.isDuplicate = isDuplicate;
	} 
}
