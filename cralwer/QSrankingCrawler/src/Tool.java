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
	ArrayList<Integer> pagelist = new ArrayList<Integer>();
	
	
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
		driver.manage().window().maximize();
		
		
	}
	
	
	public ArrayList<String> getVideoURL(String targetSite) throws IOException, InterruptedException{
		
		driver.navigate().to(targetSite);
		
		final ArrayList<String> urlList = new ArrayList<String>();
		
		Thread.sleep(5000);
				
		ArrayList <WebElement> url_list = new ArrayList<WebElement>(driver.findElements(By.cssSelector("a.c-directory-link")));
			
		int size_of_url = url_list.size();
		
		for(int i=0; i<size_of_url; i++){
			
			String url = url_list.get(i).getAttribute("href").toString();
			System.out.println(url);
			urlList.add(url);
			
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
	public void crawlData(final String url, int index) throws IOException{
			
		// open website
		driver.navigate().to(url);
		
		Actions actionObject = new Actions(driver);
		
				
		// sleep 5 seconds to wait until onload
		try{
			Thread.sleep(1000*3);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	
		// move to the location of the element to click dropdown list		
		WebElement element = driver.findElement(By.id("qs-rankings_length")).findElement(By.cssSelector("span.jcf-select-opener"));
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0," + (element.getLocation().y - 100) + ")");
//		element.click();
		actionObject.click(element).perform();
		
		List<WebElement> tempList = new ArrayList<WebElement>();
		tempList = driver.findElement(By.cssSelector("span.jcf-list-content")).findElements(By.tagName("li"));
//		tempList.get(tempList.size()-1).click();
		actionObject.click(tempList.get(tempList.size()-1)).perform();
		
		try{
			Thread.sleep(1000*3);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		ArrayList<Data> univRanking_list = new ArrayList<Data>();
		
		List<WebElement> univList_general = new ArrayList<WebElement>();
		univList_general = driver.findElement(By.tagName("tbody")).findElements(By.tagName("tr"));
		System.out.println("Univ length - " + univList_general.size());
		
		// parsing university name, rank, region(country), and ratings of QS star
		HashMap <String, String> Map_name_country = new HashMap<String, String>();
		HashMap <String, String> Map_name_rating = new HashMap<String, String>();
		for (WebElement univ: univList_general){
			String rank = univ.findElement(By.className("rank")).getText();
			String name = univ.findElement(By.className("uni")).getText();
			String country = univ.findElement(By.className("country")).findElement(By.tagName("img")).getAttribute("alt");
			String rating = univ.findElement(By.className("rating")).getText();
			if(rating.equals(""))
				rating = "0";
			
			Data data = new Data();
			data.rank = rank;
			data.univ_name = name;
			data.country = country;
			data.rating = rating;
			data.year = Integer.toString(index);
			
			Map_name_country.put(name,  country);
			Map_name_rating.put(name, rating);
		
		}
		System.out.println("end of parsing general rank info");
		
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0,0)");
		driver.findElement(By.className("quicktabs-tab-rankings_tabs-1")).click();
		try{
			Thread.sleep(1000*2);
		}catch(Exception e){
			e.printStackTrace();
		}
	
		// move to the location of the element to click dropdown list		
		element = driver.findElement(By.id("qs-rankings-indicators_length")).findElement(By.cssSelector("span.jcf-select-opener"));
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0," + (element.getLocation().y - 100) + ")");
		actionObject.click(element).perform();

		
		tempList = new ArrayList<WebElement>();
		tempList = driver.findElement(By.cssSelector("span.jcf-list-content")).findElements(By.tagName("li"));
		actionObject.click(tempList.get(tempList.size()-1)).perform();
		
		try{
			Thread.sleep(1000*3);
		}catch(Exception e){
			e.printStackTrace();
		}
			
					
		// parsing score of each indicators of ranking per univ
		// overall score, academic rep, employer_rep, faculty rep, citation per fac, int'l fac, int'l stu
		List<WebElement> univList_indicators = new ArrayList<WebElement>();
		univList_indicators = driver.findElement(By.id("qs-rankings-indicators")).
				findElement(By.tagName("tbody")).findElements(By.tagName("tr"));
		System.out.println("indicator - univList size - " + univList_indicators.size());
		
		List<WebElement> indiList = new ArrayList<WebElement>();
		for (WebElement indi : univList_indicators){
			System.out.println(indi.getText());
			
			indiList = indi.findElements(By.tagName("td"));
			
			System.out.println("indicator size - " + indiList.size());
			
			String rank = indiList.get(0).getText();
			String name = indiList.get(1).getText();
			String overall = indiList.get(2).getText();
			String academic_rep = indiList.get(3).getText();
			String employer_rep = indiList.get(4).getText();
			String faculty_stu = indiList.get(5).getText();
			String citation_fac = indiList.get(6).getText();
			String intl_fac = indiList.get(7).getText();
			String intl_stu = indiList.get(8).getText();
//			
//			String overall = indiList.get(2).getText().replace("", "0");
//			String academic_rep = indiList.get(3).getText().replace("", "0");
//			String employer_rep = indiList.get(4).getText().replace("", "0");
//			String faculty_stu = indiList.get(5).getText().replace("", "0");
//			String citation_fac = indiList.get(6).getText().replace("", "0");
//			String intl_fac = indiList.get(7).getText().replace("", "0");
//			String intl_stu = indiList.get(8).getText().replace("", "0");
//			
			String rating = Map_name_rating.get(name);
			String country = Map_name_country.get(name); 
				
			
						
			Data univ = new Data();
			
			univ.year = Integer.toString(index);
			univ.rank = rank;
			univ.univ_name = name;
			univ.rating = rating;
			univ.country = country;
			
			univ.overall_score = overall;
			univ.academic_rep = academic_rep;
			univ.employer_rep = employer_rep;
			univ.faculty_student = faculty_stu;
			univ.citation_rep_fac = citation_fac;
			univ.intl_faculty = intl_fac;
			univ.intl_student = intl_stu;
			
			univRanking_list.add(univ);
			
							
		}
		
		
			
						
		// newCreatedDocument is destination of XML.
		NodeList nodelist=newCreatedDocument.getElementsByTagName("ROOT");
		Node root=nodelist.item(0);
		
		for(Data univ: univRanking_list){
			addNode(root, univ);
		}
		

	
	}
	
	
	public void crawlIndicatorData(final String url, int index) throws IOException{
		
		// open website
		driver.navigate().to(url.concat("#indicator-tab"));
				
		// sleep 5 seconds to wait until onload
		try{
			Thread.sleep(1000*3);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		Actions actionObject = new Actions(driver);
		
		// move to the location of the element to click dropdown list		
		WebElement element = driver.findElement(By.id("qs-rankings-indicators_length")).findElement(By.cssSelector("span.jcf-select-opener"));
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0," + (element.getLocation().y - 50) + ")");
		actionObject.click(element).perform();
//		element.click();
		
		List<WebElement> tempList = new ArrayList<WebElement>();
		tempList = driver.findElement(By.cssSelector("span.jcf-list-content")).findElements(By.tagName("li"));
//		tempList.get(tempList.size()-1).click();
		actionObject.click(tempList.get(tempList.size()-1)).perform();
		
		try{
			Thread.sleep(1000*3);
		}catch(Exception e){
			e.printStackTrace();
		}
			
		
		ArrayList<Data> univRanking_list = new ArrayList<Data>();
	
			
		// parsing score of each indicators of ranking per univ
		// overall score, academic rep, employer_rep, faculty rep, citation per fac, int'l fac, int'l stu
		List<WebElement> univList_indicators = new ArrayList<WebElement>();
		univList_indicators = driver.findElement(By.id("qs-rankings-indicators")).
				findElement(By.tagName("tbody")).findElements(By.tagName("tr"));
		System.out.println("indicator univList size - " + univList_indicators.size());
		
		List<WebElement> indiList = new ArrayList<WebElement>();
		for (WebElement indi : univList_indicators){
			System.out.println(indi.getText());
			
			indiList = indi.findElements(By.tagName("td"));
			
			System.out.println("indicator size - " + indiList.size());
			
			String rank = indiList.get(0).getText();
			String name = indiList.get(1).getText();
			
						
			String overall = indiList.get(2).getText();
			String academic_rep = indiList.get(3).getText();
			String employer_rep = indiList.get(4).getText();
			String faculty_stu = indiList.get(5).getText();
			String citation_fac = indiList.get(6).getText();
			String intl_fac = indiList.get(7).getText();
			String intl_stu = indiList.get(8).getText();
			
			
//			String overall = indiList.get(2).getText().replace("", "0");
//			String academic_rep = indiList.get(3).getText().replace("", "0");
//			String employer_rep = indiList.get(4).getText().replace("", "0");
//			String faculty_stu = indiList.get(5).getText().replace("", "0");
//			String citation_fac = indiList.get(6).getText().replace("", "0");
//			String intl_fac = indiList.get(7).getText().replace("", "0");
//			String intl_stu = indiList.get(8).getText().replace("", "0");
//			
						
			Data univ = new Data();
			
			univ.year = Integer.toString(index);
			univ.rank = rank;
			univ.univ_name = name;
			
			univ.overall_score = overall;
			univ.academic_rep = academic_rep;
			univ.employer_rep = employer_rep;
			univ.faculty_student = faculty_stu;
			univ.citation_rep_fac = citation_fac;
			univ.intl_faculty = intl_fac;
			univ.intl_student = intl_stu;
			
			univRanking_list.add(univ);
			
							
		}
		
		
		// newCreatedDocument is destination of XML.
		NodeList nodelist=newCreatedDocument.getElementsByTagName("ROOT");
		Node root=nodelist.item(0);
		
		for(Data univ: univRanking_list){
			addNode(root, univ);
		}
		

	
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
						
			// rank
			org.w3c.dom.Element ranking = newCreatedDocument.createElement("ranking");
			ranking.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.rank));
			univ_rankings.appendChild(ranking);
			
			// adjusted rank
			org.w3c.dom.Element adj_ranking = newCreatedDocument.createElement("adj_ranking");
			adj_ranking.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.rank.replace("=", "")));
			univ_rankings.appendChild(adj_ranking);
			
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

			// academic_rep
			org.w3c.dom.Element academic_rep = newCreatedDocument.createElement("academic_reputation");
			academic_rep.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.academic_rep));
			univ_rankings.appendChild(academic_rep);
			
			// employer_rep
			org.w3c.dom.Element employer_rep = newCreatedDocument.createElement("employer_reputation");
			employer_rep.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.employer_rep));
			univ_rankings.appendChild(employer_rep);

			// faculty_stu
			org.w3c.dom.Element faculty_stu = newCreatedDocument.createElement("faculty_student_ratio");
			faculty_stu.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.faculty_student));
			univ_rankings.appendChild(faculty_stu);
			
			// citation_rep
			org.w3c.dom.Element citation_rep = newCreatedDocument.createElement("citation_per_faculty");
			citation_rep.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.citation_rep_fac));
			univ_rankings.appendChild(citation_rep);
						
			// intl_fac
			org.w3c.dom.Element intl_fac = newCreatedDocument.createElement("international_faculty");
			intl_fac.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.intl_faculty));
			univ_rankings.appendChild(intl_fac);
			
			// intl_stu
			org.w3c.dom.Element intl_stu = newCreatedDocument.createElement("international_student");
			intl_stu.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.intl_student));
			univ_rankings.appendChild(intl_stu);
										
		}
	}

	
}
