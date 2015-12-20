package searcher.interaction.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;

public class SearchChecker {

	public static void main(String[] args) throws IOException {
		
		String resultsFolder = "/home/pjbarrio/Documents/RelationExtractionStuff/DatabasesInterfaces/ResultsNewSearch/";
		
		String websiteCheckFolder = "/home/pjbarrio/Documents/RelationExtractionStuff/DatabasesInterfaces/ResultsNewSearchChecker/";
		
		String indexFile = "/home/pjbarrio/Documents/RelationExtractionStuff/DatabasesInterfaces/ResultsPage.txt";
		
		String folderResultsPrefixRelative = "../ResultsNewSearch/";
		
		File[] files = new File(resultsFolder).listFiles();
		
		Arrays.sort(files, new Comparator<File>(){

			@Override
			public int compare(File arg0, File arg1) {
				
				String f0 = arg0.getName().replace(".html", "");
				String f1 = arg1.getName().replace(".html", "");
				
				return Integer.valueOf(f0).compareTo(Integer.valueOf(f1));
				
			}
			
		});

		System.setOut(new PrintStream(new File(indexFile)));
		
		for (int i = 0; i < files.length; i++) {
			
			System.out.println(files[i].getName());
			
			int index = Integer.valueOf(files[i].getName().replace(".html",""));
			
			generateWebPage(index,folderResultsPrefixRelative,getIndex(files,i-1),getIndex(files,i+1),websiteCheckFolder);
			
		}
		
	}
	
	private static void generateWebPage(int index, String resultsFolder,
			int prev, int next, String output) throws IOException {
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(output + index + ".html")));
		bw.write("<html>");
		bw.newLine();
		bw.write("<body>");
		bw.newLine();
		bw.write("<table border=\"1\" width=\"100%\" height=\"100%\">");
		bw.newLine();
		bw.write("<tr height=\"95%\">");
		bw.newLine();
		bw.write(getWebsiteObject(100, resultsFolder + index + ".html"));
		bw.newLine();
		bw.write("</tr>");
		bw.newLine();
		bw.write("<tr height=\"5%\">");
		bw.newLine();
		bw.write("<td colspan=2>");
		bw.newLine();
		bw.write("<table border=\"2\" width=\"100%\" height=\"100%\">");
		bw.newLine();
		bw.write("</tr>");
		bw.newLine();
		bw.write("<td width=\"50%\" align=\"right\">");
		bw.newLine();
		bw.write("<form>");
		bw.newLine();
		bw.write(generateButton(prev,"Previous"));
		bw.newLine();
		bw.write("</form>");
		bw.newLine();
		bw.write("</td>");
		bw.newLine();
		bw.write("<td width=\"50%\">");
		bw.newLine();
		bw.write("<form>");
		bw.newLine();
		bw.write(generateButton(next,"Next"));
		bw.newLine();
		bw.write("</form>");
		bw.newLine();
		bw.write("</td>");
		bw.newLine();
		bw.write("</tr>");
		bw.newLine();
		bw.write("</table>");
		bw.newLine();
		bw.write("</td>");
		bw.newLine();
		bw.write("</tr>");
		bw.newLine();
		bw.write("</table>");
		bw.newLine();
		bw.write("</body>");
		bw.newLine();		
		bw.write("</html>");
		bw.newLine();		
			
		bw.close();

		
	}

	private static int getIndex(File[] files, int i) {
		if (i==-1){
			i = files.length-1;
		}
		if (i==files.length){
			i = 0;
		}
		return Integer.valueOf(files[i].getName().replace(".html", ""));
	}

	private static String generateButton(int i, String text) {
		
		return "<input type=\"button\" onClick=\"parent.location='"+ i + ".html'\" value=\""+ text +"\">";
		
	}

	private static String getWebsiteObject(int percent, String website) {
		
		String ret ="";
		
		ret = "<td width=\"" +percent +"%\"><frame><object data=";
		
		ret = ret + website;
		
		ret = ret + " width=\"100%\" height=\"100%\"> <embed src=";
		
		ret = ret + website;
		
		ret = ret + " width=\"100%\" height=\"100%\"> </embed> Error: Embedded data could not be displayed. </object></frame></td>";
		 
		return ret;
		
	}

}
