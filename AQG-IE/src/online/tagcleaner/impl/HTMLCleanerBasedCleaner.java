package online.tagcleaner.impl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.lang3.StringEscapeUtils;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.HtmlSerializer;
import org.htmlcleaner.SimpleHtmlSerializer;

import online.tagcleaner.HTMLTagCleaner;

public class HTMLCleanerBasedCleaner implements HTMLTagCleaner {

	private CleanerProperties props;
	private HtmlCleaner cleaner;
	private HtmlSerializer htmlSerializer;
	
	public HTMLCleanerBasedCleaner(){
		props = new CleanerProperties();
		props.setOmitXmlDeclaration(true);
		cleaner = new HtmlCleaner(props);
		htmlSerializer = new SimpleHtmlSerializer(props);
	}
	
	@Override
	public String clean(String str) {
		
		try {
					
//			String string = StringEscapeUtils.escapeHtml3(str); 
			
			org.htmlcleaner.TagNode node = cleaner.clean(str);
		
			StringWriter sw = new StringWriter();
			
			htmlSerializer.write(node, sw, "UTF-8");

//			return StringEscapeUtils.unescapeHtml3(sw.toString());
			
			return sw.toString();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public HTMLTagCleaner createInstance() {
		return new HTMLCleanerBasedCleaner();
	}

}
