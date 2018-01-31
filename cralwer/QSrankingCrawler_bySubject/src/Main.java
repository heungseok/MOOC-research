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
	
	public final static String world_university_rankings = "/subject-rankings/";
	public final static String target_year [] = { "2013", "2014", "2015", "2016" };
	public static ArrayList<ArrayList<String>> url_list = new ArrayList<ArrayList<String>>(4);

	public static void main(String[] args) throws IOException, InterruptedException {
		
		Tool tool=new Tool();
		tool.initDriver();
		
		// Document init 
		org.w3c.dom.Document document=tool.createDomRoot();
		
		System.out.print("----Start crawling with URL.---");
		System.out.println();
		
		
		for (int i=0; i<target_year.length; i++){
			System.out.println("current Year : " + target_year[i]);
			url_list.add(tool.getSubjectURL(targetSITE.concat(world_university_rankings.concat(target_year[i])) ) );
			Thread.sleep(1000);
			
			for(int j=0; j<url_list.get(i).size(); j++){
				
				String targetUrl = url_list.get(i).get(j);
				tool.crawlData(targetUrl, Integer.valueOf(target_year[i]));
				Thread.sleep(1000);
				
			}
				
		}

		
		System.out.print("End of Crawling\n");
		System.out.print("Start to make xml file\n");

	
		// create DOM file 
		if (document != null) {
			DOMSource xmlDOM = new DOMSource(document);

			Date now = new Date();
			
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
			
			StreamResult xmlFile = new StreamResult(new File("QSranking_bySubject(2013-2016)_"+format.format(now)+".xml"));
			
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
