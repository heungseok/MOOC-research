import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class Main {
	public final static String targetSITE = "https://www.edx.org";

	public final static String targetProvider = "/providers/edx/courses";
	public static ArrayList<String> TOPICURLLIST = new ArrayList<String>();
	public static ArrayList<String> URLLIST = new ArrayList<String>();
	
	public final static String targetUrlList[] = 
		{ 
				"https://www.class-central.com/courses/past",
				"https://www.class-central.com/courses/selfpaced",
				"https://www.class-central.com/courses/upcoming",
				"https://www.class-central.com/courses/recent",
				"https://www.class-central.com/courses/recentlyAdded",				
				"https://www.class-central.com/courses/ongoing"
								
		}; 
	
	/*
	https://www.class-central.com/courses/recent	= recently started or starting soon
	https://www.class-central.com/courses/recentlyAdded	= Just Announced
	https://www.class-central.com/courses/ongoing	= Courses in Progress
	https://www.class-central.com/courses/upcoming	= Future Courses
	https://www.class-central.com/courses/selfpaced	= Self Paced
	https://www.class-central.com/courses/past	= past

*/	
	public static void main(String[] args) throws Exception {
		
		Tool tool=new Tool();
		
		tool.initDriver();
		
/*		for(int i=0; i<targetUrlList.length; i++){
			try{
				URLLIST.addAll(tool.getVideoURL(targetUrlList[i]));
				Thread.sleep(1000);
			}catch (IOException e1){
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		
		tool.quitDriver();
		
		
		removeDuplicates(URLLIST);
		
		// write URL file
		tool.writeCourseURL(URLLIST);*/
		
		
		// read URL file
		URLLIST = tool.readCourseURL();
		removeDuplicates(URLLIST);
		
		System.out.println("The size of URL is - " + URLLIST.size());
		System.out.println("Finish getting all courses url, Let's crawl course data");
		
		// Document init 
		org.w3c.dom.Document document=tool.createDomRoot();
	
	
		System.out.print("----Start crawling with URL.---");
		System.out.println();
		
		for (int url_id = 0; url_id <URLLIST.size(); url_id++) {
//		for (int url_id = 0; url_id <10; url_id++) {
			
			String urlString = URLLIST.get(url_id);
			while(Thread.activeCount() > 40)
			{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.print(urlString+"\n");
			System.out.print("activeThread : "+ Thread.activeCount()+"\n");
			
//			tool.crawlData(urlString, url_id);
			tool.crawlData_Jsoup(urlString, url_id);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		System.out.print("End of Crawling\n");
		System.out.print("Start to make xml file\n");

		
		
		// create DOM file 
		if (document != null) {
			DOMSource xmlDOM = new DOMSource(document);

			Date now = new Date();
			
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
			
			StreamResult xmlFile = new StreamResult(new File("ClassCentral_courseData"+format.format(now)+".xml"));
			
			try {
				TransformerFactory.newInstance().newTransformer()
						.transform(xmlDOM, xmlFile);
			} catch (TransformerException
					| TransformerFactoryConfigurationError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 	
		}
		
		// write error page log
		tool.write_ERROR_CourseURL();
		
		
		// Finish
		System.out.println("Finish all crawling");
		
		
	}
	
	static void makeCoursesUrlFile() throws IOException{
		OutputStream output = new FileOutputStream("courses_url.txt");
		for(String url : URLLIST){
			
			try{
				output.write((url+'\n').getBytes());
			}catch(Exception e){
				e.printStackTrace();
			}
							
		}
		output.close();
	}
	
	static void removeDuplicates(ArrayList<String> list)
	{
	    int count = list.size();

	    for (int i = 0; i < count; i++) 
	    {
	        for (int j = i + 1; j < count; j++) 
	        {
	            if (list.get(i).equals(list.get(j)))
	            {
	                list.remove(j--);
	                count--;
	            }
	        }
	    }
	}
}
