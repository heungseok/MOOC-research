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
		
		//pagelist.add(mpage);
		
		// jSoup Connection instance 
		Connection mainConn;
		Document mainDom = null;
		mainConn = Jsoup.connect(urlString).timeout(1000*20);

		try {
			mainDom = mainConn.get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Element nav = mainDom.getElementsByClass("js-course-pagination").get(0).getElementsByClass("pagination")
				.get(0).getElementsByTag("input").get(0);
		
		System.out.println(nav);
		
		
		final int pageSize = Integer.valueOf(nav.attr("value"));
		System.out.println(pageSize);
		
		for(int i =0; i<pageSize; i++){
			
																
			urlString = targetSITE.concat("?page=").concat(
					String.valueOf(i+1));
			System.out.println("Try to connect to crwal course-info data at -" + urlString);

			// jSoup Connection instance 
			mainDom = null;
			mainConn = Jsoup.connect(urlString).timeout(1000*20);

			try {
				mainDom = mainConn.get();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Elements hrefs = mainDom.getElementsByTag("main").first().child(0)
					.getElementsByClass("course-listing__leftpanel").first().child(3).select("div.course-listing-card");
			
			
			for(int j=0; j<hrefs.size(); j++){
					courseData.url = hrefs.get(j).select("a[data-analytics-course]").attr("href").toString();
					urlList.add(courseData.url);
										
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
		
		Connection connection;
		
		// cannot connect to the url, then return.
//		if(url.equals("https://www.coursetalk.com/providers/edx/courses/molecular-biology-part-2-transcription-and-transposition-2")){
//			return;
//		}
//		
		final int page_size;
		
		Integer mpage=new Integer(1);
		
		String urlString = url.concat("?page=").concat(String.valueOf(mpage).concat("#reviews"));
						
		connection = Jsoup.connect(urlString).timeout(10*1000);
		Document document = null;
		
								
		try {
			document = connection.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// there is no reivew, so return 
		Element review;
		try{
			review = document.select("div.reviews-list__pagination").get(0);
		} catch (Exception e){
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
						
			Element basic_info = body.select("a.btn.btn-success.btn-size-medium.btn-block.btn-course-page.js-outbound.js-auth-popup_open").first();
			
			// course title
			data.title = basic_info.attr("data-analytics-course");
			// course-id
			data.id = basic_info.attr("data-course-id");
			// course-url
			data.url = basic_info.attr("href");
			
			// course provider
			data.provider = basic_info.attr("data-analytics-provider");
			// review count
			data.reviewCount = body.select("div.course-header-ng__rating__count").select("span").get(1).text().split(" ")[0];
			System.out.println("review Count: " + data.reviewCount);
			
			// course rating
			data.ratingValue = body.select("div.course-header-ng__rating__stars").select("span.sr-only").get(0).text().split(" ")[0];	
			
			


//			Elements metadata = body.select("div.course-info__academic").first().children();
//			
//			if(url.contains("udacity")){
//				data.school = "no school";	
//			}else{
//				// providing school				
//				data.school = metadata.get(1).text();
//			}
//			
			
			
			Elements reviews = document.getElementById("reviews-list").select("div.review.js-review.js-view-trackable");
			
			// if the size of review list is smaller than 1, return.
			if(reviews.size() < 1){
				return;
			}
			System.out.println("size of reviews at url - " + urlString + " = " + reviews.size());
			
					
			
			for(int i=0; i<reviews.size(); i++){
				
				// making dom elements
				org.w3c.dom.Element review_info = newCreatedDocument.createElement("Course-Reivew");
				root.appendChild(review_info);
				{
					// review content id
					data.review_data_id = reviews.get(i).attr("data-review-id");
					
					// reviewer displayed name
					data.reviewer_name = reviews.get(i).select("p.review-body__username").get(0).text();
					
					// if reviewer has id-account, then parse
					if(reviews.get(i).select("a.link-unstyled").size()>0){
						data.reviewer_url = reviews.get(i).select("a.link-unstyled").get(0).attr("href");
						System.out.println(data.reviewer_url);
					}else{
						data.reviewer_url = "null";
					}
					
					if(!data.reviewer_url.equals("null")){
					
						data.other_review = reviews.get(i).select("li.userinfo-activities__item--reviews-count").get(0).getElementsByTag("b").get(0).text();
						data.other_completed = reviews.get(i).select("li.userinfo-activities__item--courses-complited").get(0).getElementsByTag("b").get(0).text();
											
					}else{
						data.other_review = "anonymous";
						data.other_completed = "anonymous";
					
					}
										
					
					data.review_date = reviews.get(i).select("div.review-body-info").select("time.review-body-info__pubdate").get(0).attr("datetime");
					data.review_value = reviews.get(i).select("div.review-body-info").select("span.sr-only").get(0).text().split("/")[0];
					System.out.println("reviewValue: " + data.review_value);
					data.review = reviews.get(i).select("div.review-body__content").get(0).text().replace('&', '-');
					data.review = data.review.replace("&#", " ");
					data.helpful_rate = reviews.get(i).select("span.mini-poll-control__option-rating.js-helpful__rating").get(0).text();
					
					try{
						data.status = reviews.get(i).select("div.review-body-info").select("span").get(2).text();
					}catch(Exception e){
						data.status = "none";
					}
					
					System.out.println(data.status);
					
					
					// course_id
					org.w3c.dom.Element course_id = newCreatedDocument
							.createElement("course_id");
					course_id.appendChild(newCreatedDocument.createTextNode(data.id));
					review_info.appendChild(course_id);
					
					// course_title
					org.w3c.dom.Element course_title = newCreatedDocument.createElement("course_title");
					course_title.appendChild(newCreatedDocument.createTextNode(data.title));
					review_info.appendChild(course_title);
					
					// total review counts of course
					org.w3c.dom.Element course_review_count = newCreatedDocument.createElement("review_counts");
					course_review_count.appendChild(newCreatedDocument.createTextNode(data.reviewCount));
					review_info.appendChild(course_review_count);
					
					// avg rating value of the course
					org.w3c.dom.Element course_ratingValue = newCreatedDocument.createElement("rating_value");
					course_ratingValue.appendChild(newCreatedDocument.createTextNode(data.ratingValue));
					review_info.appendChild(course_ratingValue);
					
					// provider
					org.w3c.dom.Element course_provider = newCreatedDocument.createElement("course_provider");
					course_provider.appendChild(newCreatedDocument.createTextNode(data.provider));
					review_info.appendChild(course_provider);
					
					
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
 					org.w3c.dom.Element other_completed = newCreatedDocument.createElement("other_course_completed_count");
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
					
					// review contents
					org.w3c.dom.Element review = newCreatedDocument.createElement("review");
					review.appendChild(newCreatedDocument.createTextNode(data.review));
					review_info.appendChild(review);
					
					org.w3c.dom.Element review_status = newCreatedDocument.createElement("review_status");
					if(data.status.isEmpty()){
						review_status.appendChild(newCreatedDocument.createTextNode("null"));
					}else{
						review_status.appendChild(newCreatedDocument.createTextNode(data.status));
					}
							
					review_info.appendChild(review_status);
					
					
					
					// helpful rate of the review
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