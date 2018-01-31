import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class Tool {
	
	boolean endData=false;
	int page = 1;
	int index = 0;
	ArrayList<Integer> pagelist = new ArrayList<Integer>(); 
	Data courseData = new Data();
	
	int urlSize = 0;
	
	
	public ArrayList<String> getURL(final String targetSITE) throws IOException
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
	
	/*
	 * Crawl data.
	 * 
	 */
	
	public void crawlData(final String url, final Integer index){
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Connection connection;
				connection = Jsoup.connect(url).timeout(0);
				
				// newCreatedDocument is destination of XML.
				NodeList nodelist=newCreatedDocument.getElementsByTagName("ROOT");
				Node root=nodelist.item(0);
				
				Document document = null;
				
				Data data = new Data();
				
				data.course_list = new ArrayList<String>();
				data.course_url = new ArrayList<String>();
				data.course_provider = new ArrayList<String>();
				
				// 리뷰없는 페이지에 대한 exception추가 => we cannot access to private account
				
				try {
					document = connection.get();
				} catch (Exception e) {
				}
				
				data.url = url;
				
				// url tokenize to extract id
				String url_token[] = url.split("/");
				data.id = url_token[url_token.length-1];

				if(document == null){
					return;
				}
				
	
				Element body = document.body();
				// This url is empty about user...=> https://www.coursetalk.com/accounts/profile/mindaugas2
				
				if(body == null){
					//private account
					return;
				}
				
				Elements profile = body.getElementsByClass("profile-userinfo");
				if(profile.size()==0){
					// private account.
					return;
					
				}
								
				data.name = profile.get(0).select("h2.profile-userinfo__username").get(0).text();
				if(data.name == null){
					data.name = "null";
				}
				//System.out.println(data.name);
								
				Elements watched_courses = body.getElementsByClass("profile-review");
				
				if( watched_courses.size()<1){
					return;
				}else{
					data.Size = Integer.toString(watched_courses.size());
				}
				
				
				
				for( Element element: watched_courses){
					
					String title = element.select("a.link-unstyled").get(0).text();
					String course_url = element.select("a.link-unstyled").get(0).attr("href");
					
					data.course_list.add(title);
					data.course_url.add(course_url);
					
					String temp [] = course_url.split("/");
					data.course_provider.add(temp[2]);
					
		
				}
				
				// making dom elements
				org.w3c.dom.Element course_info = newCreatedDocument.createElement("Course");
								
				root.appendChild(course_info);
				{
					
					// id
					org.w3c.dom.Element reviewer_id = newCreatedDocument.createElement("id");
					reviewer_id.appendChild(newCreatedDocument.createTextNode(data.id));
					course_info.appendChild(reviewer_id);
					
					
					// name
					org.w3c.dom.Element reviewer_name = newCreatedDocument.createElement("name");
					reviewer_name.appendChild(newCreatedDocument.createTextNode(data.name));
					course_info.appendChild(reviewer_name);
					
					// size
					org.w3c.dom.Element courses_size = newCreatedDocument.createElement("coursesCount");
					courses_size.appendChild(newCreatedDocument.createTextNode(data.Size));
					course_info.appendChild(courses_size);
					
					
					// courses
					org.w3c.dom.Element courses = newCreatedDocument.createElement("courses");
					for(int i=0; i<data.course_list.size(); i++){
						org.w3c.dom.Element course = newCreatedDocument.createElement("course");
						course.appendChild(newCreatedDocument.
								createTextNode(data.course_list.get(i) + "@" + data.course_provider.get(i)));
						courses.appendChild(course);
						
					}
					course_info.appendChild(courses);
					
					// url
					org.w3c.dom.Element course_url = newCreatedDocument.createElement("url");
					course_url.appendChild(newCreatedDocument.createTextNode(data.url));
					course_info.appendChild(course_url);
					
										
				}
			}
		}).start();
	}

	
}
