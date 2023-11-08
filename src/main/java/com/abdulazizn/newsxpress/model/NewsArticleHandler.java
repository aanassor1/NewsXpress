package com.abdulazizn.newsxpress.model;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import jakarta.annotation.PostConstruct;

/*
 * Handles all news articles objects during runtime
 */
@Component
public class NewsArticleHandler {
	
	//	List of all available news articles
	private Map<String, ArrayList<NewsArticle>> newsArticles = new HashMap<>();
	private IDHandler idHandler = new IDHandler();	//	Used to generate unique IDs for the NewsArticle objects
	private final String articlesCSVPath = "C:\\Users\\ninja\\Documents\\Projects\\NewsXpress\\src\\main\\resources\\articlesData.csv";
	private final Long maxSecondsTillUpdate = 3600L; //3600 Seconds (60 Minutes) (L is needed to show its a Long variable)
	private final Map<String, String> newsLinks = Map.of(
			"https://feeds.bbci.co.uk/news/rss.xml", "Top Stories",
			"http://feeds.bbci.co.uk/news/world/rss.xml", "World",
			"http://feeds.bbci.co.uk/news/uk/rss.xml", "UK",
			"http://feeds.bbci.co.uk/news/business/rss.xml", "Business",
			"http://feeds.bbci.co.uk/news/politics/rss.xml", "Politics",
			"http://feeds.bbci.co.uk/news/health/rss.xml", "Health",
			"http://feeds.bbci.co.uk/news/education/rss.xml", "Education & Family",
			"http://feeds.bbci.co.uk/news/science_and_environment/rss.xml", "Science & Environment",
			"http://feeds.bbci.co.uk/news/technology/rss.xml", "Technology",
			"http://feeds.bbci.co.uk/news/entertainment_and_arts/rss.xml", "Entertainment & Arts"
	);
	private Long lastUpdatedTime;
	
	//	Returns the last updated time of articles
	public Long getUpdatedTime() {
		return lastUpdatedTime;
	}
	
	//	Returns the list of newsArticles
	public ArrayList<NewsArticle> getArticlesFromCategory(String category) {
		return newsArticles.get(category);
	}
	
	// Returns the list of all categories
	public Set<String> getCategories() {
		return newsArticles.keySet();
	}
	
	/*
	 * Web scraping task class used for parallel processing
	 */
	private class WebScrapingTask implements Callable<String> {
		private NewsArticle newsArticle; 
		
	    public WebScrapingTask(NewsArticle newsArticle) {
	        this.newsArticle = newsArticle;
	    }

	    @Override
	    public String call() throws Exception {
	    	Document articleDoc = getDoc(newsArticle.getURL());
	    	if (articleDoc.getElementsByTag("article") == null || articleDoc.getElementsByTag("article").size() == 0) { return "Error"; }
			Element article = articleDoc.getElementsByTag("article").first();
			Elements contents = article.getElementsByTag("p");
			String content = "";
			for (Element line : contents) {
				content = content + line.text() + "\n";
			}
			content = content.trim();
			if (content == "") {
				return "Error";
			} else {
				newsArticle.setContent(content);
				
				//Add news article to the news articles list
				String category = newsArticle.getCategory();
				if (newsArticles.get(category) == null) {
					newsArticles.put(category, new ArrayList<>());
				}
				ArrayList<NewsArticle> newsArticleList = newsArticles.get(category);
				newsArticleList.add(newsArticle);
				newsArticles.put(category, newsArticleList);
				
		        return "Success";
			}
	    }
	}
	
	/*
	 * Takes a parameter (String URL) and returns a Document
	 */
	private Document getDoc(String URL) throws IOException {
		Document doc = Jsoup
		        .connect(URL)
		        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36")
		        .get();	
		return doc;
	}
	
