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
	ArrayList<String> errorURLs = new ArrayList<String>();
	
	Data courseData = new Data();
	String base_url = "https://www.class-central.com";
	
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

	
	public ArrayList<String> getURL() throws IOException
	{
		
		
		final ArrayList<String> urlList=new ArrayList<String>();
		
		
		FileInputStream inputFile = new FileInputStream("./users_url.txt");
		
		// Construct BufferReader from inputStreamReader
		BufferedReader fileReader = new BufferedReader(new InputStreamReader(inputFile));
			
		
		String line = null;
		
		// read url line by line 
		while ( (line = fileReader.readLine()) != null){
			urlList.add(line);
		}
		
		fileReader.close();
			
		
		urlSize = urlList.size();
		System.out.println("size of url - " + urlSize);
		
		return urlList; 
		
		
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
	
	
	public void checkPopup(WebDriver driver) throws Exception{
		if(driver.findElements(By.className("modal-open")).size() > 0){
			driver.findElements(By.cssSelector("a.modal-close-button")).get(0).click();
			Thread.sleep(2000);
			
		}
		
	}
	
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
		
		// open website
		String site_url = base_url.concat(url).concat("/reviews");
				
		
		Connection connection;
		
		
		connection = Jsoup.connect(site_url).timeout(10*1000);
		Document document = null;

		
								
		try {
			document = (Document) connection.get();
		} catch (Exception e) {
			System.out.println("connection error at url: " + url);
			e.printStackTrace();
			errorURLs.add(url);
			return;
		}
		
		Data data = new Data();
		
		data.url = url;
		
		
		Element info_element;
		int review_size;
		int elements_size;
		try{
			info_element = document.select("div.tabbed-content").get(0);
			
			String temp_str = info_element.select("li.active").get(0).text();
			temp_str = temp_str.split(" ")[1];
			temp_str = temp_str.replaceAll("[()]", "");
			data.displayed_reviewed_size = temp_str;
			review_size = Integer.valueOf(temp_str);
			System.out.println("review size: " + review_size);					
			elements_size = info_element.getElementById("reviews").select("div.line-with-points.color-scheme--blue").size();
			System.out.println("elements size: " + elements_size);
			
			if(review_size != elements_size){
				driver.get(site_url);
				Thread.sleep(10*1000);
				checkPopup(driver);
				document = Jsoup.parse(driver.getPageSource());
				info_element = document.select("div.tabbed-content").get(0);
			}
			
		}catch(Exception e){
			driver.get(site_url);
			Thread.sleep(10*1000);
			checkPopup(driver);
			document = Jsoup.parse(driver.getPageSource());
			info_element = document.select("div.tabbed-content").get(0);
						
//			driver.quit();
						
			errorURLs.add(url);
		}
		
		
		
		// newCreatedDocument is destination of XML.
		NodeList nodelist=newCreatedDocument.getElementsByTagName("ROOT");
		Node root=nodelist.item(0);
				
		
		
				
		
		////////////// User Info parsing //////////////
		Element user_info = document.select("div.col-md-3.profile-info").get(0);
		
		// name
		data.name = user_info.select("div.info-row.info-name").get(0).getElementsByClass("info-text").get(0).text();
		
		// location
		if(user_info.select("div.info-row.info-location").size() > 0 ){
			data.location = user_info.select("div.info-row.info-location").select("span.info-text").get(0).text();
		}else{
			data.location = "null";
		}
		
		// profession
		if(user_info.select("div.info-row.info-profession").size() > 0 ){
			data.professional = user_info.select("div.info-row.info-profession").select("span.info-text").get(0).text();			
		}else{
			data.professional = "null";
		}
		
		// education
		if(user_info.select("div.info-row.info-education").size() > 0 ){
			data.education = user_info.select("div.info-row.info-education").select("span.info-text").get(0).text();			
		}else{
			data.education = "null";
		}
		
	
		
		///////////// reviewed course info /////////////
		Elements reviewed_courses = info_element.getElementById("reviews").select("div.line-with-points.color-scheme--blue");
		data.courseList = new ArrayList<Course>();
		
		data.reviewed_size = String.valueOf(reviewed_courses.size());
		for (int i=0; i<reviewed_courses.size();i++){
			Course temp_course = new Course();
/*			try{
				temp_course.url = reviewed_courses.get(i).select("span.item-title").get(0).select("a.colored").get(0).attr("href");
			}catch(Exception e){
				
				errorURLs.add(url.concat(" - Errors in for loop"));
				return;
			}
*/			
			temp_course.url = reviewed_courses.get(i).select("span.item-title").get(0).select("a.colored").get(0).attr("href");
			temp_course.title = reviewed_courses.get(i).select("span.item-title").get(0).text();
			
			try{
				temp_course.provider = reviewed_courses.get(i).select("a.initiativelinks.colored").get(0).text();
			}catch(Exception e){
				temp_course.provider = "null";
			}
			
						
			try{
				temp_course.school = reviewed_courses.get(i).select("a.uni-name").get(0).text(); 
			}catch(Exception e){
				temp_course.school = "null";
			}
			

			data.courseList.add(temp_course);
			
			temp_course = null;

		}
		
		
		
		
		
		// write dom
		org.w3c.dom.Element UserInfo = newCreatedDocument.createElement("UserInfo");
		
		root.appendChild(UserInfo);
		{
			
			// user name
			org.w3c.dom.Element user_name = newCreatedDocument.createElement("name");
			user_name.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.name));
			UserInfo.appendChild(user_name);
			
			// user url
			org.w3c.dom.Element user_url = newCreatedDocument.createElement("user_url");
			user_url.appendChild(createTextNodeWithoutNull(newCreatedDocument, url));
			UserInfo.appendChild(user_url);
			
			// user education
			org.w3c.dom.Element user_edu = newCreatedDocument.createElement("education");
			user_edu.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.education));
			UserInfo.appendChild(user_edu);
			
			// user profession
			org.w3c.dom.Element user_profession = newCreatedDocument.createElement("profession");
			user_profession.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.professional));
			UserInfo.appendChild(user_profession);
			
			// user location
			org.w3c.dom.Element user_location = newCreatedDocument.createElement("location");
			user_location.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.location));
			UserInfo.appendChild(user_location);
						
			// enrolled course list
			org.w3c.dom.Element course_list = newCreatedDocument.createElement("enrolled_courses");
			for(int i=0; i<data.courseList.size(); i++){
				org.w3c.dom.Element course = newCreatedDocument.createElement("course");
								
				org.w3c.dom.Element provider = newCreatedDocument.createElement("provider");
				provider.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.courseList.get(i).provider));
				org.w3c.dom.Element school = newCreatedDocument.createElement("school");
				school.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.courseList.get(i).school));
				org.w3c.dom.Element title = newCreatedDocument.createElement("course_title");
				title.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.courseList.get(i).title));
				org.w3c.dom.Element course_url = newCreatedDocument.createElement("course_url");
				course_url.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.courseList.get(i).url));
				
								
				course.appendChild(provider);
				course.appendChild(school);
				course.appendChild(title);
				course.appendChild(course_url);
				
				
				course_list.appendChild(course);
			}
			UserInfo.appendChild(course_list);
			
		
		
								
		}
	
		if(index == urlSize -1){
			System.out.println("Last page");
			driver.close();
					
			
		}
		
	
	}
	
	

	public void crawlData(final String url, final Integer index) throws IOException{
		
		// open website
		String site_url = base_url.concat(url).concat("/reviews");
		
		driver.navigate().to(site_url);
				
		// sleep 10 seconds to wait until onload
		try{
			Thread.sleep(1000*6);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		// newCreatedDocument is destination of XML.
		NodeList nodelist=newCreatedDocument.getElementsByTagName("ROOT");
		Node root=nodelist.item(0);
				
		
		Data data = new Data();
						
		data.url = url;
		
			
		////////////// User Info parsing //////////////
		WebElement user_info = driver.findElement(By.cssSelector("div.col-md-3.profile-info"));
		
		data.name = user_info.findElement(By.cssSelector("div.info-row.info-name"))
				.findElement(By.cssSelector("span.info-text")).getText();
		
		if(user_info.findElements(By.cssSelector("div.info-row.info-location")).size() > 0 ){
			data.location = user_info.findElement(By.cssSelector("div.info-row.info-location"))
					.findElement(By.cssSelector("span.info-text")).getText();
			
		}else{
			data.location = "null";
		}
		
		
		if(user_info.findElements(By.cssSelector("div.info-row.info-profession")).size() > 0 ){
			data.professional = user_info.findElement(By.cssSelector("div.info-row.info-profession"))
					.findElement(By.cssSelector("span.info-text")).getText();
			
		}else{
			data.professional = "null";
		}
		
		if(user_info.findElements(By.cssSelector("div.info-row.info-education")).size() > 0 ){
			data.education = user_info.findElement(By.cssSelector("div.info-row.info-education"))
					.findElement(By.cssSelector("span.info-text")).getText();
			
		}else{
			data.education = "null";
		}
		
		System.out.println("name- " + data.name);
		System.out.println("education- " + data.education);
		System.out.println("professional- " + data.professional);
		System.out.println("location- " + data.location);
		
		
		
		
		/////////////////////////////////////////////////
		////////////// course data parsing //////////////
			
		
		ArrayList <WebElement> status_list = new ArrayList <WebElement> 
			( driver.findElement(By.id("transcripts")).findElements(By.cssSelector("h2.large-thin-title")));
				
		
		ArrayList <WebElement> courses_list = new ArrayList <WebElement> 
			( driver.findElement(By.id("transcripts")).findElements(By.cssSelector("div.tab-pane-item.left-point")) );
		
		// if there is no course enrolled, break this function.
		if(! (courses_list.size() > 0) ){
			return;
		}
		
		System.out.println("type of enrollements - " + status_list.size());
		System.out.println("the number of courses - " + courses_list.size());
		
		
		data.courseList = new ArrayList<Course>();
				
		int pre_index;
		for(int i=0; i<status_list.size() ; i++){
			String[] temp = status_list.get(i).getText().split(" ");
			System.out.println(temp[temp.length-2]);
			
//			System.out.println(status_list.get(i).getText().split(" "));
			
			
			if(i==0){
				pre_index = 0;
			}else{
				String[] temp2 = status_list.get(i-1).getText().split(" ");
						
				pre_index =+ Integer.valueOf(temp2[temp2.length-2]);
			}
			
			int course_count;
			try{
				// NO COURSES FOUND일 경우
				course_count = Integer.valueOf(temp[temp.length-2]);
			}catch(Exception e){
				return;
			}
			
			System.out.println("course-count: " + course_count);
			System.out.println("pre-index: " + pre_index);
			
			for(int j=pre_index; j< pre_index+course_count; j++){
				Course temp_course = new Course();
//				temp_course.status = status_list.get(i).getText();
//				System.out.println(temp_course.status);
				temp_course.title = courses_list.get(j).findElement(By.cssSelector("span.item-title")).getText();
				System.out.println(temp_course.title);
				temp_course.url = courses_list.get(j).findElement(By.cssSelector("span.item-title"))
						.findElement(By.tagName("a")).getAttribute("href");
				System.out.println(temp_course.url);
				try{
					temp_course.provider = courses_list.get(j).findElement(By.cssSelector("span.item-info"))
							.findElement(By.cssSelector("a.initiativelinks.colored")).getText();
					System.out.println(temp_course.provider);
				}catch(Exception e){
					temp_course.provider = "null";
				}
				
				
				try{
					temp_course.school = courses_list.get(j).findElement(By.cssSelector("span.item-info"))
							.findElement(By.cssSelector("a.uni-name")).getText();
				}catch(Exception e){
					temp_course.school = "null";
				}
				
				System.out.println(temp_course.school);
				
				data.courseList.add(temp_course);
				
			}
			
		}
		
		//////////// interested courses /////////////////
		
		ArrayList <WebElement> interested_course_list = new ArrayList <WebElement> 
		( driver.findElement(By.id("interested")).findElements(By.cssSelector("div.tab-pane-item.left-point")));
		
		for( WebElement element : interested_course_list){
			Course temp_course = new Course();
			temp_course.title = element.findElement(By.cssSelector("span.item-title")).findElement(By.tagName("a")).getText();
			temp_course.url = element.findElement(By.cssSelector("span.item-title")).findElement(By.tagName("a")).getAttribute("href");
			
			try{
				temp_course.provider = element.findElement(By.cssSelector("a.initiativelinks.colored")).getText();
			}catch(Exception e){
				temp_course.provider = "null";
			}
			
			
//			temp_course.status = "Interested";
			
			try{
				temp_course.school = element.findElement(By.cssSelector("a.uni-name")).getText();
			}catch(Exception e){
				temp_course.school = "null";
			}
			
			data.courseList.add(temp_course);
			
			
			
		}
		
	
		// making dom elements
		
		org.w3c.dom.Element UserInfo = newCreatedDocument.createElement("UserInfo");
						
		root.appendChild(UserInfo);
		{
			
			// user name
			org.w3c.dom.Element user_name = newCreatedDocument.createElement("name");
			user_name.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.name));
			UserInfo.appendChild(user_name);
			
			// user url
			org.w3c.dom.Element user_url = newCreatedDocument.createElement("user_url");
			user_url.appendChild(createTextNodeWithoutNull(newCreatedDocument, url));
			UserInfo.appendChild(user_url);
			
			// user education
			org.w3c.dom.Element user_edu = newCreatedDocument.createElement("education");
			user_edu.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.education));
			UserInfo.appendChild(user_edu);
			
			// user profession
			org.w3c.dom.Element user_profession = newCreatedDocument.createElement("profession");
			user_profession.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.professional));
			UserInfo.appendChild(user_profession);
			
			// user location
			org.w3c.dom.Element user_location = newCreatedDocument.createElement("location");
			user_location.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.location));
			UserInfo.appendChild(user_location);
			
			// review size displayed
			org.w3c.dom.Element review_size = newCreatedDocument.createElement("review_size");
			review_size.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.reviewed_size));
			UserInfo.appendChild(review_size);
			
			org.w3c.dom.Element review_size_displayed = newCreatedDocument.createElement("review_size(displayed)");
			review_size_displayed.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.displayed_reviewed_size));
			UserInfo.appendChild(review_size_displayed);
						
			// enrolled course list
			org.w3c.dom.Element course_list = newCreatedDocument.createElement("enrolled_courses");
			for(int i=0; i<data.courseList.size(); i++){
				org.w3c.dom.Element course = newCreatedDocument.createElement("course");
				
//				org.w3c.dom.Element status = newCreatedDocument.createElement("status");
//				status.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.courseList.get(i).status));
				org.w3c.dom.Element provider = newCreatedDocument.createElement("provider");
				provider.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.courseList.get(i).provider));
				org.w3c.dom.Element school = newCreatedDocument.createElement("school");
				school.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.courseList.get(i).school));
				org.w3c.dom.Element title = newCreatedDocument.createElement("title");
				title.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.courseList.get(i).title));
				org.w3c.dom.Element course_url = newCreatedDocument.createElement("url");
				course_url.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.courseList.get(i).url));
				
				
//				course.appendChild(status);
				course.appendChild(provider);
				course.appendChild(school);
				course.appendChild(title);
				course.appendChild(course_url);
				
				
				course_list.appendChild(course);
			}
			UserInfo.appendChild(course_list);
			
			// interested course list
		
								
		}

		
	
	}
	
	

	
}
