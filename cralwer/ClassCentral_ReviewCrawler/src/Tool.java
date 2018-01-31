import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
	DateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
	
	WebDriver driver;
	int urlSize = 0;
	
	
	public void initDriver(){
		
		System.setProperty(
				"webdriver.chrome.driver",
				"D:/chromedriver_win32/chromedriver.exe"
		);
		
		driver = new ChromeDriver();
//		driver.get("http://www.google.com");
		
		
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
						
		data.reviewCount = document.select("span.review-count").get(0).text();
			
		data.review_sum = document.select("span.average-rating.course-rating").attr("data-score").toString();
		
		Element detailed_info_element = document.select("div.course-data").get(0);
		
		Elements detailed_info = detailed_info_element.select("div.course-data-row");
		
		
		for(int i=1; i<detailed_info.size(); i++){
			Element element = detailed_info.get(i);
			
			String type = detailed_info.get(i).attr("class");
					
			
			if(type.equals("course-data-row course-institution")){
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
				
				
			}else{
				continue;
			}
		}
		
		
		
			
		
		
		if(Integer.valueOf(data.reviewCount) == 0){
			// review count가 0일 경우 함수 빠져나감.
			return;
			
			
		}else{
			// review count가 한개 이상일 경우.
			
			Elements reviews;
			
			if(Integer.valueOf(data.reviewCount) > 150){
				driver.get(url);
				Thread.sleep(10*1000);
				checkPopup(driver);
				document = Jsoup.parse(driver.getPageSource());
						
			}
			
			reviews = document.select("div.single-review");
						
						
			// review element가 없을 경우. 함수 빠져나감  
			if(reviews.size() < 1){
				return;
			}
						
			for(int i=0; i<reviews.size(); i++){
				// review date
				data.review_date = reviews.get(i).select("a.review-date").text();
				// parsing review data
				data.fixed_review_date = parseReviewDate(data.review_date);		
				
					
				
				try{
					// reviewer id and url
					data.reviewer_url = reviews.get(i).select("div.crop-circle.crop-circle--bordered").get(0).getElementsByTag("a").get(0).attr("href").toString();										

				}catch (Exception e){
					// assign id value as anonymous
					data.reviewer_url = "null";
									
				}
				
				
				if(reviews.get(i).select("span.author").size() > 0){
					data.reviewer_id = reviews.get(i).select("span.author").get(0).text();
					
				}else{
					data.reviewer_id = "null";
				}
				// review value
				try{
					data.reviewer_rating_value = reviews.get(i).select("div.small-star-rating").get(0).getElementsByTag("meta").attr("content").toString();
				}catch (Exception e){
					data.reviewer_rating_value = "null";
				}
				
				// review feedback
				if(reviews.get(i).select("div.review-feedback").size() > 0){
					String temp_feed = reviews.get(i).select("div.review-feedback").get(0).text();
					temp_feed = temp_feed.replaceAll("out of ", "");
					String [] temp_arr = temp_feed.split(" ");
					data.positive_review_score = temp_arr[0];
					data.negative_review_score = String.valueOf(Integer.valueOf(temp_arr[1]) - Integer.valueOf(temp_arr[0]));
					
				}else{
					data.positive_review_score = String.valueOf(0);
					data.negative_review_score = String.valueOf(0);
					
				}
				System.out.println(data.positive_review_score);
				System.out.println(data.negative_review_score);
				
				
				
				
				// review content
				if(reviews.get(i).select("div.review-full").size() > 0 ){
					data.review_contents = reviews.get(i).select("div.review-full").get(0).text();
//					data.review_contents = data.review_contents.replaceAll("#&", "");
					data.review_contents = data.review_contents.replaceAll("&#", "");
				}else{
					data.review_contents = "none";
				}
				
//				System.out.println(data.review_contents);
				
				
				
				// making dom elements
				org.w3c.dom.Element course_info = newCreatedDocument.createElement("CourseReview");
								
				root.appendChild(course_info);
				{
					
											
					// title
					org.w3c.dom.Element course_title = newCreatedDocument.createElement("title");
					course_title.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.title));
					course_info.appendChild(course_title);
					
					// platform
					org.w3c.dom.Element course_platform = newCreatedDocument.createElement("platform");
					course_platform.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.provider));
					course_info.appendChild(course_platform);
					
					// school
					org.w3c.dom.Element course_school = newCreatedDocument.createElement("institution");
					course_school.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.school));
					course_info.appendChild(course_school);
					
					
					// review count
					org.w3c.dom.Element review_count = newCreatedDocument.createElement("review_count");
					review_count.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.reviewCount));
					course_info.appendChild(review_count);
										
					// review value Summation
					org.w3c.dom.Element review_sum = newCreatedDocument.createElement("review_sum");
					review_sum.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.review_sum));
					course_info.appendChild(review_sum);
					
					// user_id
					org.w3c.dom.Element user_id = newCreatedDocument.createElement("reviewer_id");
					user_id.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.reviewer_id));
					course_info.appendChild(user_id);

								
					// reviewer value
					org.w3c.dom.Element reviewer_rating_value = newCreatedDocument.createElement("single_rating_value");
					reviewer_rating_value.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.reviewer_rating_value));
					course_info.appendChild(reviewer_rating_value);
					
					// review date
					org.w3c.dom.Element review_date = newCreatedDocument.createElement("review_date");
					review_date.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.review_date));
					course_info.appendChild(review_date);
					
					// fixed_review date
					org.w3c.dom.Element fixed_review_date = newCreatedDocument.createElement("fixed_review_date");
					fixed_review_date.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.fixed_review_date));
					course_info.appendChild(fixed_review_date);
					
					// reviewer url 
					org.w3c.dom.Element reviewer_url = newCreatedDocument.createElement("reviewer_url");
					reviewer_url.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.reviewer_url));
					course_info.appendChild(reviewer_url);
					
					// review helpful
					org.w3c.dom.Element review_feed_pos = newCreatedDocument.createElement("helpful_rate");
					review_feed_pos.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.positive_review_score));
					course_info.appendChild(review_feed_pos);
					
					//
					org.w3c.dom.Element review_feed_neg = newCreatedDocument.createElement("helpful_rate_neg");
					review_feed_neg.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.negative_review_score));
					course_info.appendChild(review_feed_neg);
					
					// status(completed, etc)
					
					
					// review content
					org.w3c.dom.Element review_contents = newCreatedDocument.createElement("review_contents");
					review_contents.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.review_contents));
					course_info.appendChild(review_contents);
					
					
					// url
					// this will be an identical value
					org.w3c.dom.Element course_url = newCreatedDocument.createElement("url");
					course_url.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.url));
					course_info.appendChild(course_url);
				
										
				}
				
				
				
				
				
			}
			
			
		}
				
		

	
	
	}
	
	public String parseReviewDate(String date_row){
		
		System.out.println(date_row);
		
		String review_date = "";
		String prefix = "";
		Date cal_date;
		
		Date current_date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(current_date);
		
		try{
			prefix = date_row.split(" ")[0];
		}catch(Exception e){
			prefix = date_row;
		}
		
		
		
		if(date_row.contains("year")){
									
			if(prefix.contains("a")){
				cal.add(Calendar.YEAR, -1);				 
			}else{
				cal.add(Calendar.YEAR, -Integer.valueOf(prefix));
			}
						
			
		}else if(date_row.contains("month")){
			
			if(prefix.contains("a")){
				cal.add(Calendar.MONTH, -1);				 
			}else{
				cal.add(Calendar.MONTH, -Integer.valueOf(prefix));
			}
			
		}else if(date_row.contains("week")){
			
			if(prefix.contains("a")){
				cal.add(Calendar.DATE, -7);				 
			}else{
				cal.add(Calendar.DATE, -7*Integer.valueOf(prefix));
			}
			
		}else if(date_row.contains("day")){
			
			if(prefix.contains("a") || prefix.equals("yesterday")){
				cal.add(Calendar.DATE, -1);				 
			}else{
				cal.add(Calendar.DATE, -Integer.valueOf(prefix));
			}
			
			
		}else if(date_row.contains("hour")){
			
			if(prefix.contains("a")){
				cal.add(Calendar.HOUR, -1);				 
			}else{
				cal.add(Calendar.HOUR, -Integer.valueOf(prefix));
			}
			
		}else if(date_row.contains("minute")){
			
			if(prefix.contains("a")){
				cal.add(Calendar.MINUTE, -1);				 
			}else{
				cal.add(Calendar.MINUTE, -Integer.valueOf(prefix));
			}
			
		}else if(date_row.contains("just")){
						
			cal.add(Calendar.DATE, 0);
		}
		
		cal_date = cal.getTime();
		review_date = date_format.format(cal_date);
//		System.out.println(review_date);
		
		
		
		prefix = null;
		cal_date = null;
		current_date = null;
		cal = null;
				
		return review_date;
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
			// error occurred in web-site, then break this function
			return;
			
		}
		
		
		data.title = info_element.findElement(By.className("course-title")).getText();
