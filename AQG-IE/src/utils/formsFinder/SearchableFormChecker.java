package utils.formsFinder;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.FormTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;


public class SearchableFormChecker {

	private static final String keyword = "(search|find)";
	private Parser parser;
	private TagNameFilter filter;
	private ArrayList<String> nonSearchableforms;
	private boolean searchable;
	private boolean hasForms;
	private Pattern searchPattern;
	private ArrayList<String> searchableForms;
	
	public SearchableFormChecker(){
		
		parser = new Parser();
		
		filter = new TagNameFilter("form");
		
		searchPattern = Pattern.compile(keyword);
		
	}
	
	public boolean hasForm(String htmlWebsite){
		
		searchable = false;
		
		hasForms = false;
		
		nonSearchableforms = new ArrayList<String>();
		
		searchableForms = new ArrayList<String>();
		
        try {
           
        	parser.setResource(htmlWebsite);
            
            NodeList list = parser.parse(filter);
            
            for (int i = 0; i < list.size(); i++) {
				
            	Node node = list.elementAt(i);

            	if (node instanceof FormTag){
                	
            		FormTag ft = (FormTag) node;
                	
            		String text = ft.getText().toLowerCase();
            		
            		if (searchPattern.matcher(text).find()){
            			
            			searchable = true;
                		searchableForms.add(ft.toHtml(true));
                	
            		} else {
                	
                		nonSearchableforms.add(ft.toHtml(true));
                	
                	}
               	
                	hasForms = true;
            	}
            	
			}

        } catch (ParserException e) {
            return false;
        }

		return hasForms;
	
	}
	
	public boolean isSearchable(){
		return searchable;
	}
	
	public ArrayList<String> getNonSearchableForms(){
		return nonSearchableforms;
	}
	
	public ArrayList<String> getSearchableForms(){
		return searchableForms;
	}
	
	public boolean hasNonSearchableForms() {
		return nonSearchableforms.size()>0;
	}
	
}
