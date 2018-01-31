import java.awt.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

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
	
	
	public ArrayList<String> getAllVideoURL(final String targetSITE)
	{
		
		final ArrayList<String> urlList=new ArrayList<String>();
		
		
		Integer mpage=new Integer(1);
														
		String urlString = targetSITE.concat("?page=").concat(
				String.valueOf(mpage));
		
		pagelist.add(mpage);
		
		// jSoup Connection instance 
		Connection mainConn;
		Document mainDom = null;
		mainConn = Jsoup.connect(urlString).timeout(1000*5);

		try {
			mainDom = mainConn.get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Element nav = mainDom.getElementsByClass("js-course-pagination").get(0).getElementsByClass("pagination").get(0).getElementsByTag("input").get(0);
		
		System.out.println(nav);
		

		final int pageSize = Integer.valueOf(nav.attr("value"));
		System.out.println(pageSize);
											
		while (true) {
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					
					Integer mpage=new Integer(page);
					++page;
															
					String urlString = targetSITE.concat("?page=").concat(
							String.valueOf(mpage));
					
					pagelist.add(mpage);
					
					// jSoup Connection instance 
					Connection mainConn;
					Document mainDom = null;
					mainConn = Jsoup.connect(urlString).timeout(1000*5);

					try {
						mainDom = mainConn.get();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					Elements hrefs = mainDom.getElementsByTag("main").first().child(0)
							.getElementsByClass("course-listing__leftpanel").first().child(3).select("div.course-listing-card");
					if( mpage <= pageSize){
						
						for(int i=0; i<hrefs.size(); i++){
							
							
							// can assign title, provider, course-id, reviewValue, and 

//							courseData.title = hrefs.get(i).select("a[data-analytics-course").text().toString();
//							courseData.provider = hrefs.get(i).select("a[data-analytics-course").attr("data-analytics-provider").toString();
//							courseData.id = hrefs.get(i).select("a[data-analytics-course").attr("data-analytics-course-id").toString();
//							courseData.reviewValue = hrefs.get(i).select("meta[itemprop]").get(0).attr("content").toString();
//							courseData.reviewCount = hrefs.get(i).select("li.course-listing-summary__ratings__number").text().toString().split(" ")[0];
//							
							courseData.url = hrefs.get(i).select("a[data-analytics-course").attr("href").toString();
							System.out.println(courseData.url);
							urlList.add(courseData.url);
												
						}
					
					}else{
						endData = true;
					}
						
				}
			}).start();
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			if(endData)
			{
				//threadLock
				while(Thread.activeCount()!=1)
				{
				}
				for (String string : urlList) {
					System.out.print(string+"\n");
				}

				System.out.print("-----Url size(the number of Courses in couretalk.com)------\n");
				System.out.print(urlList.size()+"\n");
				return urlList;
				
			}
		
		}
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
				connection = Jsoup.connect(url).timeout(10*1000);
				
				// newCreatedDocument is destination of XML.
				NodeList nodelist=newCreatedDocument.getElementsByTagName("ROOT");
				Node root=nodelist.item(0);
				
				Document document = null;
				
				Data data = new Data();
				data.keyword = new ArrayList<String>();
				data.tag = new ArrayList<String>();


				try {
					document = connection.get();
				} catch (Exception e) {
				}
				
				data.url = url;
	
				Element body = document.body();
				
				Element basic_info = body.getElementById("outbound");
				
				// course title
				data.title = basic_info.attr("data-analytics-course");
				// course-id
				data.id = basic_info.attr("data-course-id");
				// course-url
				data.url = basic_info.attr("href");
				// course rating
				data.ratingValue = basic_info.attr("data-analytics-rating").toString();
				// course provider
				data.provider = basic_info.attr("data-analytics-provider");
				
						
				Elements metadata = body.select("div.course-info__academic").first().children();
				
				// course_instructors
				data.instructors = metadata.get(0).text();
				// providing school				
				data.school = metadata.get(1).text();
				
				// course_description
				try{
					data.description = metadata.get(2).text().replace('&', '-');
				}catch(Exception e){
					
					data.description = "no description";
				}
				
				data.reviewCount = body.getElementById("reviews").select("h2.heading--underlined").text().split(" ")[0];
				
							
				
				
				Elements description = metadata.get(2).select("p");
				for (Element element : description) {
					try{
						data.description.concat(element.text());
					}catch(Exception e){
						data.description= "no description";
					}
					
								
				}
				
				// making dom elements
				org.w3c.dom.Element course_info = newCreatedDocument.createElement("Course");
								
				root.appendChild(course_info);
				{
					
					// index
					org.w3c.dom.Element course_id = newCreatedDocument
							.createElement("course_id");
					course_id.appendChild(newCreatedDocument.createTextNode(data.id));
					course_info.appendChild(course_id);
					
					// title
					org.w3c.dom.Element course_title = newCreatedDocument.createElement("title");
					course_title.appendChild(newCreatedDocument.createTextNode(data.title));
					course_info.appendChild(course_title);
					
					// provider
					org.w3c.dom.Element course_provider = newCreatedDocument.createElement("provider");
					course_provider.appendChild(newCreatedDocument.createTextNode(data.provider));
					course_info.appendChild(course_provider);
					
					// rating
					org.w3c.dom.Element course_rating = newCreatedDocument.createElement("rating");
					course_rating.appendChild(newCreatedDocument.createTextNode(data.ratingValue));
					course_info.appendChild(course_rating);
					
					
					// revirw Count
					org.w3c.dom.Element review_count = newCreatedDocument.createElement("review_count");
					review_count.appendChild(newCreatedDocument.createTextNode(data.reviewCount));
					course_info.appendChild(review_count);
					
					// instructor 
					org.w3c.dom.Element course_instructor = newCreatedDocument.createElement("instructor");
					course_instructor.appendChild(newCreatedDocument.createTextNode(data.instructors));
					course_info.appendChild(course_instructor);
					
					// school
					org.w3c.dom.Element course_school = newCreatedDocument.createElement("school");
					course_school.appendChild(newCreatedDocument.createTextNode(data.school));
					course_info.appendChild(course_school);
					
										
					// description
					org.w3c.dom.Element course_description = newCreatedDocument.createElement("description");
					course_description.appendChild(newCreatedDocument.createTextNode(data.description));
					course_info.appendChild(course_description);
					
					// url
					org.w3c.dom.Element course_url = newCreatedDocument.createElement("url");
					course_url.appendChild(newCreatedDocument.createTextNode(data.url));
					course_info.appendChild(course_url);
					
										
				}
			}
		}).start();
	}

	
}