//		data.description = info_element.findElement(By.className("course-desc")).getText();
		
//		System.out.println(data.title);
//		System.out.println(data.description);
		
		data.reviewCount = driver.findElement(By.cssSelector("span.review-count")).getText();
		data.review_sum = driver.findElement(By.cssSelector("span.average-rating.course-rating")).getAttribute("data-score").toString();
		
		if(Integer.valueOf(data.reviewCount) == 0){
			// review count가 0일 경우 함수 빠져나감.
			return;
			
			
		}else{
			// review count가 한개 이상일 경우.
			
			ArrayList <WebElement> reviews;
			

			reviews = new ArrayList <WebElement> (driver.findElements(By.cssSelector("div.single-review")));
			
			// review element가 없을 경우. 함수 빠져나감  
			if(reviews.size() < 1){
				return;
			}
			
			
			for(int i=0; i<reviews.size(); i++){
				// review date
				data.review_date = reviews.get(i).findElement(By.cssSelector("a.review-date")).getText();
					
				
				try{
					// reviewer id and url
					data.reviewer_url = reviews.get(i).findElement(By.cssSelector("div.crop-circle.crop-circle--bordered"))
					.findElement(By.tagName("a")).getAttribute("href").toString();
												

				}catch (Exception e){
					// assign id value as anonymous
					data.reviewer_url = "null";
									
				}
				
				data.reviewer_id = reviews.get(i).findElement(By.cssSelector("div.review-title.title-with-image"))
						.findElement(By.cssSelector("span.author")).getText();
				
				// review value
				data.reviewer_rating_value = reviews.get(i).findElement(By.cssSelector("div.small-star-rating"))
				.findElement(By.tagName("meta")).getAttribute("content").toString();
				
				// review content
				if(reviews.get(i).findElements(By.cssSelector("div.review-full")).size() > 0 ){
					data.review_contents = reviews.get(i).findElement(By.cssSelector("div.review-full")).getText().toString();
					data.review_contents = data.review_contents.replaceAll("#&", "");
				}else{
					data.review_contents = "none";
				}
				
//				System.out.println(data.review_contents);
				
				
				
				// making dom elements
				org.w3c.dom.Element course_info = newCreatedDocument.createElement("CourseReview");
								
				root.appendChild(course_info);
				{
					
											
					// title
					org.w3c.dom.Element course_title = newCreatedDocument.createElement("title");
					course_title.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.title));
					course_info.appendChild(course_title);
					
					// review count
					org.w3c.dom.Element review_count = newCreatedDocument.createElement("review_count");
					review_count.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.reviewCount));
					course_info.appendChild(review_count);
										
					// review value Summation
					org.w3c.dom.Element review_sum = newCreatedDocument.createElement("review_sum");
					review_sum.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.review_sum));
					course_info.appendChild(review_sum);
					
					// user_id
					org.w3c.dom.Element user_id = newCreatedDocument.createElement("reviewer_id");
					user_id.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.reviewer_id));
					course_info.appendChild(user_id);

								
					// reviewer value
					org.w3c.dom.Element reviewer_rating_value = newCreatedDocument.createElement("single_rating_value");
					reviewer_rating_value.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.reviewer_rating_value));
					course_info.appendChild(reviewer_rating_value);
					
					// review date
					org.w3c.dom.Element review_date = newCreatedDocument.createElement("review_date");
					review_date.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.review_date));
					course_info.appendChild(review_date);
					
					// reviewer url 
					org.w3c.dom.Element reviewer_url = newCreatedDocument.createElement("reviewer_url");
					reviewer_url.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.reviewer_url));
					course_info.appendChild(reviewer_url);
					
					
					// status(completed, etc)
					
					
					// review content
					org.w3c.dom.Element review_contents = newCreatedDocument.createElement("review_contents");
					review_contents.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.review_contents));
					course_info.appendChild(review_contents);
					
					
					// url
					// this will be an identical value
					org.w3c.dom.Element course_url = newCreatedDocument.createElement("url");
					course_url.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.url));
					course_info.appendChild(course_url);
				
										
				}
				
				
				
				
				
			}
			
			
		}
				
		

	
	
	}
	
	

	
}
