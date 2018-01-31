import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;



public class Tool {
	
	boolean endData=false;
	int page = 1;
	int index = 0;
	final String base_url = "https://www.edx.org";
	
	ArrayList<String> errorURLs = new ArrayList<String>();
	
	
	Data courseData = new Data();
	
	int urlSize = 0;
	
	WebDriver driver;

	public void initDriver() throws InterruptedException{
		
		System.setProperty(
				"webdriver.chrome.driver",
				"D:/chromedriver_win32/chromedriver.exe"
		);
		
		
		ChromeOptions options = new ChromeOptions();
		options.addArguments("start-maximized");
		driver = new ChromeDriver(options);
				
		driver.get(base_url);
//		driver.manage().window().maximize();
		
		
//		driver.navigate().to(base_url);
//		Actions actionObject = new Actions(driver);
		Thread.sleep(4000);
		
		// currently the coursera platform block the login-access through selenuim web-driver 
		// so, sleep the program and I manually login to the platform
		// *********************** PLEASE LOGIN MANUANLLY!!! *************************
	
		
	}
	
	public void quitDriver(){
		
		driver.quit();
	}
	
	
	
	public ArrayList<String> getVideoURL(String targetSite) throws IOException, InterruptedException{
		
		
		driver.navigate().to(targetSite);
		
		ArrayList <WebElement> pageList = new ArrayList<WebElement>(driver.findElements(By.cssSelector("a.box.number")));
		int pageSize = Integer.valueOf(pageList.get(pageList.size()-1).getText());
		System.out.println("size of pagelist: " + pageSize);
		
		Connection connection;
		String url;
		
		
		final ArrayList<String> urlList = new ArrayList<String>();
		
//		for(int i=1; i<2; i++){
		for(int i=1; i<pageSize+1; i++){
			
			// parse course url using Selenuim
			// driver.navigate().to(targetSite.concat("?page=").concat(String.valueOf(i)));
			
			// parse course url using Jsoup
			url = targetSite.concat("?page=").concat(String.valueOf(i));
			System.out.println("current page: " + i);
			
						
			
			connection = Jsoup.connect(url).timeout(10*1000);
			org.jsoup.nodes.Document document = null;
					
			Thread.sleep(200);
			
			Elements course_urls;
			// connection.get 또는 element parsing에서 exception 발생할 경우 WebDriver로 parsing
			try{
				document =  connection.get();
				course_urls = document.select("a.c-directory-link");
			}catch(Exception e){
				System.out.println("Jsoup connection Error occurred at page-"+i);
				driver.get(url);
				Thread.sleep(3*1000);
				document = Jsoup.parse(driver.getPageSource());
				course_urls = document.select("a.c-directory-link");
				
			}
			
			
			for (int j=0; j< course_urls.size(); j++){
//				System.out.println(course_urls.get(j).attr("href"));
				urlList.add(course_urls.get(j).attr("href"));
			}
						
		}
		
		System.out.println("all courses url parsed");
		System.out.println("size of url-" + urlList.size());
				
			
		return urlList; 
		
	}
	
