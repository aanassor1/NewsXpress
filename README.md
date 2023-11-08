# NewsXpress

## Author
Abdul-Aziz Nassor

## Description
A spring web application created in Java that displays web articles of various different categories to the user. These articles are web scraped from the official BBC News website and there are different categories that the user can have access to such as: world news, sports, technology etc. Additionally, to make the website look appealing to the user, I have also styled the website using CSS.\n
Whenever the application starts, it checks for 2 conditions; if there is an articles data CSV file and how long ago the last update to this file was. If there is none, or if the last update was over 60 minutes ago, it will begin to web scrape. This uses multi threading processing to speed up the process.
Once web scraping is complete, it saves the articles data to a CSV file to be used for later.

## What I learnt
* Java
* Multithreading
* Spring
* HTML
* CSS
* Controllers
* Data Manpiulation
* Web Scraping
  
## Installation
Go onto Github, find the project and clone the repository.
Afterwards, open bash, locate the destinated file with 'cd', and use git clone on the cloned repository.

```bash
git clone https://github.com/aanassor1/NewsXpress.git
```

Once the repository is installed, open the project in a Java IDE such as Eclipse, install Gradle and the dependencies mentioned in the build.gradle file. After, run the program, allow the program to web scrape which can take upto a minute, and then open up "http://localhost:8080/news/".
