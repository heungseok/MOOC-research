import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;



public class Tool {
	
	boolean endData=false;
	int page = 1;
	int index = 0;
	HashMap <String, String> Map_subject_area = new HashMap <String, String>();
		
	
	Data courseData = new Data();
//	final WebDriver driver = new FirefoxDriver();
	int urlSize = 0;
	
	WebDriver driver;

	public void initDriver(){
		
		System.setProperty(
				"webdriver.chrome.driver",
				"D:/chromedriver_win32/chromedriver.exe"
		);
		
		driver = new ChromeDriver();
		driver.get("http://www.google.com");
//		driver.manage().window().maximize();
		
		
	}
	
	
	public ArrayList<String> getSubjectURL(String targetSite) throws IOException, InterruptedException{
		
		driver.navigate().to(targetSite);
		
		final ArrayList<String> urlList = new ArrayList<String>();
		
		Thread.sleep(3000);
				
		ArrayList <WebElement> area_list = new ArrayList<WebElement>(driver.findElements(By.cssSelector("div.land-box")));
			
		int size_of_url = area_list.size();
		
		for(WebElement element : area_list){
			
			String area = element.findElement(By.cssSelector("div.link")).findElement(By.tagName("h2")).getText();
			System.out.println("area- " + area);
			
			ArrayList<WebElement> temp_list = new ArrayList<WebElement>
			(element.findElement(By.cssSelector("div.link")).findElements(By.tagName("a")));
			
			
			for(WebElement ele : temp_list){
				String url = ele.getAttribute("href");
				urlList.add(url);
				String[] splited_str = url.split("/");
				String subject = splited_str[splited_str.length-1];
				Map_subject_area.put(subject, area);
				
				System.out.println(ele.getAttribute("href"));
			}
			
					
		}
		
		
		urlSize = urlList.size();
		System.out.println("size of url - " + urlSize);
		
		return urlList; 
		
	}
		
	/*
	 * End of get All url function
	 */
	
	/*
	 * creatDomRoot
	 * 
	 */
		
	org.w3c.dom.Document newCreatedDocument = null;
	synchronized public org.w3c.dom.Document createDomRoot(){
		System.out.println("----------------Root create-----------------");

		try {
			newCreatedDocument = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		org.w3c.dom.Node root = newCreatedDocument.createElement("ROOT");
		newCreatedDocument.appendChild(root);
		return newCreatedDocument;
		
	}
	
	// To prevent occurring null pointer exception during transforming XML
	public static Text createTextNodeWithoutNull(Document doc, String str){
		Text textNode;
		if(str != null) textNode = doc.createTextNode(str);
		else textNode = doc.createTextNode("null");
		
		return textNode;
	}
	
	/*
	 * Crawl data.
	 * 
	 */
	public void crawlData(final String url, int index) throws IOException, InterruptedException{
			
		// open website
		driver.navigate().to(url);
		
		String [] splited_string = url.split("/");
		String subject = splited_string[(splited_string.length-1)];
		System.out.println("Year- " + index + "/ subject- " + subject);
		
		Actions actionObject = new Actions(driver);
		
		// sleep 5 seconds to wait until onload
		Thread.sleep(1000*2);		
			
		// move to the location of the element to click "show more ranks" until all universities show up	
		WebElement element = driver.findElement(By.cssSelector("a.get-more.btn.dark-gray-bg"));
		while(true){
			
			if(element.isDisplayed()){
				((JavascriptExecutor) driver).executeScript("window.scrollTo(0," + (element.getLocation().y - 50) + ")");
				actionObject.click(element).perform();
			}else{
				break;
			}
			Thread.sleep(1000);
		}
				
		ArrayList<Data> univRanking_list = new ArrayList<Data>();
		
		List<WebElement> univList_general = new ArrayList<WebElement>();
		univList_general = driver.findElement(By.tagName("tbody")).findElements(By.tagName("tr"));
		System.out.println("Univ length - " + univList_general.size());
		
		// parsing university name, rank, region(country), and ratings of QS star
		// and add every ranking data to univRanking list 
		for (WebElement univ: univList_general){
			String rank = univ.findElement(By.className("ranking")).getText();
			String name = univ.findElement(By.className("uni")).getText();
			String country = univ.findElement(By.className("country")).findElement(By.tagName("img")).getAttribute("alt");
			String rating = String.valueOf(univ.findElement(By.className("rating")).findElements(By.cssSelector("span.qsstar")).size());
			
			String overallscore = univ.findElement(By.cssSelector("span.result")).getText();
			
			if(overallscore.equals(""))
				overallscore = "NaN";
			
			if(rating.equals(""))
				rating = "0";

			Data data = new Data();
			data.rank = rank;
			data.univ_name = name;
			data.country = country;
			data.rating = rating;
			data.year = Integer.toString(index);
			data.univ_subject = subject;
			data.subject_area = Map_subject_area.get(subject);
			data.overall_score = overallscore;
			
			univRanking_list.add(data);
			
					
		}
		System.out.println("end of parsing general rank info");
		
						
		// newCreatedDocument is destination of XML.
		NodeList nodelist=newCreatedDocument.getElementsByTagName("ROOT");
		Node root=nodelist.item(0);
		
		for(Data univ: univRanking_list){
			addNode(root, univ);
		}
		
//		if(driver.findElement(arg0)_hj-f5b2a1eb-9b07_survey_invite_container
		

	
	}

	
	public void addNode(Node root, Data data){
		// making dom elements
		org.w3c.dom.Element univ_rankings = newCreatedDocument.createElement("univRankingInfo");
		root.appendChild(univ_rankings);
		{
			
			// year
			org.w3c.dom.Element ranking_year = newCreatedDocument
					.createElement("year");
			ranking_year.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.year));
			univ_rankings.appendChild(ranking_year);
			
			// area
			org.w3c.dom.Element subject_area = newCreatedDocument
					.createElement("area");
			subject_area.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.subject_area));
			univ_rankings.appendChild(subject_area);
			
			// subject
			org.w3c.dom.Element subject = newCreatedDocument
					.createElement("subject");
			subject.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.univ_subject));
			univ_rankings.appendChild(subject);
						
			// rank
			org.w3c.dom.Element ranking = newCreatedDocument.createElement("ranking");
			ranking.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.rank));
			univ_rankings.appendChild(ranking);
			
			// name
			org.w3c.dom.Element univ_name = newCreatedDocument.createElement("univ_title");
			univ_name.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.univ_name));
			univ_rankings.appendChild(univ_name);
			
			// country
			org.w3c.dom.Element country = newCreatedDocument.createElement("country");
			country.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.country));
			univ_rankings.appendChild(country);
						
			// rating
			org.w3c.dom.Element rating = newCreatedDocument.createElement("QS_stars_rating");
			rating.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.rating));
			univ_rankings.appendChild(rating);
			
			// Overall score
			org.w3c.dom.Element overall_score = newCreatedDocument.createElement("ovarall_score");
			overall_score.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.overall_score));
			univ_rankings.appendChild(overall_score);


		}
	}

	
}
