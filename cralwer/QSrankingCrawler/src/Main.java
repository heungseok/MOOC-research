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
	public final static String targetSITE = "http://www.topuniversities.com";
	
	
	public final static String targetProvider = "/providers/edx/courses";
	public final static String courseListSite = "https://www.coursera.org/directory/courses";
	
	public static ArrayList<String> URLLIST = new ArrayList<String>();
	
	public final static String world_university_rankings = "/university-rankings/world-university-rankings/";
	public final static String target_year [] = { "2012", "2013", "2014", "2015", "2016" };

	public static void main(String[] args) throws IOException {
		
		Tool tool=new Tool();
		tool.initDriver();

		// Document init 
		org.w3c.dom.Document document=tool.createDomRoot();
		
		System.out.print("----Start crawling with URL.---");
		System.out.println();
		
		for (int i=0; i<target_year.length; i++){
			String targetUrl = new String();
			targetUrl = targetSITE.concat(world_university_rankings.concat(target_year[i]));
			
			tool.crawlData(targetUrl, Integer.valueOf(target_year[i]));
//			tool.crawlIndicatorData(targetUrl, Integer.valueOf(target_year[i]));
			
			try {
				Thread.sleep(1000);
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
			
			StreamResult xmlFile = new StreamResult(new File("QSranking"+format.format(now)+".xml"));
			
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
