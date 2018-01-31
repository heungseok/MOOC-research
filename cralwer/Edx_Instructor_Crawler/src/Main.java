package src;

import java.io.File;
import java.io.IOException;
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

	public static void main(String[] args) throws IOException {
		
		Tool tool=new Tool();

		try {
			URLLIST = tool.getVideoURL();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("The size of URL is - " + URLLIST.size());
		System.out.println("Finish getting all instructors' url, Let's crawl the data");
		
		// Document init 
		org.w3c.dom.Document document=tool.createDomRoot();
	
		System.out.print("----Start crawling with URL.---");
		System.out.println();
		
		for (int url_id = 0; url_id <URLLIST.size(); url_id++) {

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
			
			tool.crawlData(urlString, url_id);
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
			
			StreamResult xmlFile = new StreamResult(new File("edX_courseData"+format.format(now)+".xml"));
			
			try {
				TransformerFactory.newInstance().newTransformer()
						.transform(xmlDOM, xmlFile);
			} catch (TransformerException
					| TransformerFactoryConfigurationError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 	
		}
		// Finish
		System.out.println("Finish all crawling");
		
		
	}
}
