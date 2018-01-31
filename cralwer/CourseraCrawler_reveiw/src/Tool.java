import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
		
		// implicit wait
//		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		
		
		driver.navigate().to("https://www.coursera.org");
		
//		Thread.sleep(15000);
		
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
		
		Actions actionObject = new Actions(driver);
				
		
		Data data = new Data();
		
		data.id = Integer.toString(index);
		
		// assign course url 
		data.url = url;
		
		// title
		List<WebElement> tempList = new ArrayList<WebElement>();
		tempList = driver.findElements(By.cssSelector("div.title.display-3-text"));
		// if the size of the list parsed from title web elements is zero, then the course is not supported anymore in the platform(Coursera) 
		if(tempList.size() == 0) 
			return;
		else
			data.title = tempList.get(0).getText().replaceAll("&amp;", "");
				
		
		// subject
		WebElement tempElement = driver.findElement(By.cssSelector("div.rc-BannerBreadcrumbs.caption-text"));
		tempList = tempElement.findElements(By.cssSelector("span.item"));
		data.subject = tempList.get(tempList.size()-1).getText();
		
		
		// review crawling
		List <WebElement> review_flag = driver.findElements(By.className("rc-Ratings"));
		String total_review_count;
		
		data.reviewContent = new ArrayList<String>();
		data.reviewDate = new ArrayList<String>();
		data.reviewValue = new ArrayList<Integer>();
		
		if(review_flag.size() > 0){
			// move to the location of the element to input
			List<WebElement> review_btn = review_flag.get(0).findElements(By.className("cdp-view-all-button"));
			if(review_btn.size() > 0){
				((JavascriptExecutor) driver).executeScript("window.scrollTo(" + 
						(review_btn.get(0).getLocation().x) +","+ (review_btn.get(0).getLocation().y) + ")");
				Thread.sleep(500);
				actionObject.click(review_btn.get(0)).perform();
				
				Thread.sleep(10000);
							
				
				total_review_count = driver.findElement(By.cssSelector("div.c-value")).getText().replace(",", "");
				System.out.println("total review count: " + total_review_count);
				data.total_reviewCount_visible = Integer.valueOf(total_review_count);
							
				List<WebElement> pre_review_list = driver.findElements(By.className("rc-CourseReview"));
				int pre_review_size = pre_review_list.size();
				pre_review_list.get(0).click();
				actionObject.sendKeys(Keys.PAGE_DOWN).perform();
				Thread.sleep(200);
				actionObject.sendKeys(Keys.PAGE_DOWN).perform();
				Thread.sleep(2000);
				
				List<WebElement> cur_review_list = new ArrayList<WebElement>();
				int cur_review_size;
				
				while(true){
					
					cur_review_list = driver.findElements(By.className("rc-CourseReview"));
					cur_review_size = cur_review_list.size();
					System.out.println("The cur review size: " + cur_review_size);
					
					if(pre_review_size != cur_review_size){
						actionObject.sendKeys(Keys.PAGE_DOWN).perform();
						Thread.sleep(200);
						actionObject.sendKeys(Keys.PAGE_DOWN).perform();
						Thread.sleep(200);
						actionObject.sendKeys(Keys.PAGE_DOWN).perform();
						Thread.sleep(200);
						actionObject.sendKeys(Keys.PAGE_DOWN).perform();
						Thread.sleep(200);
						actionObject.sendKeys(Keys.PAGE_DOWN).perform();
						Thread.sleep(200);
						
						pre_review_size = cur_review_size;
						System.out.println("The pre review size: " + pre_review_size);
						
						
					}else{
						break;
					}
										
					Thread.sleep(3000);
				}
				
//				List <WebElement> reviewList = driver.findElements(By.className("rc-CourseReview"));
				for(WebElement review : cur_review_list){
					String content = review.findElement(By.cssSelector("div.rc-CML.styled")).getText();
					int ratings = review.findElements(By.cssSelector("i.c-course-rating-icon.cif-star")).size();
					String date = review.findElement(By.cssSelector("div.c-course-review-header")).getText();
//					System.out.println("date: " + date + "\nratings: "+ ratings + "\n" + content);
					data.reviewContent.add(content);
					data.reviewDate.add(date);
					data.reviewValue.add(ratings);
					
				}
				data.total_reviewCount_calculated = cur_review_list.size();
				for(int reviewValue : data.reviewValue){
					data.total_reviewValue += reviewValue;
				}
				if(data.total_reviewValue !=0)
					data.total_reviewValue = data.total_reviewValue / data.reviewValue.size();
				else
					data.total_reviewValue = 0.f;
					
				
				

			}
						

		}else{
			total_review_count = "null";
			
		}
		
		
		
		Thread.sleep(5000);
		
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
			

			// subject
			org.w3c.dom.Element course_subject = newCreatedDocument.createElement("subject");
			course_subject.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.subject));
			course_info.appendChild(course_subject);
			
			// total review counts
			org.w3c.dom.Element reveiw_count = newCreatedDocument.createElement("review_count_visible");
			reveiw_count.appendChild(createTextNodeWithoutNull(newCreatedDocument, String.valueOf(data.total_reviewCount_visible)) );
			course_info.appendChild(reveiw_count);
			
			// total review counts
			org.w3c.dom.Element reveiw_count_sum = newCreatedDocument.createElement("review_count_sum");
			reveiw_count_sum.appendChild(createTextNodeWithoutNull(newCreatedDocument, String.valueOf(data.total_reviewCount_calculated)) );
			course_info.appendChild(reveiw_count_sum);
			
			// average review value
			org.w3c.dom.Element avr_review_value = newCreatedDocument.createElement("average_reveiw_value");
			avr_review_value.appendChild(createTextNodeWithoutNull(newCreatedDocument, String.valueOf(data.total_reviewValue)) );
			course_info.appendChild(avr_review_value);

			
			// reviews
			org.w3c.dom.Element reviews = newCreatedDocument.createElement("reviews");
			for(int i=0; i<data.reviewValue.size(); i++){
				org.w3c.dom.Element review = newCreatedDocument.createElement("review");
				
				org.w3c.dom.Element date = newCreatedDocument.createElement("review_date");
				date.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.reviewDate.get(i)));
				
				org.w3c.dom.Element value = newCreatedDocument.createElement("review_value");
				value.appendChild(createTextNodeWithoutNull(newCreatedDocument, String.valueOf(data.reviewValue.get(i))) );
				
				org.w3c.dom.Element content = newCreatedDocument.createElement("review_content");
				content.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.reviewContent.get(i)));
				
				review.appendChild(date);
				review.appendChild(value);
				review.appendChild(content);
				
				
				reviews.appendChild(review);
			}
			course_info.appendChild(reviews);
								
			// url
			org.w3c.dom.Element course_url = newCreatedDocument.createElement("url");
			course_url.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.url));
			course_info.appendChild(course_url);
			
			
	
								
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
