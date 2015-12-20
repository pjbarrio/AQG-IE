package utils.formsFinder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.tags.FormTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import utils.FileHandlerUtils;

public class FinalFormExtractor {

	private static NodeFilter formFilter = new NodeClassFilter(FormTag.class);

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ParserException 
	 * @throws NumberFormatException 
	 */
	public static void main(String[] args) throws IOException, NumberFormatException, ParserException {
		
		String forms = "/home/pjbarrio/Documents/RelationExtractionStuff/DatabasesInterfaces/SecondRound/Sites/FinalFormsTable";
		
		String originalFormsDirectory = "/home/pjbarrio/Documents/RelationExtractionStuff/DatabasesInterfaces/SecondRound/Sites/PrevFinalForms/";
		
		String finalFormsDirectory = "/home/pjbarrio/Documents/RelationExtractionStuff/DatabasesInterfaces/SecondRound/Sites/FinalForms/";
		
		BufferedReader br = new BufferedReader(new FileReader(new File(forms)));
		
		String line;
		
		while ((line = br.readLine())!=null){
			
			System.err.println(line);
			
			String[] splits = line.split(",");
			
			if (splits.length < 3)
				continue;
			
			String index = splits[0];
			
			String form = splits[1];
			
			String method = splits[2];
			
			if (!method.trim().equals("")){
				
				selectForm(originalFormsDirectory + index + ".html", finalFormsDirectory + index + ".html", Integer.valueOf(form).intValue());
				
			}
		}
		
		br.close();
	}

	private static void selectForm(String originalFile, String outputFile, int form) throws ParserException, IOException {
		
		String formContent = StringEscapeUtils.unescapeHtml4(FileHandlerUtils.loadFileasString(originalFile));
		
		Parser p = new Parser(new Lexer(new Page(formContent)));
		
		NodeList forms = p.parse(formFilter);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputFile)));
		
		bw.write(forms.elementAt(form).toHtml(true));
		
		bw.close();
		
	}

}