	/*
	 * Saves news articles to a CSV file
	 */
	private int saveNewsArticles() throws IOException {
        lastUpdatedTime = Instant.now().getEpochSecond();

		File articlesCSV = new File(articlesCSVPath);
		if ((articlesCSV.exists() && articlesCSV.isFile()) == false) {
			articlesCSV.createNewFile();
		}
        FileWriter articlesCSVFile = new FileWriter(articlesCSV); 
        CSVWriter writer = new CSVWriter(articlesCSVFile); 
        
        Long currentTimeInSeconds = Instant.now().getEpochSecond();
        String[] timeData = {Long.toString(currentTimeInSeconds)};
        writer.writeNext(timeData);
        
        //Loop through the categories in the newsArticles list
		for (Map.Entry<String, ArrayList<NewsArticle>> newsArticlesPair  : newsArticles.entrySet()) {
			ArrayList<NewsArticle> newsArticles = newsArticlesPair.getValue();
			for (NewsArticle newsArticle : newsArticles) {
	        	String[] data = {
	        			String.valueOf(newsArticle.getId()),
	        			newsArticle.getTitle(),
	        			newsArticle.getDesc(),
	        			newsArticle.getURL(),
	        			newsArticle.getContent(),
	        			newsArticle.getPublicationDateTime(),
	        			newsArticle.getCategory()
	        	};
	        	writer.writeNext(data);
	        }
		}
        writer.close();
        return 1; //Success
	}
	
	private boolean isNumeric(String strNum) {
		Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
	    if (strNum == null) {
	        return false; 
	    }
	    return pattern.matcher(strNum).matches();
	}
	
	/*
	 * Reads the articles CSV file (if any) and checks the os time
	 * If the os time compared to the current os time is over a certain amount of seconds
	 * Then web scrape and look for new articles
	 */
	private int checkTimeFromArticlesCSV() throws IOException, CsvException {
		File articlesCSV = new File(articlesCSVPath);
		if ((articlesCSV.exists() && articlesCSV.isFile()) == false) {
			return 1; //No data to read
		}
		CSVReader reader = new CSVReader(new FileReader(articlesCSVPath));
		String prevTimeString = reader.readNext()[0];
		if (isNumeric(prevTimeString) == true) {
			Long prevTime = Long.parseLong(prevTimeString);
	        Long currentTimeInSeconds = Instant.now().getEpochSecond();
	        Long timeDiffInSeconds = currentTimeInSeconds-prevTime;
	        lastUpdatedTime = prevTime;
	        System.out.println("Last update on articles data: " + Long.toString(timeDiffInSeconds) + " seconds ago.");
			if (timeDiffInSeconds > maxSecondsTillUpdate) {
				System.out.println("Updating articles data...");
				return 1; //Valid conditions to create the CSV file
			}
		}
		return 0;
	}
	
	/*
	 * Checks if there's existing article data
	 * If so, populate the news articles list
	 */
	private int checkForArticleDataAndGenerateData() throws IOException, CsvException {
		File articlesCSV = new File(articlesCSVPath);
		if ((articlesCSV.exists() && articlesCSV.isFile()) == true) {
			CSVReader reader = new CSVReader(new FileReader(articlesCSVPath));
			reader.readNext(); //Skip the first line as it includes the time and is irrelevant
			String[] nextLine;
			int articleCount = 0;
			while ((nextLine = reader.readNext()) != null) {
				if (nextLine.length == 7) {
					String title = nextLine[1];
					String link = nextLine[3];
					String pubDate = nextLine[5];
					String description = nextLine[2];
					String content = nextLine[4];
					String category = nextLine[6];
					
					NewsArticle newsArticle = new NewsArticle();
					newsArticle.setId(idHandler.generateId());	
					newsArticle.setTitle(title);
					newsArticle.setURL(link);
					newsArticle.setPublicationDateTime(pubDate);
					newsArticle.setDesc(description);
					newsArticle.setContent(content);
					newsArticle.setCategory(category);
					
					//Add news article to the news articles list
					if (newsArticles.get(category) == null) {
						newsArticles.put(category, new ArrayList<>());
					}
					ArrayList<NewsArticle> newsArticleList = newsArticles.get(category);
					newsArticleList.add(newsArticle);
					newsArticles.put(category, newsArticleList);

					articleCount++;
				}
			}
			System.out.println("Successfully loaded " +articleCount + " news articles.");
			return 1;
		}
		return 0;
	}
	
