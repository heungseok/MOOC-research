import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
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
	ArrayList<Integer> pagelist = new ArrayList<Integer>();
	
	
	Data courseData = new Data();
//	final WebDriver driver = new FirefoxDriver();
	int urlSize = 0;
	
	WebDriver driver;

	public void initDriver() throws InterruptedException{
		
		System.setProperty(
				"webdriver.chrome.driver",
				"D:/chromedriver_win32/chromedriver.exe"
		);
		
		driver = new ChromeDriver();
		driver.get("http://www.google.com");
		driver.manage().window().maximize();
		
		
		driver.navigate().to("https://www.coursera.org");
		Actions actionObject = new Actions(driver);
		Thread.sleep(30000);
		
		// currently the coursera platform block the login-access through selenuim web-driver 
		// so, sleep the program and I manually login to the platform
		
		// *********************** PLEASE LOGIN MANUANLLY!!! *************************
	
		
		/*System.out.println(driver.findElement(By.cssSelector("ul.c-navbar-list.bt3-nav.bt3-navbar-nav.bt3-navbar-right")).getLocation());
		
		WebElement login = driver.findElement(By.cssSelector("ul.c-navbar-list.bt3-nav.bt3-navbar-nav.bt3-navbar-right"));
		((JavascriptExecutor) driver).executeScript("window.scrollTo(" + login.getLocation().x + "," + (login.getLocation().y + 20) + ")");
		System.out.println(login.getLocation());
		actionObject.click(login).perform();
		Thread.sleep(2000);
		
		WebElement id = driver.findElement(By.cssSelector("input#user-modal-email.placeholder"));
		
		Thread.sleep(500);
			
						
		// move to the location of the element to input	
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0," + (id.getLocation().y) + ")");
		actionObject.sendKeys("totoro1865@naver.com").perform();
				
//		id.sendKeys("totoro1865@naver.com");
		Thread.sleep(1000);
		actionObject.sendKeys(Keys.ENTER).perform();
		Thread.sleep(3000);
//		((JavascriptExecutor) driver).executeScript("window.scrollTo(0," + (pw.getLocation().y) + ")");
		actionObject.sendKeys("heungseok2").perform();
		Thread.sleep(1000);
//		actionObject.sendKeys(Keys.ENTER).perform();
//		pw.sendKeys("heungseok2");
//		pw.sendKeys(Keys.ENTER);
		Thread.sleep(3000);
		
	*/	
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
	public void crawlData(final String url, int index) throws IOException, InterruptedException{
			
		// open website
		driver.navigate().to(url);
				
		// sleep 5 seconds to wait until onload
		
		try{
			Thread.sleep(1000*5);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		// newCreatedDocument is destination of XML.
		NodeList nodelist=newCreatedDocument.getElementsByTagName("ROOT");
		Node root=nodelist.item(0);
				
		
		Data data = new Data();
		
		data.id = Integer.toString(index);
		
		// assign course url 
		data.url = url;
		
		// title
		List<WebElement> tempList = new ArrayList<WebElement>();
		tempList = driver.findElements(By.cssSelector("div.s12n-headline"));
		// if the size of the list parsed from title web elements is zero, then the course is not supported anymore in the platform(Coursera) 
		if(tempList.size() == 0) 
			return;
		else
			data.title = tempList.get(0).getText().replaceAll("&amp;", "");
		
		System.out.println(data.title);
				
		// sub-title
		tempList = driver.findElements(By.cssSelector("div.s12n-subheader"));
		if(tempList.size() == 0) 
			data.subtitle = null;
		else
			data.subtitle = tempList.get(0).getText().replaceAll("&amp;", "");
		
		System.out.println(data.subtitle);
		
		// specialization description
		WebElement temp_element = driver.findElement(By.cssSelector("div.rc-AboutS12n.s12n-section div:nth-of-type(2)"));
//		data.description = driver.findElement(By.cssSelector("div.rc-CML.styled")).getText();
		data.description = temp_element.getText();
		System.out.println(data.description);
		
		// included courses
		data.included_courses = new ArrayList<String>();
		tempList = driver.findElement(By.cssSelector("div.pricing-container")).findElements(By.cssSelector("span.course-title"));
		for(WebElement element: tempList){
			data.included_courses.add(element.getText());
			System.out.println(element.getText());
		}
		data.included_courses_price = new ArrayList<String>();
		tempList = driver.findElement(By.cssSelector("div.pricing-container")).findElements(By.cssSelector("div.course-price"));
		for(WebElement element: tempList){
			data.included_courses_price.add(element.getText());
			System.out.println(element.getText());
		}
		
		
		// subject
//		WebElement tempElement = driver.findElement(By.cssSelector("div.rc-BannerBreadcrumbs.caption-text"));
//		tempList = tempElement.findElements(By.cssSelector("span.item"));
//		data.subject = tempList.get(tempList.size()-1).getText();
//		
		// institution
//		data.school = driver.findElement(By.cssSelector("div.partner-marketing-blurb")).getText();
//		System.out.println(data.school);
		
		// platform
		data.provider = "Coursera";
		
		// level
		data.level = driver.findElement(By.cssSelector("div.s12n-level-description")).getText().replaceAll("&nbsp;", "");
		System.out.println(data.level);
		
		// instructors(Name, job, img)
		/*
		tempList = driver.findElements(By.cssSelector("div.profile-instructor-container.horizontal-box"));
		data.instructors = new ArrayList<String>();
		data.instructors_bio = new ArrayList<String>();
		data.instructors_img = new ArrayList<String>();
		
		
		List<WebElement> temp = new ArrayList<WebElement>();
		
		for(int i=0; i<tempList.size(); i++){
			String name = tempList.get(i).findElement(By.cssSelector("span.body-1-text")).getText();
//			name = name.replaceAll("Taught by:", "");
			String bio;
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
			
		}
		*/
		
		
		/*
		
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
		
		*/
		
		Thread.sleep(1000*2);
	
		
		// making dom elements
		
		org.w3c.dom.Element course_info = newCreatedDocument.createElement("CourseInfo");
		root.appendChild(course_info);
		{
			
			// index
			org.w3c.dom.Element course_id = newCreatedDocument
					.createElement("course_id");
			course_id.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.id));
			course_info.appendChild(course_id);
						
			// specialization title
			org.w3c.dom.Element course_title = newCreatedDocument.createElement("title");
			course_title.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.title));
			course_info.appendChild(course_title);
			
			// specialization sub-title
			org.w3c.dom.Element course_sub_title = newCreatedDocument.createElement("sub-title");
			course_sub_title.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.subtitle));
			course_info.appendChild(course_sub_title);
			
			// course_size
			org.w3c.dom.Element num_of_courses = newCreatedDocument.createElement("num_of_courses");
			num_of_courses.appendChild(createTextNodeWithoutNull(newCreatedDocument, String.valueOf(data.included_courses.size())));
			course_info.appendChild(num_of_courses);

			// level
			org.w3c.dom.Element course_level = newCreatedDocument.createElement("level");
			course_level.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.level));
			course_info.appendChild(course_level);

