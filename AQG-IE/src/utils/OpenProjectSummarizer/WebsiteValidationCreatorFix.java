package utils.OpenProjectSummarizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;

public class WebsiteValidationCreatorFix {

	private static Hashtable<Integer, String> indexTable;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String indexFileName = "/home/pjbarrio/Documents/RelationExtractionStuff/DatabasesInterfaces/SecondRound/Sites/DataIndex.txt";
		
		loadIndex(new File(indexFileName));
		
		String folderForms = "/home/pjbarrio/Documents/RelationExtractionStuff/DatabasesInterfaces/SecondRound/Sites/SearchFormsToFix/"; 
		
		File folder = new File(folderForms);
		
		File[] files = folder.listFiles();
		
		Arrays.sort(files, new Comparator<File>(){

			@Override
			public int compare(File arg0, File arg1) {
				
				String f0 = arg0.getName().replace(".html", "");
				String f1 = arg1.getName().replace(".html", "");
				
				return Integer.valueOf(f0).compareTo(Integer.valueOf(f1));
				
			}
			
		});
		
		String websiteFolder = "/home/pjbarrio/Documents/RelationExtractionStuff/DatabasesInterfaces/SecondRound/Sites/SearchWebsiteToFix/";
		
		String formsFolder = "../SearchFormsToFix/";
		
		for (int i = 0; i < files.length; i++) {
			
			int index = Integer.valueOf(files[i].getName().replace(".html",""));
			
			String website = indexTable.get(index);
			
			generateWebPage(index,website,folderForms,websiteFolder,getIndex(files,i-1),getIndex(files,i+1),formsFolder);
			
		}

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

	private static void loadIndex(File file) throws IOException {
		
		indexTable = new Hashtable<Integer, String>();
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String line;
		
		while ((line=br.readLine())!=null){
			
			int index = Integer.valueOf(line.substring(0,line.indexOf(',')));
			
			String website = line.substring(line.indexOf(',')+1);
			
			indexTable.put(index, website);
			
		}
		
		br.close();
		
	}

	private static void generateWebPage(int index, String website,
			String folderForms, String websiteFolder, int previous, int next,String formsFolder) throws IOException {
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(websiteFolder + index + ".html")));
		bw.write("<html>");
		bw.newLine();
		bw.write("<body>");
		bw.newLine();
		bw.write("<table border=\"1\" width=\"100%\" height=\"100%\">");
		bw.newLine();
		bw.write("<tr height=\"95%\">");
		bw.newLine();
		bw.write(getWebsiteObject(75,website));
		bw.newLine();
		bw.write(getWebsiteObject(25, formsFolder+index + ".html"));
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
		bw.write(generateButton(previous,"Previous"));
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
