import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamResult;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


public class Tool {
	
	boolean endData=false;
	int page = 1;
	int index = 0;
	ArrayList<Integer> pagelist = new ArrayList<Integer>();
	ArrayList<String> instructorList = new ArrayList<String>();
	
	ArrayList<String> errorURLs = new ArrayList<String>();
	
	Data courseData = new Data();

	WebDriver driver;
	int urlSize = 0;
	
	
	public void initDriver(){
		
		System.setProperty(
				"webdriver.chrome.driver",
				"D:/chromedriver_win32/chromedriver.exe"
		);
		
		driver = new ChromeDriver();

		
		
		
	}
	
	
	public void quitDriver(){
		
		driver.quit();
	}
	
	
	public ArrayList<String> getVideoURL(String targetURL) throws Exception{
		
		driver.navigate().to(targetURL);
//		driver.manage().window().maximize();
		
		
		final ArrayList<String> urlList = new ArrayList<String>();

		
		boolean plag = false;
				
		float current_position = 0f;		
		float position = 10000;
		
		// page span to load all data 
		while(!plag){
			
			scrollingToBottomofAPage(driver);
			checkPopup(driver);
			
			WebElement loadCourses = driver.findElement(By.id("show-more-courses"));
			
			
			if( loadCourses.getCssValue("display").equals("none")) {
				plag = true;
				break;
				
			}else{
								
				System.out.println(loadCourses.getCssValue("display") );
				loadCourses.click();
				position = position + position * 0.5f;
				
				try {
					Thread.sleep(2000);
					checkPopup(driver);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}				
					
			

		}
		
		ArrayList <WebElement> url_list = new ArrayList<WebElement>(driver.findElements(By.className("course-name")));
		
		for(int i=0; i<url_list.size(); i++){
			if( url_list.get(i).getAttribute("class").equals("course-name") ){
//				System.out.println(url_list.get(i).getAttribute("href"));
				urlList.add(url_list.get(i).getAttribute("href"));
			}
			
			
		}
						
		
//		System.out.println("size of url - " + urlList.size());
		
		return urlList; 
		
	}
	
	public void checkPopup(WebDriver driver) throws Exception{
		if(driver.findElements(By.className("modal-open")).size() > 0){
			driver.findElements(By.cssSelector("a.modal-close-button")).get(0).click();
			Thread.sleep(2000);
			
		}
		
	}
	
	public void scrollingToBottomofAPage(WebDriver driver){
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
	}
	
	public void writeCourseURL(ArrayList<String> urlList) throws Exception{
		
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		Date now = new Date();
				
		OutputStream output = new FileOutputStream("ClassCentral_coursesURL_" + format.format(now) + ".txt", true);
		
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
	
	public ArrayList<String> readCourseURL() throws Exception{
		
		ArrayList<String> URLlist = new ArrayList<String>();
		
		FileInputStream inputFile = new FileInputStream("./ClassCentral_coursesURL_" + "20170504" + ".txt");
		
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
	public static Text createTextNodeWithoutNull(org.w3c.dom.Document newCreatedDocument2, String str){
		Text textNode;
		if(str != null) textNode = ((org.w3c.dom.Document) newCreatedDocument2).createTextNode(str);
		else textNode = ((org.w3c.dom.Document) newCreatedDocument2).createTextNode("null");
		
		return textNode;
	}
	/*
	 * Crawl data.
	 * 
	 */
	
	public void crawlData_Jsoup(final String url, final Integer index) throws Exception{
		
		Connection connection;
		
		
		connection = Jsoup.connect(url).timeout(10*1000);
		Document document = null;

		
								
		try {
			document = (Document) connection.get();
		} catch (Exception e) {
			System.out.println("connection error at url: " + url);
			e.printStackTrace();
			errorURLs.add(url);
			return;
		}
		
		
		Element info_element;
		try{
			info_element = document.select("div.course-info").get(0);
			
		}catch(Exception e){
			driver.get(url);
			Thread.sleep(10*1000);
			checkPopup(driver);
			document = Jsoup.parse(driver.getPageSource());
			info_element = document.select("div.course-info").get(0);
			
			
//			driver.quit();
						
			errorURLs.add(url);
		}
		
		
		
		// newCreatedDocument is destination of XML.
		NodeList nodelist=newCreatedDocument.getElementsByTagName("ROOT");
		Node root=nodelist.item(0);
				
		
		Data data = new Data();
						
		data.url = url;
		
		data.title = info_element.select("h1.course-title").get(0).text();
		data.description = info_element.select("div.course-desc").get(0).text();
						
		data.reviewCount = document.select("span.review-count").get(0).text();
			
		data.review_sum = document.select("span.average-rating.course-rating").attr("data-score").toString();
		
		
				
//		WebElement detailed_info_element = driver.findElement(By.cssSelector("div.course-data"));
		Element detailed_info_element = document.select("div.course-data").get(0);
		
		Elements detailed_info = detailed_info_element.select("div.course-data-row");
		
		
		for(int i=1; i<detailed_info.size(); i++){
			Element element = detailed_info.get(i);
			
			String type = detailed_info.get(i).attr("class");
					
			
			if(type.equals("course-data-row course-pace")){
				// course pace
				System.out.println("course-pace" +element.text());
				data.pace = element.getElementsByTag("a").get(0).text();
				
			}else if(type.equals("course-data-row course-subject")){
				// subject
				System.out.println("course-subject" +element.text());
				
				data.subject = element.getElementsByTag("a").get(0).text();
				
			}else if(type.equals("course-data-row course-institution")){
				// school or institution
				System.out.println("course-institution" +element.text());
				data.school = element.getElementsByTag("a").get(0).text();
				
			}else if(type.equals("course-data-row course-provider")){
				// provider
				System.out.println("course-provider" +element.text());
				data.provider = element.getElementsByTag("a").get(0).text();
				if(data.provider.equals("Coursera")){
					data.title.replace("Coursera: ", "");
				}else if(data.provider.equals("edX")){
					data.title.replace("edX: ", "");
				}
				
			}else if(type.equals("course-data-row course-language")){
				// language
				System.out.println("language" +element.text());
				data.language = element.getElementsByTag("a").get(0).text();
				
								
			}else if(type.equals("course-data-row course-certificates")){
				// certification available
				System.out.println("certificate avail" +element.text());
				data.certification = element.getElementsByClass("course-data-value").get(0).text();
								
			}else if(type.equals("course-data-row course-hours")){
				// courses hour
				System.out.println("course-hours" + element.text());
				data.course_hour = element.getElementsByClass("course-data-value").get(0).text();
				
			}else if(type.equals("course-data-row course-sessions")){
				// courses sessions which include when the course were opened
				data.sessionList = new ArrayList<String>();
				Elements templist = element.getElementsByTag("option") ;
				
				for(Element temp: templist){
					data.sessionList.add(temp.attr("content"));
					
				}
				// add each session to list
				
				 
				if( element.select("span.course-length").size() > 0 ){
					data.course_length = element.select("span.course-length").text();
				}else{
					data.course_length = "none";
				}
				
//				System.out.println(element.getText());
				
			}else{
				continue;
			}
			
			
		}

		
		
		
		// making dom elements
		
		org.w3c.dom.Element course_info = newCreatedDocument.createElement("CourseInfo");
						
		root.appendChild(course_info);
		{
			
									
			// title
			org.w3c.dom.Element course_title = newCreatedDocument.createElement("title");
			course_title.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.title));
			course_info.appendChild(course_title);
			
			// provider
			org.w3c.dom.Element provider = newCreatedDocument.createElement("provider");
			provider.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.provider));
			course_info.appendChild(provider);

						
			// subject
			org.w3c.dom.Element course_subject = newCreatedDocument.createElement("subject");
			course_subject.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.subject));
			course_info.appendChild(course_subject);
						
			// school
			org.w3c.dom.Element course_school = newCreatedDocument.createElement("school");
			course_school.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.school));
			course_info.appendChild(course_school);
			
			// rating
			org.w3c.dom.Element course_rating = newCreatedDocument.createElement("rating");
			course_rating.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.review_sum));
			course_info.appendChild(course_rating);
			
			org.w3c.dom.Element course_reviewCount = newCreatedDocument.createElement("review_count");
			course_reviewCount.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.reviewCount));
			course_info.appendChild(course_reviewCount);
			
			
			// language
			org.w3c.dom.Element course_language = newCreatedDocument.createElement("language");
			course_language.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.language));
			course_info.appendChild(course_language);
			
			// course hour
			org.w3c.dom.Element course_hour = newCreatedDocument.createElement("course_hours");
			course_hour.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.course_hour));
			course_info.appendChild(course_hour);
			
			// course sessions
			org.w3c.dom.Element course_sessions = newCreatedDocument.createElement("course_sessions");
			for(int i=0; i<data.sessionList.size(); i++){
				org.w3c.dom.Element session = newCreatedDocument.createElement("session");
				session.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.sessionList.get(i)));
				course_sessions.appendChild(session);
			}
			course_info.appendChild(course_sessions);
			
			// course length
			org.w3c.dom.Element course_length = newCreatedDocument.createElement("course_length");
			course_length.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.course_length));
			course_info.appendChild(course_length);
			
			
			// pace
			org.w3c.dom.Element status = newCreatedDocument.createElement("pace");
			status.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.pace));
			course_info.appendChild(status);
			
			// certification available
			org.w3c.dom.Element cert_avail = newCreatedDocument.createElement("certification_available");
			cert_avail.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.certification));
			course_info.appendChild(cert_avail);
			
			// description
			org.w3c.dom.Element course_description = newCreatedDocument.createElement("description");
			course_description.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.description));
			course_info.appendChild(course_description);
			
			// url
			org.w3c.dom.Element course_url = newCreatedDocument.createElement("url");
			course_url.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.url));
			course_info.appendChild(course_url);

			

			
								
		}

		if(index == urlSize -1){
			System.out.println("Last page");
			driver.close();
			OutputStream output = new FileOutputStream("instructor_url.txt");
			
			for(String instructor : instructorList){
				System.out.println(instructor);
				try{
					output.write((instructor+'\n').getBytes());
				}catch(Exception e){
					
				}
								
			}
			output.close();
			
			
		}
		

	
	
	}
	
	public void crawlData(final String url, final Integer index) throws Exception{
		
		
		
		// open website
		driver.navigate().to(url);
				
		// sleep 10 seconds to wait until onload
		try{
			Thread.sleep(1000*6);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		checkPopup(driver);
		
		// newCreatedDocument is destination of XML.
		NodeList nodelist=newCreatedDocument.getElementsByTagName("ROOT");
		Node root=nodelist.item(0);
				
		
		Data data = new Data();
						
		data.url = url;
		
		WebElement info_element;
		
		try{
			info_element = driver.findElement(By.className("course-info"));
		}catch(Exception e){
			// error occurred in web-site
			return;
			
		}
		
		
		data.title = info_element.findElement(By.className("course-title")).getText();
		data.description = info_element.findElement(By.className("course-desc")).getText().replaceAll("&#", "");
		
		
		System.out.println(data.title);
//		System.out.println(data.description);
		
	
		WebElement detailed_info_element = driver.findElement(By.cssSelector("div.course-data"));
		
		ArrayList <WebElement> detailed_info = new ArrayList <WebElement> (detailed_info_element
				.findElements(By.cssSelector("div.course-data-row")));
		
		for(int i=1; i<detailed_info.size(); i++){
			WebElement element = detailed_info.get(i);
			
			String type = detailed_info.get(i).getAttribute("class");
			
			if(type.equals("course-data-row course-pace")){
				// course pace
				System.out.println("course-pace" +element.getText());
				data.pace = element.findElement(By.tagName("a")).getText();
				
			}else if(type.equals("course-data-row course-subject")){
				// subject
				System.out.println("course-subject" +element.getText());
				
				data.subject = element.findElement(By.tagName("a")).getText();
				
			}else if(type.equals("course-data-row course-institution")){
				// school or institution
				System.out.println("course-institution" +element.getText());
				data.school = element.findElement(By.tagName("a")).getText();
				
			}else if(type.equals("course-data-row course-provider")){
				// provider
				System.out.println("course-provider" +element.getText());
				data.provider = element.findElement(By.tagName("a")).getText();
				
			}else if(type.equals("course-data-row course-language")){
				// language
				System.out.println("language" +element.getText());
				data.language = element.findElement(By.tagName("a")).getText();
				
								
			}else if(type.equals("course-data-row course-certificates")){
				// certification available
				System.out.println("certificate avail" +element.getText());
				data.certification = element.findElement(By.className("course-data-value")).getText();
								
			}else if(type.equals("course-data-row course-hours")){
				// courses hour
				System.out.println("course-hours" + element.getText());
				data.course_hour = element.findElement(By.className("course-data-value")).getText();
				
			}else if(type.equals("course-data-row course-sessions")){
				// courses sessions which include when the course were opened
				data.sessionList = new ArrayList<String>();
				ArrayList<WebElement> templist = new ArrayList<WebElement>( element.findElements(By.tagName("option")) );
				
				for(WebElement temp: templist){
					data.sessionList.add(temp.getAttribute("content"));
					
				}
				// add each session to list
				
				 
				if( element.findElements(By.cssSelector("span.course-length")).size() > 0){
					data.course_length = element.findElement(By.cssSelector("span.course-length")).getText();
				}else{
					data.course_length = "none";
				}
				
//				System.out.println(element.getText());
				
			}else{
				continue;
			}
			
			
		}


		// making dom elements
		
		org.w3c.dom.Element course_info = newCreatedDocument.createElement("CourseInfo");
						
		root.appendChild(course_info);
		{
			
									
			// title
			org.w3c.dom.Element course_title = newCreatedDocument.createElement("title");
			course_title.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.title));
			course_info.appendChild(course_title);
			
			// provider
			org.w3c.dom.Element provider = newCreatedDocument.createElement("provider");
			provider.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.provider));
			course_info.appendChild(provider);

						
			// subject
			org.w3c.dom.Element course_subject = newCreatedDocument.createElement("subject");
			course_subject.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.subject));
			course_info.appendChild(course_subject);
						
			// school
			org.w3c.dom.Element course_school = newCreatedDocument.createElement("school");
			course_school.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.school));
			course_info.appendChild(course_school);
			
			// language
			org.w3c.dom.Element course_language = newCreatedDocument.createElement("language");
			course_language.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.language));
			course_info.appendChild(course_language);
			

			// course hour
			org.w3c.dom.Element course_hour = newCreatedDocument.createElement("course_hours");
			course_hour.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.course_hour));
			course_info.appendChild(course_hour);
			
			// course sessions
			org.w3c.dom.Element course_sessions = newCreatedDocument.createElement("course_sessions");
			for(int i=0; i<data.sessionList.size(); i++){
				org.w3c.dom.Element session = newCreatedDocument.createElement("session");
				session.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.sessionList.get(i)));
				course_sessions.appendChild(session);
			}
			course_info.appendChild(course_sessions);
			
			// course length
			org.w3c.dom.Element course_length = newCreatedDocument.createElement("course_length");
			course_length.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.course_length));
			course_info.appendChild(course_length);
			
			
			// pace
			org.w3c.dom.Element status = newCreatedDocument.createElement("pace");
			status.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.pace));
			course_info.appendChild(status);
			
			// certification available
			org.w3c.dom.Element cert_avail = newCreatedDocument.createElement("certification_available");
			cert_avail.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.certification));
			course_info.appendChild(cert_avail);
			
			// description
			org.w3c.dom.Element course_description = newCreatedDocument.createElement("description");
			course_description.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.description));
			course_info.appendChild(course_description);
			
			// url
			org.w3c.dom.Element course_url = newCreatedDocument.createElement("url");
			course_url.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.url));
			course_info.appendChild(course_url);

			

			
								
		}

		if(index == urlSize -1){
			System.out.println("Last page");
			driver.close();
			OutputStream output = new FileOutputStream("instructor_url.txt");
			
			for(String instructor : instructorList){
				System.out.println(instructor);
				try{
					output.write((instructor+'\n').getBytes());
				}catch(Exception e){
					
				}
								
			}
			output.close();
			
			
		}
	
	}
	
	

	
}
