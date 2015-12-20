package utils.OpenProjectSummarizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.htmlparser.util.ParserException;

import utils.formsFinder.MethodDetector;
import utils.formsFinder.SearchableFormChecker;

public class FormFinderValidator {

	private static SearchableFormChecker sfc;

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ParserException 
	 */
	public static void main(String[] args) throws IOException, ParserException {
		
		sfc = new SearchableFormChecker();
		
		String databasesInterfaces = "/local/pjbarrio/Files/Projects/AutomaticQueryGeneration/Sites/Data/";
		
		String databasesInterfacesForm = "/local/pjbarrio/Files/Projects/AutomaticQueryGeneration/Sites/SearchForms/";
		
		File[] files = new File(databasesInterfaces).listFiles();
		
		Arrays.sort(files, new Comparator<File>(){

			@Override
			public int compare(File arg0, File arg1) {
				
				String f0 = arg0.getName().replace(".html", "");
				String f1 = arg1.getName().replace(".html", "");
				
				return Integer.valueOf(f0).compareTo(Integer.valueOf(f1));
				
			}
			
		});
		
		for (File file : files) {
			
//			System.err.println(file);
			
			ArrayList<String> searchableForms = searchForms(file);
			
			write(databasesInterfacesForm,searchableForms,file.getName().replace(".html", ""));
							
		}
		
	}

	private static ArrayList<String> searchForms(File file) throws ParserException {
		
		ArrayList<String> ret;
		
		System.out.print(file.getName());
		
		if (sfc.hasForm(file.getAbsolutePath())){
						
			if (sfc.isSearchable()){
				
				ret = sfc.getSearchableForms();
				
				for (int i = 0; i < ret.size(); i++) {
					
					System.out.print("," + i + "-" + MethodDetector.findMethod(ret.get(i)));
					
				}
				
				System.out.println("");
				return ret;
			}
		}

		System.out.println("");
		
		String st = "<p> <b>NO SEARCHABLE FORM</b> <br> Choose a form or -NO- to cancel </p>";
		
		ret = new ArrayList<String>();
		
		ret.add(st);
		
		return ret;
		
	}

	private static void write(String databasesInterfacesForm, ArrayList<String> forms, String file) throws IOException {
		
		String fileout = databasesInterfacesForm + file + ".html";
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileout));
		
		bw.write("<html>");
		bw.newLine();
		bw.write("<body>");
		bw.newLine();

		int i = 0;
		
		for (String form : forms) {
			
			bw.write("<h1> Form " + i++ + ": </h1>");
			
			bw.newLine();
			
			bw.write(form);
			
		}
		
		bw.write("</body>");
		bw.newLine();
		bw.write("</html>");
		bw.newLine();
		
		bw.close();
		
	}

}
