package online.documentRetriever.download;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;

import online.documentRetriever.download.impl.DownloaderRunnable;

import org.apache.commons.io.IOUtils;

import utils.thread.TimedOutTask;
import utils.thread.UnderTimeOutRunnable;

public class Downloader {

	private static final int MAX_ATTEMPTS = 3;
	private static final int TIME_BETWEEN_TRIAL = 1000;
	private static final int TIME_OUT = 10000;
	private static final String EMPTY_STRING = "";

	public String download(URL url) {
		
//		if (!isRetrievable(url)){
//			return EMPTY_STRING;
//		}
		
		return get(url);
		
	}

	private boolean isRetrievable(URL url) {
		
		if (url.getPath().endsWith(".pdf") || url.getPath().endsWith(".flv"))
			return false;
		
		return true;
		
	}

	private String get(URL url) {
		
		UnderTimeOutRunnable<String> dr = new DownloaderRunnable<String>(this,url);
		
		TimedOutTask<String> tot = new TimedOutTask<String>(TIME_OUT,dr);
		
		return tot.execute();
		
	}

	public String retrieve(URL url) {
		
		System.out.println("retrieving: " + url);
		
		int attempt = 0;
		
		while (attempt < MAX_ATTEMPTS){
			
			try {
				
				Thread.sleep(attempt*TIME_BETWEEN_TRIAL);
				
				URLConnection conn = url.openConnection();
				
				conn.setConnectTimeout(TIME_OUT);
				
				conn.setReadTimeout(TIME_OUT);
				
				conn.setRequestProperty("User-agent", "spider");
				
				conn.setRequestProperty("Additional-Information", "http://db-pc(02|04|13|14|15|16|17).cs.columbia.edu/, http://cs.columbia.edu/~pjbarrio/");
				conn.setRequestProperty("Contact-Information", "pjbarrio@cs.columbia.edu");
				
				conn.connect();
				
				StringWriter sw = new StringWriter();
				
				IOUtils.copy(new InputStreamReader(conn.getInputStream()), sw);
				
				String ret = sw.toString();
				
				if  (ret == null)
					ret = "";
				
				return ret;
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}

			if (attempt==0){
				
				try{

					String query = url.getQuery();
					
					if (query != null){
					
						String[] atts = query.split("&");
						
						if (atts.length > 0){
						
							String[] sp = atts[0].split("=");
							
							String newQ = "";
							
							if (sp.length > 1)
								newQ = URLEncoder.encode(sp[0],"UTF-8") + "=" + URLEncoder.encode(sp[1], "UTF-8");
							else{
								
								if (atts[0].contains("="))
									newQ = sp[0] + "=";
								else {
									newQ = sp[0];
								}
							}
							for (int i = 1; i < atts.length; i++) {
								
								String[] spl = atts[i].split("=");
								
								if (spl.length > 1)
									newQ += "&" + URLEncoder.encode(spl[0],"UTF-8") + "=" + URLEncoder.encode(spl[1], "UTF-8");
								else{
									
									if (atts[i].contains("=")){
										newQ += "&" + URLEncoder.encode(spl[0],"UTF-8") + "=";
									}else{
										newQ += "&" + URLEncoder.encode(spl[0],"UTF-8");
									}
									
	
								}
							}
							
							url = new URL(url.toString().replace(query, newQ));
						
						}
						
					}
					
					} catch (IOException e1){
						e1.printStackTrace();
					}
					
				
			}
			attempt++;
			
		}

		return "";
		
	}
	
}
