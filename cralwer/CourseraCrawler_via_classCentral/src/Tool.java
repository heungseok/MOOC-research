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
	final String base_url = "https://www.coursera.org";
	
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
		Thread.sleep(15000);
		
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
		
		
		
		
		
		// 2. Connect to Coursera.org
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
			
		
		// title
		List<WebElement> tempList = new ArrayList<WebElement>();
		tempList = driver.findElements(By.cssSelector("h1.title.display-3-text"));
		// if the size of the list parsed from title web elements is zero, then the course is not supported anymore in the platform(Coursera) 
		if(tempList.size() == 0){
			errorURLs.add(targetURL);
			return;
		}else{
			data.title = tempList.get(0).getText().replaceAll("&amp;", "");
			System.out.println(data.title);
		}
						
		
		try{
			// subject
			WebElement tempElement = driver.findElement(By.cssSelector("div.rc-BannerBreadcrumbs.caption-text"));
			tempList = tempElement.findElements(By.cssSelector("span.item"));
			data.subject = tempList.get(tempList.size()-1).getText();
			
			// area 
			data.area = tempList.get(tempList.size()-2).getText();
			
		}catch(Exception e){
			data.subject = "null";
			data.area = "null";
			
		}
		
		
		// institution
		data.school = driver.findElement(By.cssSelector("div.headline-1-text.creator-names")).getText().replaceAll("Created by:  ", "");
		
		// platform
		data.provider = "Coursera";
		
		// instructors(Name, job, img, instructor's url)
		tempList = driver.findElements(By.cssSelector("div.rc-InstructorInfo"));
		data.instructors = new ArrayList<String>();
		data.instructors_bio = new ArrayList<String>();
		data.instructors_img = new ArrayList<String>();
		data.instructors_url = new ArrayList<String>();
		
		
		List<WebElement> temp = new ArrayList<WebElement>();
		
		for(int i=0; i<tempList.size(); i++){
			String name = tempList.get(i).findElement(By.cssSelector("span.body-1-text")).getText();
//			name = name.replaceAll("Taught by:", "");
			String bio;
			
			String inst_url;
			try{
				inst_url = tempList.get(i).findElement(By.cssSelector("span.body-1-text")).findElement(By.tagName("a")).getAttribute("href");
			}catch(Exception e){
				inst_url = "null";
			}
			temp = tempList.get(i).findElements(By.cssSelector("div.instructor-bio.caption-text.color-accent-brown"));
			if(temp.size() == 0)
				bio = "null";
			else
				bio = temp.get(0).getText();
			String img = tempList.get(i).
					findElement(By.cssSelector("div.instructor-photo.bt3-col-xs-4.bt3-col-sm-2")).
					findElement(By.tagName("img")).getAttribute("src");
			
			
			
			data.instructors.add(name);
			data.instructors_img.add(img);
			data.instructors_bio.add(bio);
			data.instructors_url.add(inst_url);
			
		}
		
		// course length
//		tempList = driver.findElements(By.cssSelector("div.week"));
//		data.length = Integer.toString(tempList.size());
		
		
		try{
			WebElement BasicInfo = driver.findElement(By.cssSelector("div.rc-BasicInfo"));
			List<WebElement> infoTable = new ArrayList<WebElement>();
			infoTable = BasicInfo.findElements(By.tagName("tr"));
			for(int i=0; i<infoTable.size(); i++){
				tempList = infoTable.get(i).findElements(By.tagName("td"));
				
				String key = tempList.get(0).getText().toLowerCase();
				String value = tempList.get(1).getText();
				if(key.equals("level")){
					data.level = value;
				}else if(key.equals("commitment")){
					data.effort = value;
				}else if(key.equals("language")){
					data.language = value;
				}else if(key.equals("basic info")){
					data.basic_info = value;
				}
				
			}
			
		}catch(Exception e){
			data.level = "null";
			data.effort = "null";
			data.language = "null";
			data.basic_info = "null";
		}
		
		

		// course description
