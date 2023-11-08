package com.abdulazizn.newsxpress.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abdulazizn.newsxpress.model.NewsArticle;
import com.abdulazizn.newsxpress.model.NewsArticleHandler;

/*
 * Used to acquire News Article objects
 */
@Service
public class NewsArticleService {
	
	private final NewsArticleHandler newsArticleHandler;
	private final Map<String, String> newsCategoryTitles = Map.of(
				"top_stories", "Top Stories",
				"world", "World",
				"uk", "UK",
				"business", "Business",
				"politics", "Politics",
				"health", "Health",
				"education", "Education & Family",
				"science", "Science & Environment",
				"technology", "Technology",
				"entertainment", "Entertainment & Arts"
			);
	
	@Autowired
	public NewsArticleService(NewsArticleHandler newsArticleHandler) {
		this.newsArticleHandler = newsArticleHandler;
	}
	
	public ArrayList<NewsArticle> getAllNewsArticlesFromCategory(String category) {
		return newsArticleHandler.getArticlesFromCategory(category);
	}
	
	public NewsArticle getNewsArticleById(int id) {
		for (Map.Entry<String, String> categories : newsCategoryTitles.entrySet()) {
			String category = categories.getValue();
			ArrayList<NewsArticle> newsArticles = newsArticleHandler.getArticlesFromCategory(category);
			for (NewsArticle newsArticle : newsArticles) {
				if (newsArticle.getId() == id) {
					return newsArticle;
				}
			}
		}
		return null;
	}
	
	public Map<String, String> getCategoryTitleFromCategoryURL() {
		return newsCategoryTitles;
	}
	
	public String getLastUpdatedTime() {
		Long time = newsArticleHandler.getUpdatedTime();
		Date convertedTime = new Date(time * 1000);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");  
		String datetimeString = dateFormat.format(convertedTime);
		return datetimeString;
	}
	
}
