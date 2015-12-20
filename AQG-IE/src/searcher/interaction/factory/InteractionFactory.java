package searcher.interaction.factory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.htmlparser.util.ParserException;

import exploration.model.Database;

import searcher.interaction.GETInteraction;
import searcher.interaction.Interaction;
import searcher.interaction.POSTInteraction;
import searcher.interaction.formHandler.POSTInteractionForm;
import utils.FileHandlerUtils;
import utils.formsFinder.MethodDetector;

public class InteractionFactory {

	private static final String GET = "GET";
	
	private static final String POST = "POST";
	
	public synchronized static Interaction generateInstance(Database website, String formFile, String encoding, List<String> inputNames){
		
		String method = "";
		
		try {
			method = getFormMethod(formFile);
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (GET.equals(method)){
			
			return new GETInteraction(website, formFile, encoding, inputNames);
			
		}else if (POST.equals(method)){
			
			return new POSTInteraction(website,formFile,encoding,inputNames);
			
		}
		
		return null;
		
		
	}

	private synchronized static String getFormMethod(String formFile) throws ParserException {
		
		String formString = FileHandlerUtils.loadFileasString(formFile);
		
		return MethodDetector.findMethod(formString);
		
	}
	
	public synchronized static String encode(String text, String encoding){
		try {
			return URLEncoder.encode(text, encoding);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return text;
	}
	
	public synchronized static String decode(String text, String encoding){
		try {
			return URLDecoder.decode(text, encoding);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return text;
	}
	
}
