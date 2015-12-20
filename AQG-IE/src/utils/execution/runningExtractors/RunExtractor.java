package utils.execution.runningExtractors;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import online.navigation.utils.NavigationUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import utils.id.DatabaseIndexHandler;

import extraction.com.clearforest.CalaisLocator;

public class RunExtractor {

	public class ExtractRunnable implements Runnable{

		private File toSave;
		private File toSaveExtraction;

		public ExtractRunnable(File toSave, File toSaveExtraction) {
			this.toSave = toSave;
			this.toSaveExtraction = toSaveExtraction;
		}

		@Override
		public void run() {
			
			String result;
			
			try {
				
				int attemptsTotal = 3;

				int attempts = 0;

				do{

					try {
						Thread.sleep(3000 * attempts);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
					result = new CalaisLocator().getcalaisSoap().enlighten(licenseID , FileUtils.readFileToString(toSave), paramsXML);
				
					attempts++;
					
				} while (result.startsWith("<Error") && attempts <= attemptsTotal); 
					
				FileUtils.writeStringToFile(toSaveExtraction, result);
				
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			

		}
		
	}
	
	private static String paramsXML;
	private static String licenseID = "y2k3xz3rwfpzg2rzd764rm96";

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ServiceException 
	 * @throws ParserException 
	 */
	public static void main(String[] args) throws IOException, ParserException, ServiceException {
		
		for (int hh = 0; hh < 1000; hh++){
				
			System.gc();
			
			System.setErr(new PrintStream(new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/tfidf/extraction"+ hh +".err")));
			
			Set<URL> processed = new HashSet<URL>();
			
			paramsXML = FileUtils.readFileToString(new File("/proj/dbNoBackup/pjbarrio/workspace/SampleGeneration/data/calaisParams.xml"));
			
			String type = "smalltraining";
			
			String technique = "TEDW";
			
			String extractionType = "total";
			
			List<String> ids = FileUtils.readLines(new File("/proj/dbNoBackup/pjbarrio/Experiments/Wrappers/" + type));
			
			File folder = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/tfidf/SizeOrderedResults/" + type + "/" + technique + "/" + extractionType+ "/");
			
			File outputFolder = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/tfidf/SizeOrderedExtractions/" + type + "/" + technique + "/" + extractionType+ "/");
			
			File databaseIndex = new File("/local/pjbarrio/Files/Projects/AutomaticQueryGeneration/Forms/FinalSelection/FinalInteractionIndex.txt");
			
			Map<Integer, String> table = DatabaseIndexHandler.loadInvertedDatabaseIndex(databaseIndex);
			
			Map<Integer, URL> urlTable = new HashMap<Integer, URL>();
			
			for (String string : ids) {
				
				int id = Integer.valueOf(string);
				
				urlTable.put(id,new URL(table.get(id)));
				
			}
			
			table.clear();
			
			File[] bySizeFolder = folder.listFiles();
			
			for (int i = 0; i < bySizeFolder.length; i++) {
				
				File bySize = bySizeFolder[i];
				
				System.out.println("Size: " + bySize.getName());
				
				File[] toExtract = bySize.listFiles();
				
				File outPref = new File(outputFolder,bySize.getName());
				
				outPref.mkdir();
				
				for (int j = 0; j < toExtract.length; j++) {
					
					System.out.println("Extract: " + toExtract[j].getName());
					
					File toSavePrefix = new File(outPref,FilenameUtils.getBaseName(toExtract[j].getName()) + "/");
					
					toSavePrefix.mkdir();
					
					extract(processed, urlTable.get(Integer.valueOf(FilenameUtils.getBaseName(toExtract[j].getName()))), toExtract[j],toSavePrefix);
					
				}
				
			}
		
		
		}

	}

	private static void extract(Set<URL> processed, URL website, File file, File toSavePrefix) throws IOException, ParserException {
		
		String htmlContent = FileUtils.readFileToString(file);
		
		Parser parser = new Parser(new Lexer(new Page(htmlContent, "UTF-8")));
		
		NodeList result = parser.parse(new HasAttributeFilter(NavigationUtils.HREF_ATTRIBUTE));
		
		for (int i = 0; i < result.size(); i++) {
			
			System.out.println("Extracting: " + i + " out Of: " + result.size());
			
			File toSave = new File(toSavePrefix, i + ".html");
			
			File toSaveExtraction = new File(toSavePrefix,i + ".rdf");
			
			if (toSaveExtraction.exists())
				continue;
			
			new RunExtractor().execute(processed, website, ((TagNode)result.elementAt(i)).getAttribute(NavigationUtils.HREF_ATTRIBUTE), toSave, toSaveExtraction);
			
		}
		
	}

	private void execute(Set<URL> processed, URL website, String html, File toSave, File toSaveExtraction) throws MalformedURLException {
		
		Thread t = new Thread(new RunExecutorRunnable(processed, website,html,toSave,toSaveExtraction));
		
		t.start();
		
	}

	synchronized static void extract(File toSave, File toSaveExtraction){
		
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		new RunExtractor().sendRequest(toSave,toSaveExtraction);
		
	}

	private void sendRequest(File toSave, File toSaveExtraction) {
		
		new Thread(new ExtractRunnable(toSave,toSaveExtraction)).start();
		
	}

}