	/*
	 * Populates newsArticles by web scraping BBC News UK
	 */
	@PostConstruct
	private void generateNewsArticle() throws IOException, CsvException, ExecutionException {
		System.out.println("Generating news articles...");
		if (checkTimeFromArticlesCSV() == 1 || checkForArticleDataAndGenerateData() == 0) { //If it is 0, that means there is no data
			int badArticleCount = 0;
			int goodArticleCount = 0;
			int i = 1;
			for (Map.Entry<String, String> newsPair : newsLinks.entrySet()) { //Loop through the different news links/categories
				String newsCategory = newsPair.getValue();
				String newsURL = newsPair.getKey();
				System.out.println("Currently accessing(" + i + "/" + newsLinks.size() + "):" + newsURL); 
				
				Document articleFeedDoc = getDoc(newsURL); //BBC News RSS Feed Doc
				Elements articles = articleFeedDoc.getElementsByTag("item");
				List<WebScrapingTask> tasks = new ArrayList<>();
				
				for (Element article : articles) { 	//Loop through the RSS feed and get the title, desc and URL for use later
					Element title = article.getElementsByTag("title").first();
					Element pubDate = article.getElementsByTag("pubDate").first();
					Element link = article.getElementsByTag("link").first();
					Element description = article.getElementsByTag("description").first();
					
					NewsArticle newsArticle = new NewsArticle();
					newsArticle.setId(idHandler.generateId());	
					if (title == null || link == null || pubDate == null || description == null) {
						badArticleCount++;
						return;
					}
					newsArticle.setTitle(title.text());
					newsArticle.setURL(link.text());
					newsArticle.setPublicationDateTime(pubDate.text());
					newsArticle.setDesc(description.text());
					newsArticle.setCategory(newsCategory);
					
					tasks.add(new WebScrapingTask(newsArticle)); //Add web scraping task to task schedule
				}
				
				long startTime = System.currentTimeMillis();
				int numThreads = Runtime.getRuntime().availableProcessors();; // Number of parallel threads
		        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
				List<Future<String>> results = new ArrayList<>();

				//	Execute parallel processing to web scrape the news articles
		        try {
		            results = executorService.invokeAll(tasks);
		            
		            int badScrapeCount = 0;
		            for (Future<String> result : results) {
		                String resultStatus = result.get();
		               if (resultStatus.equals("Error") == true) {
		            	   badArticleCount++;
		            	   badScrapeCount++;
		               } else if (resultStatus.equals("Success") == true) {
		            	   goodArticleCount++;
		               }
		            }
		            System.out.println("Removed " + Integer.toString(badScrapeCount) + " bad articles");
		        } catch (InterruptedException e) {
		            e.printStackTrace();
		        } finally {
		            executorService.shutdown();
		        }
		        
		        // Record the end time
		        long endTime = System.currentTimeMillis();
		        
		        // Calculate the elapsed time in milliseconds
		        long elapsedTime = (endTime - startTime);
		        
		        System.out.println("Finished accessing(" + i + "/" + newsLinks.size() + "):" + newsURL);
		        System.out.println("Total time taken: " + elapsedTime + " ms");
		        i++;
			}
			System.out.println("Successfully generated " + goodArticleCount + " news articles.");
			System.out.println("Removed a total of " + badArticleCount + " bad articles.");
			
			/*
			 * Save News articles to CSV file
			 */
			if (saveNewsArticles() != 1) {
				System.out.println("An error has occured while saving news article objects..");
			} else {
				System.out.println("Successfully saved news article objects to articles data.");
			}
		}	
	}
}
