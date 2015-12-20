package searcher.interaction.formHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;

import org.apache.commons.lang3.StringEscapeUtils;
import org.htmlparser.Attribute;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.InputTag;
import org.htmlparser.tags.OptionTag;
import org.htmlparser.tags.SelectTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

import utils.FileHandlerUtils;

import exploration.model.Query;

public abstract class InteractionForm {

	private static final String NAME_ATTRIBUTE = "name";
	private static final String VALUE_ATTRIBUTE = "value";
	private static final String TYPE_ATTRIBUTE = "type";
	private static final String CHECKED_ATTRIBUTE = "checked";
	
	
	private static final String RADIO_TYPE = "radio";
	private static final String CHECKBOX_TYPE = "checkbox";
	private static final String CHECKED_STATE = "checked";
	
	private static final String FORM_TAG = "form";
	private static final String SELECTED_ATTRIBUTE = "selected";
	private static final String SELECTED_VALUE = "selected";
	private String formFile;
	private String encoding;
	private Parser parser = new Parser();
	private NodeList inputs;
	private NodeList selects;
	private NodeFilter inputFilter = new NodeClassFilter(InputTag.class);
	private NodeFilter selectFilter = new NodeClassFilter(SelectTag.class);
	public NodeFilter filters = new OrFilter(inputFilter, selectFilter);
	private static Map<String,String> fileTable = new HashMap<String, String>();
	private NodeFilter SelectedOptionFilter = new AndFilter(new NodeClassFilter(OptionTag.class),new HasAttributeFilter(SELECTED_ATTRIBUTE, SELECTED_VALUE)) ;
	
	protected InteractionForm(String formFile, String encoding) {
		this.formFile = formFile;
		this.encoding = encoding;
		loadForm(formFile);
	}

	private void loadForm(String formFile) {
		
		try {

			String content;
			
			synchronized (fileTable) {
				
				content = fileTable.get(formFile);
				
				if (content == null){
					content = FileHandlerUtils.loadFileasString(formFile);
					fileTable.put(formFile, content);
							
				}
				
			}
			
			
			parser.reset();
				
			parser.setLexer(new Lexer(new Page(StringEscapeUtils.unescapeHtml4(content))));
				
			NodeList inForm = parser.parse(filters);
			
			inputs = inForm.extractAllNodesThatMatch(inputFilter);

			selects = inForm.extractAllNodesThatMatch(selectFilter);
		
		} catch (ParserException e) {
			e.printStackTrace();
		}
	}

	protected InteractionForm(String formFile){
		this(formFile,"UTF-8");
	}
	
	public abstract String generateParameters(TextQuery query, List<String> inputNames);

	protected String getEncoding(){
		
		return encoding;
	
	}
	
	protected String getValue(String inputName, int index) {
		
		NodeList nl = inputs.extractAllNodesThatMatch(new HasAttributeFilter(NAME_ATTRIBUTE, inputName));

		TagNode i;
		
		if (nl.size() > 0){
		
			i = (InputTag)nl.elementAt(index);
		
			if (i.getAttribute(TYPE_ATTRIBUTE).equals(RADIO_TYPE) || i.getAttribute(TYPE_ATTRIBUTE).equals(CHECKBOX_TYPE)){
				
				String isChecked = i.getAttribute(CHECKED_ATTRIBUTE);
				
				if (isChecked!=null && isChecked.equals(CHECKED_STATE)){
					
					return i.getAttribute(VALUE_ATTRIBUTE);
					
				}else{
					return null;
				}
				
			}else{
			
				return i.getAttribute(VALUE_ATTRIBUTE);
			}
		}
		else {
			
			nl = selects.extractAllNodesThatMatch(new HasAttributeFilter(NAME_ATTRIBUTE, inputName));
			
			i = (SelectTag)nl.elementAt(index);
			
			NodeList options = i.getChildren().extractAllNodesThatMatch(SelectedOptionFilter);
			
			return ((TagNode)options.elementAt(0)).getAttribute(VALUE_ATTRIBUTE);
			
		}
		
	}

	public String getValueOfAttribute(String attribute) {
		
		try {
			
			parser.reset();
				
			parser.setLexer(new Lexer(new Page(StringEscapeUtils.unescapeHtml4(FileHandlerUtils.loadFileasString(formFile)))));
				
			NodeList forms = parser.parse(new AndFilter(new TagNameFilter(FORM_TAG), new HasAttributeFilter(attribute)));
			
			if (forms.size() == 0)
				return null;
			
			return ((TagNode)forms.elementAt(0)).getAttribute(attribute);
			
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
	}

	
}