//		data.description = driver.findElement(By.cssSelector("div.about-section-wrapper")).getText();
		data.description = "null";
		
		
		try{
			WebElement price_btn_root = driver.findElement(By.cssSelector("div.rc-CTANavItem"));
			WebElement price_btn = price_btn_root.findElement(By.tagName("button"));
					
			// This will enable this element if element is invisible
			String js = "arguments[0].style.height='auto'; arguments[0].style.visibility='visible';";
			// Execute the Javascript for the element which we find out
			((JavascriptExecutor) driver).executeScript(js, price_btn);
			Thread.sleep(500);
			
			// Click on element
			price_btn.click();
			
			// sleep 1.5 seconds
			Thread.sleep(1500);
			
			
			
			List <WebElement> price_option;
			price_option = driver.findElements(By.cssSelector("h4.headline-1-text"));
			if(price_option.size() > 0){
				data.price_options = new ArrayList<String>();
				for(int i=0; i<price_option.size(); i++){
					data.price_options.add(price_option.get(i).getText());
					
					
					if(price_option.get(i).getText().contains("US$")){
						data.price = price_option.get(i).getText();
						System.out.println(data.price);
					}
				}
				
			}else if(driver.findElements(By.cssSelector("h4.primary-description")).size() > 0){
				price_option = driver.findElements(By.cssSelector("h4.primary-description"));
				
				data.price_options = new ArrayList<String>();
				for(int i=0; i<price_option.size(); i++){
					data.price_options.add(price_option.get(i).getText());
					
					
					if(price_option.get(i).getText().contains("Purchase Course")){
						data.price = price_option.get(i).getText();
						System.out.println(data.price);
					}
				}
							
				
			}else{
				data.price = "null";
				data.price_options = new ArrayList<String>();
			}

			
		}catch(Exception e){
			data.price = "null";
			data.price_options = new ArrayList<String>();
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
			
			// length
//			org.w3c.dom.Element course_length = newCreatedDocument.createElement("course_length");
//			course_length.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.length));
//			course_info.appendChild(course_length);
			
			// effort
			org.w3c.dom.Element course_effort = newCreatedDocument.createElement("effort");
			course_effort.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.effort));
			course_info.appendChild(course_effort);
			
			// course area
			org.w3c.dom.Element course_area = newCreatedDocument.createElement("area");
			course_area.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.area));
			course_info.appendChild(course_area);
								

			// subject
			org.w3c.dom.Element course_subject = newCreatedDocument.createElement("subject");
			course_subject.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.subject));
			course_info.appendChild(course_subject);
			
			// price
			org.w3c.dom.Element course_price = newCreatedDocument.createElement("price");
			course_price.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.price));
			course_info.appendChild(course_price);
			
			
			
			if(data.price_options.size() ==0){
				course_price.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.price));
			}else{
				
				
				try{
					// price options
					org.w3c.dom.Element price_options = newCreatedDocument.createElement("price_options");
					for(int i=0; i<data.price_options.size(); i++){
						org.w3c.dom.Element p_option = newCreatedDocument.createElement("option");
						p_option.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.price_options.get(i)));
						price_options.appendChild(p_option);
						
					}
					course_info.appendChild(price_options);
					
				}catch(Exception e){
					
					// do nothing
				}

				
			}
							

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
			
			// instructors
			org.w3c.dom.Element instructors = newCreatedDocument.createElement("instructors");
			for(int i=0; i<data.instructors.size(); i++){
				org.w3c.dom.Element instructor = newCreatedDocument.createElement("instructor");
				
				org.w3c.dom.Element name = newCreatedDocument.createElement("name");
				name.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.instructors.get(i)));
				org.w3c.dom.Element bio = newCreatedDocument.createElement("bio");
				bio.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.instructors_bio.get(i)));
				org.w3c.dom.Element img = newCreatedDocument.createElement("img");
				img.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.instructors_img.get(i)));
				org.w3c.dom.Element inst_url = newCreatedDocument.createElement("inst_url");
				inst_url.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.instructors_url.get(i)));
				
				instructor.appendChild(name);
				instructor.appendChild(bio);
				instructor.appendChild(img);
				instructor.appendChild(inst_url);
				
				
				instructors.appendChild(instructor);
			}
			course_info.appendChild(instructors);
			
			// basic info
			org.w3c.dom.Element basic_info = newCreatedDocument.createElement("basic_info");
			basic_info.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.basic_info));
			course_info.appendChild(basic_info);
			
			// description
			org.w3c.dom.Element course_description = newCreatedDocument.createElement("description");
			course_description.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.description));
			course_info.appendChild(course_description);
	
								
		}

		if(index == urlSize -1){
			System.out.println("Last page");
			driver.close();

			OutputStream output = new FileOutputStream("instructor_url.txt");
			
			for(String instructor : data.instructors_url){
				System.out.println(instructor);
				try{
					output.write((instructor+'\n').getBytes());
				}catch(Exception e){
					
				}
								
			}
			output.close();
			
			
		}
	
	}

	public ArrayList<String> readCourseURL() throws Exception{
		
		ArrayList<String> URLlist = new ArrayList<String>();
		
//		FileInputStream inputFile = new FileInputStream("./coursera_url_for_recrawling.txt");
		FileInputStream inputFile = new FileInputStream("./classcentral_url_for_recrawling_20170719.txt");
		
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
