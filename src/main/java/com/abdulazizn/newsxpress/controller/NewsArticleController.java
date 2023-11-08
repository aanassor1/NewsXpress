package com.abdulazizn.newsxpress.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.abdulazizn.newsxpress.model.NewsArticle;
import com.abdulazizn.newsxpress.service.NewsArticleService;

/*
 * Handles URL mapping
 */
@Controller
@RequestMapping("/news")
public class NewsArticleController {
	
	private int pageSize = 10;
	
	@Autowired
	private NewsArticleService newsArticleService;
	
	private int getMaxPages(String category) {
		ArrayList<NewsArticle> articles = newsArticleService.getAllNewsArticlesFromCategory(category);
		int articleCount = articles.size();
		return articleCount / pageSize + (articleCount % pageSize == 0 ? 0 : 1);
    }
	
	private List<NewsArticle> getNewsArticlesPerPage(String category, int pageNumber) {
		ArrayList<NewsArticle> articles = newsArticleService.getAllNewsArticlesFromCategory(category);
		
		int fromIndex = (pageNumber - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, articles.size());
        List<NewsArticle> pageOfArticles = articles.subList(fromIndex, toIndex);
        
        return pageOfArticles;
	}
	
	@GetMapping({"/", "{category}", "{category}/page/{pageNumber}"})
	public String showArticles(@PathVariable(required = false) String category, @PathVariable(required = false) Integer pageNumber, Model model) {
		String categoryURL = "";
		if (category == null) {
			category = "Top Stories";
			categoryURL = "top_stories";
		} else {
			Map<String, String> categoryTitleMap = newsArticleService.getCategoryTitleFromCategoryURL();
			for (Map.Entry<String, String> entry : categoryTitleMap.entrySet()) {
				String categoryURLLink = entry.getKey();
				String categoryURLTitle = entry.getValue();
				if (categoryURLLink.toLowerCase().equals(category.toLowerCase()) == true) {
					category = categoryURLTitle;
					categoryURL = categoryURLLink;
					break;
				}
		    }
		}
		if (pageNumber == null) {
			pageNumber = 1;
		}

		List<NewsArticle> articles = getNewsArticlesPerPage(category, pageNumber);
	    String lastUpdatedTime = newsArticleService.getLastUpdatedTime();
	    
	    model.addAttribute("articles", articles);
	    model.addAttribute("lastUpdatedTime", lastUpdatedTime);
	    model.addAttribute("category", category);
	    model.addAttribute("currentPage", pageNumber);
	    model.addAttribute("maxPages", getMaxPages(category));
	    model.addAttribute("categoryForURL", categoryURL);
	    return "news-feed";
	}
	
	@GetMapping("/id/{id}")
	public String showArticleWithId(@PathVariable Integer id, Model model) {
		NewsArticle article = newsArticleService.getNewsArticleById(id);
		model.addAttribute("article", article);		
		return "news-article";
	}
}
