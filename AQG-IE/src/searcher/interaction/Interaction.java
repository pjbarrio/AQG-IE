package searcher.interaction;

import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import exploration.model.Database;
import exploration.model.Document;
import exploration.model.Query;
import exploration.model.database.OnlineDatabase;
import searcher.interaction.formHandler.InteractionForm;
import searcher.interaction.formHandler.TextQuery;

public abstract class Interaction {

	private static final String ACTION = "action";
	private static final String HTTP = "http";
	private static final String PARENT_FOLDER = "../";
	private static final String SAME_FOLDER = "./";
	public static final int TIME_OUT = 10000;
	
	private Database website;
	private String formFile;
	private InteractionForm interactionForm;
	private List<String> inputNames;
	private String encoding;
	
	protected Interaction(Database website, String formFile, String encoding, List<String> inputNames) {
		this.website = website;
		this.formFile = formFile;
		this.encoding = encoding;
		this.inputNames = inputNames;
		this.interactionForm = null;
	}

	protected Database getWebsite(){
		return website;
	}
	
	protected InteractionForm getInteractionForm(){
		
		if (interactionForm == null){
			interactionForm = getInteractionFormInstance(formFile,encoding);
		}
		return interactionForm;
	}
	
	protected abstract InteractionForm getInteractionFormInstance(String formFile, String encoding);

	public abstract Document submitQuery(TextQuery query);
	
	public abstract Reader getNextResultsPage();
	
	protected List<String> getInputNames(){
		return inputNames;
	}
	
	public String getPrefixWebsite() {
		
		String website = ((OnlineDatabase)getWebsite()).getWebsite();
		
		String action = getInteractionForm().getValueOfAttribute(ACTION);
		
		if (action == null)
			action = "";
		
		if (action.startsWith(PARENT_FOLDER)){
			
			int index = website.lastIndexOf("/");
					
			if (index > 0){
				website = website.substring(0,index);
			}
			
			index = website.lastIndexOf("/");
			
			if (index > 0){
				website = website.substring(0,index);
			}
			
			website = website + "/";
			
			action = action.substring(PARENT_FOLDER.length());
		
		}else if (action.startsWith(SAME_FOLDER)){
			
			int index = website.lastIndexOf("/");
			
			if (index > 0){
				website = website.substring(0,index);
			}
			
			website = website + "/";
			
			action = action.substring(SAME_FOLDER.length());
		
		}
		
		if (action.startsWith(HTTP)){
			
			String removingLast = getWebsite().getName().substring(0,getWebsite().getName().length()-1).replace("www.", "");
			
			if (action.replace("www.", "").equals(removingLast))
				return getWebsite().getName();
			
			return action;
			
		}
		
		return concatenate(website,action);
		
	}

	private String concatenate(String website, String action) {
		if (website.endsWith("/") && action.startsWith("/")){
			return website + action.substring(1);
		}
		return website + action;
	}

	public abstract Document _submitQuery(TextQuery query);

}
