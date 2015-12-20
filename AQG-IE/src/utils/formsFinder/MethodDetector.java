package utils.formsFinder;

import java.io.File;
import java.util.logging.FileHandler;

import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import utils.FileAnalyzer;
import utils.FileHandlerUtils;

public class MethodDetector {

	private static HasAttributeFilter filterPost = new HasAttributeFilter("method","post");
	
	private static HasAttributeFilter filterGet = new HasAttributeFilter("method","get");
	
	private static HasAttributeFilter filterTextInput = new HasAttributeFilter("type","text");

	private static Parser p = new Parser();
	
	public synchronized static String findMethod(String form) throws ParserException {
		
		String ret = "";
		
		p.setResource(form);
		
		NodeList result = p.parse(filterGet);
		
		if (result.size() > 0){
			ret = "GET";
		}
		
		p.reset();
		
		p.setResource(form);
		
		result = p.parse(filterPost);
		
		if (result.size() > 0){
			ret = ret + "POST";
		}

		p.reset();
		
		p.setResource(form);

		result = p.parse(filterTextInput);
		
		if (result.size() > 0){
			
			if (ret.equals(""))
				return "GET";
			
		}
		
		return ret;
		
	}

}