//			// school
//			org.w3c.dom.Element course_school = newCreatedDocument.createElement("school");
//			course_school.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.school));
//			course_info.appendChild(course_school);
			
//			// language
//			org.w3c.dom.Element course_language = newCreatedDocument.createElement("language");
//			course_language.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.language));
//			course_info.appendChild(course_language);
//			
			
			// url
			org.w3c.dom.Element course_url = newCreatedDocument.createElement("url");
			course_url.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.url));
			course_info.appendChild(course_url);
			
			// courses
			org.w3c.dom.Element courses = newCreatedDocument.createElement("courses");
			for(int i=0; i<data.included_courses.size(); i++){
				org.w3c.dom.Element course = newCreatedDocument.createElement("course");
				
				org.w3c.dom.Element name = newCreatedDocument.createElement("title");
				name.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.included_courses.get(i)));
				org.w3c.dom.Element price = newCreatedDocument.createElement("price");
				price.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.included_courses_price.get(i)));
				course.appendChild(name);
				course.appendChild(price);
								
				courses.appendChild(course);
			}
			course_info.appendChild(courses);
			
			
			/*
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
				instructor.appendChild(name);
				instructor.appendChild(bio);
				instructor.appendChild(img);
				
				
				instructors.appendChild(instructor);
			}
			course_info.appendChild(instructors);
			*/
			
			/*
			// basic info
			org.w3c.dom.Element basic_info = newCreatedDocument.createElement("basic_info");
			basic_info.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.basic_info));
			course_info.appendChild(basic_info);
			*/
			
			// specialization-description
			org.w3c.dom.Element course_description = newCreatedDocument.createElement("description");
			course_description.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.description));
			course_info.appendChild(course_description);
	
								
		}

		if(index == urlSize -1){
			System.out.println("Last page");
			driver.close();

//			OutputStream output = new FileOutputStream("instructor_url.txt");
//			
//			for(String instructor : instructorList){
//				System.out.println(instructor);
//				try{
//					output.write((instructor+'\n').getBytes());
//				}catch(Exception e){
//					
//				}
//								
//			}
//			output.close();
			
			
		}
	
	}
	
	

	
}
