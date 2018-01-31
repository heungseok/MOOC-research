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
	boolean endReviewData = false;
	int page = 1;
	int review_page = 1;
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
							courseData.url = hrefs.get(i).select("a[data-analytics-course]").attr("href").toString();
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
				page = 1;
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
		
		Connection connection;
		final int page_size;
		
		Integer mpage=new Integer(1);
		
		String urlString = url.concat("?page=").concat(String.valueOf(mpage).concat("#reviews"));
						
		connection = Jsoup.connect(urlString).timeout(10*1000);
		Document document = null;
		
								
		try {
			document = connection.get();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
						
		Element review = document.getElementById("reviews");
		
		if(review == null){
			return;
		}
		
		Elements review_nav = review.select("nav").select("li");
				
		if(review_nav.isEmpty()){
			page_size = 1;
		}else{
			page_size = Integer.valueOf(review_nav.get(review_nav.size()-2).text());
		}
		System.out.println("reviews page size - " + page_size);
		
		this.crawlReview(url, page_size); 
			
				
	}

	private void crawlReview(String url, int page_size) {
		// TODO Auto-generated method stub
		
		Connection connection;
		Integer reviewPage=new Integer(1);
		String urlString;
										
		// newCreatedDocument is destination of XML.
		NodeList nodelist=newCreatedDocument.getElementsByTagName("ROOT");
		Node root=nodelist.item(0);
			
		Document document;
			
		Data data = new Data();
		
		for( ;reviewPage <= page_size; reviewPage++){
			
			urlString = url.concat("?page=").concat(String.valueOf(reviewPage).concat("#reviews"));
			
			//System.out.println("current url- " + urlString);
			connection = Jsoup.connect(urlString).timeout(10*1000);
				
			// newCreatedDocument is destination of XML.
							
			document = null;
													
			try {
				document = connection.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
					
			Element body = document.body();
			Element basic_info = body.getElementById("outbound");
			
			// course title
			data.title = basic_info.attr("data-analytics-course");
			// course-id
			data.id = basic_info.attr("data-course-id");
			// course rating
			data.ratingValue = basic_info.attr("data-analytics-rating").toString();
			// course provider
			data.provider = basic_info.attr("data-analytics-provider");

			Elements metadata = body.select("div.course-info__academic").first().children();
			// providing school				
			data.school = metadata.get(1).text();
				
			Elements reviews = document.getElementById("reviews").select("div.reviews-list__item");
			System.out.println("size of reviews at url - " + urlString + " = " + reviews.size());
			
					
			/*
			 *  review data is always starting second index of reviews Elements
			 *  Therefore, if the target course dosen't have any review, the for-loop below of this comment will be not operate 
			 */
			for(int i=2; i<reviews.size(); i++){
				
				// making dom elements
				org.w3c.dom.Element review_info = newCreatedDocument.createElement("Course-Reivew");
				root.appendChild(review_info);
				{
					data.review_data_id = reviews.get(i).getElementsByAttribute("data-review-id").attr("data-review-id");
					data.reviewer_name = reviews.get(i).select("p.userinfo__username").get(0).text();
					if(reviews.get(i).select("a.link-unstyled").size()>0){
						data.reviewer_url = reviews.get(i).select("a.link-unstyled").get(0).attr("href");
						System.out.println(data.reviewer_url);
					}else{
						data.reviewer_url = "null";
					}
					
					if(!data.reviewer_name.toLowerCase().equals("student")){
						try{
							data.other_review = reviews.get(i).select("li.userinfo-activities__item--reviews-count").get(0).getElementsByTag("b").get(0).text();
							data.other_completed = reviews.get(i).select("li.userinfo-activities__item--courses-complited").get(0).getElementsByTag("b").get(0).text();
						}catch(Exception e){
							data.other_review = "anonymous";
							data.other_completed = "anonymous";
						}
						
					}else{
						data.other_review = "anonymous";
						data.other_completed = "anonymous";
					}
					data.review_date = reviews.get(i).select("div.review-body-info").select("time.review-body-info__pubdate").get(0).attr("datetime");
					data.review_value = reviews.get(i).select("meta[itemprop]").get(0).attr("content").toString();	// 0번째 child == 'ratingValue'
					
					data.review = reviews.get(i).select("div.review-body__content").get(0).text().replace('&', '-');
					
					data.helpful_rate = reviews.get(i).select("span.mini-poll-control__option-rating.js-helpful__rating").get(0).text();
					data.status = reviews.get(i).select("div.review-body-info").select("span.review-body-info__course-stage--completed").text();
//					System.out.println(data.status);
					
					
					// course_id
					org.w3c.dom.Element course_id = newCreatedDocument
							.createElement("course_id");
					course_id.appendChild(newCreatedDocument.createTextNode(data.id));
					review_info.appendChild(course_id);
					
					// course_title
					org.w3c.dom.Element course_title = newCreatedDocument.createElement("course_title");
					course_title.appendChild(newCreatedDocument.createTextNode(data.title));
					review_info.appendChild(course_title);
					
					// course_school
					org.w3c.dom.Element course_school = newCreatedDocument.createElement("school");
					course_school.appendChild(newCreatedDocument.createTextNode(data.school));
					review_info.appendChild(course_school);
					
					// review_id
					// This web-site are not providing users' identification 
					org.w3c.dom.Element review_id = newCreatedDocument.createElement("review_data_id");
					review_id.appendChild(newCreatedDocument.createTextNode(data.review_data_id));
					review_info.appendChild(review_id);
					
					// reviewer name
					org.w3c.dom.Element reviewer = newCreatedDocument.createElement("reviewer_name");
					reviewer.appendChild(newCreatedDocument.createTextNode(data.reviewer_name));
					review_info.appendChild(reviewer);
					
					// other_review
					org.w3c.dom.Element other_review = newCreatedDocument.createElement("other_review_count");
					other_review.appendChild(newCreatedDocument.createTextNode(data.other_review));
					review_info.appendChild(other_review);
					
					// other_completed
 					org.w3c.dom.Element other_completed = newCreatedDocument.createElement("other_course_completed");
					other_completed.appendChild(newCreatedDocument.createTextNode(data.other_completed));
					review_info.appendChild(other_completed);
					
					// reviewer url
					org.w3c.dom.Element reviewer_url = newCreatedDocument.createElement("reviewer_url");
					reviewer_url.appendChild(newCreatedDocument.createTextNode(data.reviewer_url));
					review_info.appendChild(reviewer_url);
									
					// review_date
					org.w3c.dom.Element review_date = newCreatedDocument.createElement("review_date");
					review_date.appendChild(newCreatedDocument.createTextNode(data.review_date));
					review_info.appendChild(review_date);
					
					// review_value
					// This review_value means the value of courses' content
					org.w3c.dom.Element review_value = newCreatedDocument.createElement("review_value");
					review_value.appendChild(newCreatedDocument.createTextNode(data.review_value));
					review_info.appendChild(review_value);
					
					org.w3c.dom.Element review_status = newCreatedDocument.createElement("review_status");
					if(data.status.isEmpty()){
						review_status.appendChild(newCreatedDocument.createTextNode("null"));
					}else{
						review_status.appendChild(newCreatedDocument.createTextNode(data.status));
					}
							
					review_info.appendChild(review_status);
					
					// review contents
					org.w3c.dom.Element review = newCreatedDocument.createElement("review");
					if(data.review.isEmpty()){
						review.appendChild(newCreatedDocument.createTextNode("null"));
					}else{
						review.appendChild(newCreatedDocument.createTextNode(data.review));
					}
//					review.appendChild(newCreatedDocument.createTextNode(data.review));
					review_info.appendChild(review);
					
					
					
					// helpful rate
					org.w3c.dom.Element helpful_rate = newCreatedDocument.createElement("helpful_rate");
					helpful_rate.appendChild(newCreatedDocument.createTextNode(data.helpful_rate));
					review_info.appendChild(helpful_rate);
					
				}
			}
			

		}
			
		System.out.println("last page of the course - " + reviewPage);
		System.out.println("finish crawling in - " + url);
		return;
		
	}
			
		
		

}