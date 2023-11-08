package com.abdulazizn.newsxpress.model;

/*
 * Represents a News Article
 */
public class NewsArticle {
	private int id;
	private String title;
	private String publicationDateTime;
	private String content;
	private String desc;
	private String URL;
	private String category;
	
	//	GETTERS AND SETTERS
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPublicationDateTime() {
		return publicationDateTime;
	}

	public void setPublicationDateTime(String publicationDateTime) {
		this.publicationDateTime = publicationDateTime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getURL() {
		return URL;
	}

	public void setURL(String URL) {
		this.URL = URL;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
	
	public String[] getContentAsList() {
		return content.split("\\r?\\n");
	}
	
}
