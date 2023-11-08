package com.abdulazizn.newsxpress.model;

/*
 * Handles generating unique IDs for NewsArticle class
 */
public class IDHandler {
	private int uniqueId = 0;
	
	public int generateId() {
		uniqueId++;
		return uniqueId;
	}
}
