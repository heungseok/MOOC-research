package src;

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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;



public class Tool {
	
	boolean endData=false;
	int page = 1;
	int index = 0;
	ArrayList<Integer> pagelist = new ArrayList<Integer>();
	ArrayList<String> instructorList = new ArrayList<String>();
	
	final WebDriver driver = new FirefoxDriver();
	Data courseData = new Data();

	int urlSize = 0;
	
	public ArrayList<String> getVideoURL() throws IOException{
		
		final ArrayList<String> urlList = new ArrayList<String>();
		
		FileInputStream inputFile = new FileInputStream("./instructor_url.txt");
		
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
	
	public void crawlData(final String url, final Integer index) throws IOException{
		
		// open website
		driver.navigate().to(url);
				
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
				
		if(driver.findElements(By.cssSelector("div.course-start")).size() > 0 ){
			data.status = driver.findElement(By.cssSelector("div.course-start")).findElement(By.tagName("span")).getText().toString();
		}else{
			data.status = "enrollment_closed";
		}
		
		
//				System.out.println(data.status);

	
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
		
		if( driver.findElements(By.cssSelector("div.course-side-area")).size() < 1 )
			return;
		
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
//						data.school = Summary.get(i).getText().replace("Institution: ", "");
				
				if(schoolMap.get(school_url) != null)
					data.school = schoolMap.get(school_name);
				else
					data.school = schoolMap.get(school_name); 
				
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
		
		// Parsing course-about
		
		ArrayList <WebElement> course_Detail = new ArrayList <WebElement> (driver.findElement(By.id("course-about-area"))
				.findElements(By.cssSelector("div.content-grouping")) );
		
		// Before parsing course_about, we need to span the element of description.
		// interact(click) with "see more" and validate whether the element is exist.
		List<WebElement> more_content = course_Detail.get(0).findElements(By.linkText("See more"));
		
		if(more_content.size()>0){
			more_content.get(0).click();
		}
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// after interacting with the element, parsing description and 'what you'll learn
		try{
			data.description = course_Detail.get(0).findElement(By.cssSelector("div.see-more-content")).getText().toString();
			System.out.println("description - " + data.description);
			
		}catch(Exception e){
			data.description = " ";
		}
		
		try{

			data.learnAbout = course_Detail.get(1).findElement(By.cssSelector("ul")).getText().toString();
			System.out.println("What you'll learn - " + data.learnAbout);
			
		}catch(Exception e){
			data.learnAbout = " ";
		}
		
		// Parsing instructors' detail pages, and their info
		data.instructors = new ArrayList<Instructor>();
		
		ArrayList <WebElement> instructors = new ArrayList <WebElement> 
			(driver.findElements(By.cssSelector("li.list-instructor__item")));
		
		for(WebElement instructor : instructors){
			// add detail url to the url list
			instructorList.add(instructor.findElement(By.tagName("a")).getAttribute("href"));
			
			// parsing basic information of the instructors
			
			Instructor inst = new Instructor();
			inst.name = instructor.findElement(By.tagName("a")).findElement(By.tagName("p")).getText();
			ArrayList <WebElement> tempList = new ArrayList <WebElement>
				(instructor.findElements(By.cssSelector("p.instructor-position")));
			if(tempList.size() != 0)
				inst.bio = tempList.get(0).getText().replaceAll("\n", " at ");
			else
				inst.bio = "null";
			
			inst.img = instructor.findElement(By.tagName("a")).findElement(By.tagName("img")).getAttribute("src");
			data.instructors.add(inst);
			
							
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

			
			// instructors
			org.w3c.dom.Element instructor_size = newCreatedDocument.createElement("instructor_size");
			instructor_size.appendChild(createTextNodeWithoutNull(newCreatedDocument, String.valueOf(data.instructors.size())));
			course_info.appendChild(instructor_size);
							
			org.w3c.dom.Element dom_instructors = newCreatedDocument.createElement("instructors");
			for(int i=0; i<data.instructors.size(); i++){
				org.w3c.dom.Element instructor = newCreatedDocument.createElement("instructor");
				
				org.w3c.dom.Element name = newCreatedDocument.createElement("name");
				name.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.instructors.get(i).name));
				org.w3c.dom.Element bio = newCreatedDocument.createElement("bio");
				bio.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.instructors.get(i).bio));
				org.w3c.dom.Element img = newCreatedDocument.createElement("img");
				img.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.instructors.get(i).img));
				instructor.appendChild(name);
				instructor.appendChild(bio);
				instructor.appendChild(img);
				
				
				dom_instructors.appendChild(instructor);
			}
			course_info.appendChild(dom_instructors);
			

			
			// description (intro + description + learnAbout)
			org.w3c.dom.Element course_description = newCreatedDocument.createElement("description");
			course_description.appendChild(createTextNodeWithoutNull(newCreatedDocument, data.intro + data.description + data.learnAbout));
			course_info.appendChild(course_description);
			

											
		}

		if(index == urlSize -1){
			System.out.println("Last page");
			driver.close();
			
			
		}
	
	}
	
	

	
}
