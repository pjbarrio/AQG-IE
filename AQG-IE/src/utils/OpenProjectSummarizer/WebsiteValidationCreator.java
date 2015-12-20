package utils.OpenProjectSummarizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class WebsiteValidationCreator {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String indexFileName = "/local/pjbarrio/Files/Projects/AutomaticQueryGeneration/Sites/DataIndex.txt";
		
		String folderForms = "/local/pjbarrio/Files/Projects/AutomaticQueryGeneration/Sites/SearchForms/"; 
		
		String websiteFolder = "/local/pjbarrio/Files/Projects/AutomaticQueryGeneration/Sites/SearchWebsite/";
		
		String relatedFormFolder = "../SearchForms/";
		
		File indexFile = new File(indexFileName);
		
		String line;
		
		BufferedReader br = new BufferedReader(new FileReader(indexFile));
		
		while ((line=br.readLine())!=null){
			
			int index = Integer.valueOf(line.substring(0,line.indexOf(',')));
			
			String website = line.substring(line.indexOf(',')+1);
			
			generateWebPage(index,website,folderForms,websiteFolder,relatedFormFolder);
			
		}

		br.close();
	}

	private static void generateWebPage(int index, String website,
			String folderForms, String websiteFolder, String relatedFormFolder) throws IOException {
		
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
		bw.write(getWebsiteObject(25, relatedFormFolder+index + ".html"));
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
		bw.write(generateButton(index-1,"Previous"));
		bw.newLine();
		bw.write("</form>");
		bw.newLine();
		bw.write("</td>");
		bw.newLine();
		bw.write("<td width=\"50%\">");
		bw.newLine();
		bw.write("<form>");
		bw.newLine();
		bw.write(generateButton(index+1,"Next"));
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
