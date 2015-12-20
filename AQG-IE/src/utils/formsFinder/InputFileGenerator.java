package utils.formsFinder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import online.navigation.utils.NavigationUtils;

import org.apache.commons.lang3.StringEscapeUtils;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.tags.InputTag;
import org.htmlparser.tags.SelectTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

import searcher.interaction.formHandler.InteractionForm;
import utils.FileHandlerUtils;

public class InputFileGenerator {

	private static final String TYPE_ATTRIBUTE = "type";
	private static final String NAME_ATTRIBUTE = "name";
	private static final String ID_ATTRIBUTE = "id";
	private static final String TEXT_ATTRIBUTE_VALUE = "text";
	private static final String RADIO_ATTRIBUTE = "radio";
	private static final String CHECKBOX_ATTRIBUTE = "checkbox";
	private static final String TYPE_DEFAULT_VALUE = TEXT_ATTRIBUTE_VALUE;

	private static NodeFilter inputFilter = new NodeClassFilter(InputTag.class);
	private static NodeFilter selectFilter = new NodeClassFilter(SelectTag.class);
	public static NodeFilter filters = new OrFilter(inputFilter, selectFilter);
	
	/**
	 * @param args
	 * @throws ParserException 
	 * @throws IOException 
	 */
	
	
	public static void main(String[] args) throws ParserException, IOException {
		
		String formFileDirectoryName = "/home/pjbarrio/Documents/RelationExtractionStuff/DatabasesInterfaces/SecondRound/Sites/FinalForms/";
		
		String inputFolder = "/home/pjbarrio/Documents/RelationExtractionStuff/DatabasesInterfaces/SecondRound/Sites/PrevFinalInputNames/";
		
		File formFileDirectory = new File(formFileDirectoryName);
		
		FileFilter f;
		
		File[] forms = formFileDirectory.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith("html");
			}
		});
		
		for (int i = 0; i < forms.length; i++) {
			
			Parser p = new Parser(new Lexer(new Page(StringEscapeUtils.unescapeHtml4(FileHandlerUtils.loadFileasString(forms[i].getAbsolutePath())))));
			
			NodeList nl = p.extractAllNodesThatMatch(filters);
			
			SimpleNodeIterator iterator = nl.elements();
			
			List<String> attributeNames = new ArrayList<String>();
			
			boolean hasText = false;
			
			boolean hasRadio = false;
			
			boolean hasCheck = false;
			
			while (iterator.hasMoreNodes()){
				
				Node node = iterator.nextNode();
				
				if (node instanceof InputTag){
					
					InputTag input = (InputTag)node;
					
					String type = NavigationUtils.getAttribute(input, TYPE_ATTRIBUTE);
					
					if (type == null || !isTypeValue(type.toLowerCase())){
						type = TYPE_DEFAULT_VALUE;
					}
					
					type = type.toLowerCase();
					
					String name = input.getAttribute(NAME_ATTRIBUTE);
					
					if (RADIO_ATTRIBUTE.equals(type)){
						if (!hasRadio){
							System.err.println("Radio:" + forms[i].getName());
							hasRadio = true;
						}
					} else if (CHECKBOX_ATTRIBUTE.equals(type))
						if (!hasCheck){
							System.err.println("CheckBox:" + forms[i].getName());
							hasCheck = true;
						}
					if (name != null){
						
						if (TEXT_ATTRIBUTE_VALUE.equals(type)){
							if (hasText){
								System.err.println("More text:" + forms[i].getName());
								attributeNames.add(name);
							}else{
								hasText = true;
								attributeNames.add(0,name);
							}
						}else{
							attributeNames.add(name);
						}
					} else { //name = null recover id
											
						
						name = input.getAttribute(ID_ATTRIBUTE);
						
						if (TEXT_ATTRIBUTE_VALUE.equals(type)){
							
							if (hasText){
								System.err.println("More text:" + forms[i].getName());
								attributeNames.add(name);
							}else{
								hasText = true;
								attributeNames.add(0,name);
							}
						}
						
					}
				} else if (node instanceof SelectTag){
			
					System.err.println("SELECT: " + forms[i].getName());
					
					SelectTag select = (SelectTag)node;
					
					String name = select.getAttribute(NAME_ATTRIBUTE);
					
					if (name != null){
						attributeNames.add(name);
					}					
				}
				
			}
			
			if (!hasText){
				System.err.println("NO TEXT:" + forms[i].getName());
			}
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(inputFolder + forms[i].getName().replaceAll("html", "txt"))));
			
			for (String inputNames : attributeNames) {
				
				bw.write(inputNames);
				
				bw.newLine();
				
			}
			
			bw.close();
			
		}

	}

	private static boolean isTypeValue(String type) {
		
		if (type.equals("text"))
			return true;
		if (type.equals("password"))
			return true;
		if (type.equals("checkbox"))
			return true;
		if (type.equals("radio"))
			return true;
		if (type.equals("submit"))
			return true;
		if (type.equals("reset"))
			return true;
		if (type.equals("file"))
			return true;
		if (type.equals("hidden"))
			return true;
		if (type.equals("image"))
			return true;		
		if (type.equals("button"))
			return true;
		
		return false;
	}

}
