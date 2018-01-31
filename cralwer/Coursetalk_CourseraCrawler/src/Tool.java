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
	 
	Data courseData = new Data();
	
	
	public ArrayList<String> getAllVideoURL(final String targetSITE)
	{
		
		final ArrayList<String> urlList=new ArrayList<String>();
		
		
		Integer mpage=new Integer(1);
														
		String urlString = targetSITE.concat("?page=").concat(
				String.valueOf(mpage));
		
	
		
		// jSoup Connection instance 
		Connection mainConn;
		Document mainDom = null;
		// try to connect to the web-site per ten-seconds 
		mainConn = Jsoup.connect(urlString).timeout(10*1000);

		try {
			mainDom = mainConn.get();
			System.out.println("successed to connect");
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
					if(page > pageSize)
						endData = true;
					
					Integer mpage=new Integer(page);
					++page;
																				
					String urlString = targetSITE.concat("?page=").concat(
							String.valueOf(mpage));
					System.out.println("Try to connect to crwal course-info data at -" + urlString);
										
					
					// jSoup Connection instance 
					Connection mainConn;
					Document mainDom = null;
					mainConn = Jsoup.connect(urlString).timeout(10*1000);

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
							
							
							// can assign title, provider, course-id, reviewValue, and reviewCount
							
//							courseData.title = hrefs.get(i).select("a[data-analytics-course").text().toString();
//							courseData.provider = hrefs.get(i).select("a[data-analytics-course").attr("data-analytics-provider").toString();
//							courseData.id = hrefs.get(i).select("a[data-analytics-course").attr("data-analytics-course-id").toString();
//							courseData.reviewValue = hrefs.get(i).select("meta[itemprop]").get(0).attr("content").toString();
//							courseData.reviewCount = hrefs.get(i).select("li.course-listing-summary__ratings__number").text().toString().split(" ")[0];
//							
							courseData.url = hrefs.get(i).select("a[data-analytics-course").attr("href").toString();
							
							urlList.add(courseData.url);
//							System.out.println(courseData.url);
												
						}
					
					}else{
						endData = true;
//						return;
					}
						
				}
			}).start();
			
			
			try {
				Thread.sleep(100);
				// sleep five second per page
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
	
	

	
	public ArrayList<String> getAllVideoURLwithoutThread(final String targetSITE)
	{
		
		final ArrayList<String> urlList=new ArrayList<String>();
		
		
		Integer mpage=new Integer(1);
														
		String urlString = targetSITE.concat("?page=").concat(
				String.valueOf(mpage));
		
	
		
		// jSoup Connection instance 
		Connection mainConn;
		Document mainDom = null;
		// try to connect to the web-site per ten-seconds 
		mainConn = Jsoup.connect(urlString).timeout(10*1000);

		try {
			mainDom = mainConn.get();
			System.out.println("successed to connect");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
			
		Element nav = mainDom.getElementsByClass("js-course-pagination").get(0).getElementsByClass("pagination").get(0).getElementsByTag("input").get(0);
		
		System.out.println(nav);
		

		final int pageSize = Integer.valueOf(nav.attr("value"));
		System.out.println(pageSize);
		
		
		
		// parsing all pages of courses' list
		while (true) {
			if(page > pageSize)
				break;
					
			mpage=new Integer(page);
			++page;
																		
			urlString = targetSITE.concat("?page=").concat(
					String.valueOf(mpage));
			System.out.println("Try to connect to crwal course-info data at -" + urlString);
						
			// jSoup Connection instance 
			mainDom = null;
			mainConn = Jsoup.connect(urlString).timeout(10*1000);

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
					
					courseData.url = hrefs.get(i).select("a[data-analytics-course").attr("href").toString();
					
					urlList.add(courseData.url);

										
				}
			
			}else{
				break;
			}
		}
	
		
	
		for (String string : urlList) {
			System.out.print(string+"\n");
		}
	
		System.out.print("-----Url size(the number of Courses in couretalk.com)------\n");
		System.out.print(urlList.size()+"\n");
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
				data.keyword = new ArrayList<String>();
				data.tag = new ArrayList<String>();


				try {
					document = connection.get();
				} catch (Exception e) {
				}
				
				data.url = url;
//				
//				if(url.equals("http://www.ted.com/talks/john_legend_true_colors")){
//					// This talk isn't provided in TED.com currently.
//					System.out.println("This talk isn't provided in TED.com");
//					return;
//				}else{
//					data.url = url;
//				}
//				
				Element body = document.body();
				
//				Element basic_info = body.getElementById("outbound");
				Element basic_info = body.select("a.btn.btn-success.btn-size-medium.btn-block.btn-course-page.js-outbound.js-auth-popup_open").first();
				
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
				
				// course price info
				data.price = body.select("p.course-details-panel__course-cost").first().text();
								
				// review count
				data.reviewCount = body.select("div.course-header-ng__rating__count").select("span").get(1).text().split(" ")[0];
							
				
				
				// course information such as instructors, institution, and description
				Element course_detail = body.getElementById("incourse-about");
									
								
				// course_instructors
				try{
					data.instructors = course_detail.select("div.course-info__instructors__names").get(0).text().replace("Instructors:  ", "");
				}catch(Exception e){
					data.instructors = "null";
				}
				
				// providing school				
				data.school = course_detail.select("div.course-info__school__name").get(0).text().replace("University:  ", "");
				
				try{
					data.description = course_detail.select("div.course-info__description").get(0).text().replace("&#", "");
				}catch(Exception e){
					data.description = "no decription";
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
					
					// price
					org.w3c.dom.Element price = newCreatedDocument.createElement("price");
					price.appendChild(newCreatedDocument.createTextNode(data.price));
					course_info.appendChild(price);
					
					// review Count
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