	public void writeCourseURL(ArrayList<String> urlList) throws Exception{
		
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		Date now = new Date();
				
		OutputStream output = new FileOutputStream("Coursera_coursesURL_" + format.format(now) + ".txt", true);
		
		for(String url : urlList){
			System.out.println(url);
			try{
				output.write((url+'\n').getBytes());
			}catch(Exception e){
				
			}
							
		}
		output.close();
	}
	
	
	public void write_ERROR_CourseURL() throws Exception{
		
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		Date now = new Date();
				
		OutputStream output = new FileOutputStream("ParsingErrorURL_" + format.format(now) + ".txt", true);
		
		for(String url : errorURLs){
			System.out.println(url);
			try{
				output.write((url+'\n').getBytes());
			}catch(Exception e){
				
			}
							
		}
		output.close();
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
		String targetURL = url;
		
		
		try{
			driver.navigate().to(targetURL);
			
		}catch(TimeoutException e){
//			((JavascriptExecutor) driver).executeScript("window.stop();");
			
			// add the url to the errorURLs list and return;
			errorURLs.add(targetURL);
			return;
			
		}
		
		
				
		// sleep 3 seconds to wait until onload
		try{
			Thread.sleep(1000*3);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		// newCreatedDocument is destination of XML.
		NodeList nodelist=newCreatedDocument.getElementsByTagName("ROOT");
		Node root=nodelist.item(0);
				
		
		Data data = new Data();
		
		data.id = Integer.toString(index);
		
		// assign course url 
		data.url = targetURL;
		
		
		
		
		
		// 2. Connect to edX.org
		String coursera_url;
		try{
			coursera_url = driver.findElements(By.cssSelector("div.course-data-button")).get(0).findElement(By.tagName("a")).getAttribute("href");
		}catch (Exception e){
			errorURLs.add(targetURL);
			return;
		}
		driver.navigate().to(coursera_url);
		// sleep 3 seconds to wait until onload
		try{
			Thread.sleep(1000*3);
		}catch(Exception e){
			e.printStackTrace();
		}
			
		
					
		if(driver.findElements(By.cssSelector("div.course-start")).size() > 0 ){
			data.status = driver.findElement(By.cssSelector("div.course-start")).getText().toString();
		}else{
			data.status = "enrollment_closed";
		}
		
		
//		System.out.println(data.status);

	
		ArrayList <WebElement> metadata = new ArrayList <WebElement> (driver
				.findElements(By.tagName("meta")));
		
		System.out.println("meta data size - " + metadata.size());
		
		
		
		for(int i=0; i<metadata.size(); i++){
			WebElement element = metadata.get(i);
			
			if(element.getAttribute("property") == null){
				continue;
			}
								
			if(element.getAttribute("property").equals("og:title")){
				//course title
				data.title = element.getAttribute("content").toString();
				System.out.println("title - " +data.title);
				
			}else if(element.getAttribute("property").equals("og:description")){
				//course intro
				data.intro = element.getAttribute("content").toString();
				System.out.println("intro - " +data.intro);
				
			}else if(element.getAttribute("property").equals("article:published_time")){
				// course published time
				data.date = element.getAttribute("content").toString();
				System.out.println("published date - " +data.date);
				
				// break loop when assign three values
				break;
			}
			
		}

		// Parsing course-Summary
		//System.out.println(driver.findElement(By.id("course-summary-area")).findElements(By.cssSelector("li")).get(1).getText());
		
//		if( driver.findElements(By.cssSelector("div.course-overview-area")).size() < 1 ){
		if( driver.findElements(By.cssSelector("div.course-side-area")).size() > 0 ){
			ArrayList <WebElement> Summary = new ArrayList <WebElement> (driver.findElement(By.id("course-summary-area"))
					.findElements(By.cssSelector("li")) );
			
			for(int i=0; i<Summary.size(); i++){
				// Sometimes attributes are not assigned with 'data-field'
				if(Summary.get(i).getAttribute("data-field") == null){
					continue;
				}
				
				if(Summary.get(i).getAttribute("data-field").equals("length")){
					data.length = Summary.get(i).getText().replaceAll("Length: ", "");
					System.out.println("length - " + data.length);
					
				}else if(Summary.get(i).getAttribute("data-field").equals("effort")){
					data.effort = Summary.get(i).getText().replaceAll("Effort: ", "");
					System.out.println("effort - " + data.effort);
				}		
				else if(Summary.get(i).getAttribute("data-field").equals("school")){
					String school_url = Summary.get(i).findElement(By.tagName("a")).getAttribute("href");
					String [] temp = school_url.split("/");
					String school_name = temp[temp.length-1];
					data.school = school_name;
					 				
					System.out.println("school - " + data.school);
					
					
					
				}else if(Summary.get(i).getAttribute("data-field").equals("subject")){
					data.subject = Summary.get(i).getText().replaceAll("Subject: ", "");
					System.out.println("subject - " + data.subject);
				}else if(Summary.get(i).getAttribute("data-field").equals("price")){
					data.price = Summary.get(i).getText().replace("Price: ", "");
					System.out.println("Price - " + data.price);
					
				}else if(Summary.get(i).getAttribute("data-field").equals("level")){
					data.level = Summary.get(i).getText().replace("Level: ", "");
					System.out.println("level - " + data.level); 
				}else if(Summary.get(i).getAttribute("data-field").equals("language")){
				
					try{
						data.language = Summary.get(i).getText().replaceAll("Languages: ", "");
					}catch(Exception e){
						data.language = "null";
					}
					// break loop when assign three values
					break;
				}else if(Summary.get(i).getAttribute("data-field").equals("video-transcript")){
					try{
						data.transcript = Summary.get(i).findElement(By.className("block-list__desc")).getText();
					}catch(Exception e){
						data.transcript = "null";
					}
				}
				
			}
			
					
			
			
		}else if( driver.findElements(By.cssSelector("div.course-overview-area")).size() > 0 ){
			
			ArrayList <WebElement> Summary = new ArrayList <WebElement> (driver.findElement(
					By.cssSelector("ul.overview-list.block-list.content-block")).findElements(By.cssSelector("li")) );
			
			for(int i=0; i<Summary.size(); i++){
				// Sometimes attributes are not assigned with 'data-field'
				if(Summary.get(i).getAttribute("data-field") == null){
					continue;
				}
				
				if(Summary.get(i).getAttribute("data-field").equals("length")){
					data.length = Summary.get(i).getText().replaceAll("Length: ", "");
					System.out.println("length - " + data.length);
					
				}else if(Summary.get(i).getAttribute("data-field").equals("effort")){
					data.effort = Summary.get(i).getText().replaceAll("Effort: ", "");
					System.out.println("effort - " + data.effort);
				}		
				else if(Summary.get(i).getAttribute("data-field").equals("school")){
					String school_url = Summary.get(i).findElement(By.tagName("a")).getAttribute("href");
					String [] temp = school_url.split("/");
					String school_name = temp[temp.length-1];
					data.school = school_name;
					 				
					System.out.println("school - " + data.school);
					
					
					
				}else if(Summary.get(i).getAttribute("data-field").equals("subject")){
					data.subject = Summary.get(i).getText().replaceAll("Subject: ", "");
					System.out.println("subject - " + data.subject);
				}else if(Summary.get(i).getAttribute("data-field").equals("price")){
					data.price = Summary.get(i).getText().replace("Price: ", "");
					System.out.println("Price - " + data.price);
					
				}else if(Summary.get(i).getAttribute("data-field").equals("level")){
					data.level = Summary.get(i).getText().replace("Level: ", "");
					System.out.println("level - " + data.level); 
				}else if(Summary.get(i).getAttribute("data-field").equals("language")){
				
					try{
						data.language = Summary.get(i).getText().replaceAll("Languages: ", "");
					}catch(Exception e){
						data.language = "null";
					}
					// break loop when assign three values
					break;
				}else if(Summary.get(i).getAttribute("data-field").equals("video-transcript")){
					try{
						data.transcript = Summary.get(i).findElement(By.className("block-list__desc")).getText();
					}catch(Exception e){
						data.transcript = "null";
					}
				}
				
			}
			

			// institution, subject parsing
			ArrayList <WebElement> more_info = new ArrayList<WebElement> (driver.findElements(By.cssSelector("div.accordion-panel")));
			
			// get last index of more_info
			ArrayList <WebElement> second_info = new ArrayList<WebElement> (more_info.get(more_info.size()-1).findElements(By.tagName("li")));
			more_info = null;
			
			for (int i=0; i<second_info.size(); i++){
				
					
				if(second_info.get(i).getAttribute("data-field").equals("school")){
					data.school = second_info.get(i).getText().replaceAll("Institution", "");
					System.out.println("school - " + data.school);
								
				}else if(second_info.get(i).getAttribute("data-field").equals("subject")){
					data.subject = second_info.get(i).getText().replaceAll("Subject", "");
					System.out.println("subject - " + data.subject);
				
					
				}
				
			}
			
			
		}else{
			errorURLs.add(targetURL);
			return;
		}
			
		
				

		// making dom elements
		
		org.w3c.dom.Element course_info = newCreatedDocument.createElement("CourseInfo");
						
		root.appendChild(course_info);
		{
			
			// index
			org.w3c.dom.Element course_id = newCreatedDocument
					.createElement("course_id");
			course_id.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.id));
			course_info.appendChild(course_id);
						
			// title
			org.w3c.dom.Element course_title = newCreatedDocument.createElement("title");
			course_title.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.title));
			course_info.appendChild(course_title);
			
			// published_date
			org.w3c.dom.Element published_date = newCreatedDocument.createElement("published_date");
			published_date.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.date));
			course_info.appendChild(published_date);

			// course intro
			/*
			org.w3c.dom.Element course_intro = newCreatedDocument.createElement("course_intro");
			course_intro.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.intro));
			course_info.appendChild(course_intro);
			*/
			// length
			org.w3c.dom.Element course_length = newCreatedDocument.createElement("course_length");
			course_length.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.length));
			course_info.appendChild(course_length);
			
			// effort
			org.w3c.dom.Element course_effort = newCreatedDocument.createElement("effort");
			course_effort.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.effort));
			course_info.appendChild(course_effort);

			// subject
			org.w3c.dom.Element course_subject = newCreatedDocument.createElement("subject");
			course_subject.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.subject));
			course_info.appendChild(course_subject);
			
			// price
			org.w3c.dom.Element course_price = newCreatedDocument.createElement("price");
			course_price.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.price));
			course_info.appendChild(course_price);
			
			// level
			org.w3c.dom.Element course_level = newCreatedDocument.createElement("level");
			course_level.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.level));
			course_info.appendChild(course_level);

			// school
			org.w3c.dom.Element course_school = newCreatedDocument.createElement("school");
			course_school.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.school));
			course_info.appendChild(course_school);
			
			// language
			org.w3c.dom.Element course_language = newCreatedDocument.createElement("language");
			course_language.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.language));
			course_info.appendChild(course_language);
			
			// url
			org.w3c.dom.Element course_url = newCreatedDocument.createElement("url");
			course_url.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.url));
			course_info.appendChild(course_url);
			
			// status
			org.w3c.dom.Element status = newCreatedDocument.createElement("status");
			status.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.status));
			course_info.appendChild(status);

			
//			// instructors
//			org.w3c.dom.Element instructor_size = newCreatedDocument.createElement("instructor_size");
//			instructor_size.appendChild(createTextNodeWithoutNull(newCreatedDocument, String.valueOf(data.instructors.size())));
//			course_info.appendChild(instructor_size);
//						
//			// description (intro + description + learnAbout)
//			org.w3c.dom.Element course_description = newCreatedDocument.createElement("description");
//			course_description.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.intro + data.description + data.learnAbout));
//			course_info.appendChild(course_description);
			

											
		}

		if(index == urlSize -1){
			System.out.println("Last page");
			driver.close();

			
			
			
		}
	
	}

	public ArrayList<String> readCourseURL() throws Exception{
		
		ArrayList<String> URLlist = new ArrayList<String>();
		
//		FileInputStream inputFile = new FileInputStream("./coursera_url_for_recrawling.txt");
		FileInputStream inputFile = new FileInputStream("./classcentral_url_for_recrawling_20170719_edx.txt");
		
		// Construct BufferReader from inputStreamReader
		BufferedReader fileReader = new BufferedReader(new InputStreamReader(inputFile));
		
		String line = null;
		
		// read url line by line 
		while ( (line = fileReader.readLine()) != null){
			URLlist.add(line);
		}
		
		fileReader.close();
	
		return URLlist;
	}
	

	
}
