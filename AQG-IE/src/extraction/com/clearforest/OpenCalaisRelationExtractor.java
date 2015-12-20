package extraction.com.clearforest;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.rpc.ServiceException;

import org.apache.commons.io.FilenameUtils;

import utils.CommandLineExecutor;
import utils.FileHandlerUtils;
import utils.LynxCommandLineGenerator;

public class OpenCalaisRelationExtractor {

//	public static final String licenseID = "y2k3xz3rwfpzg2rzd764rm96";

	public static final String licenseID = "shf6uggkkp2au4jexwej9hbt";
	
	public static final long WAITING_TIME = 250;
	
	public static final long WAITING_CONCURRENT_TIME = 1730;

	public static final String SUFFIX = ".rdf";
	
	private String paramsXML;
	
	public OpenCalaisRelationExtractor(String string) {
		
		paramsXML = FileHandlerUtils.loadFileasString(string);
		
	}
	
	public void ConcurrentProcess(File file, File output){
		
		threadProcess(file,new String(output.getAbsolutePath()));
	
	}

	private void threadProcess(File fileName, String outputFile) {
		
		try {
			Thread.sleep(WAITING_CONCURRENT_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		ExtractorRunnable er = new ExtractorRunnable(fileName, outputFile, licenseID, paramsXML);
		
		new Thread(er).start();
			
	}

//	public String IEProcess(String fileName,String newName) {
//		
//		String storedFile = extractedIn.get(fileName);
//		
//		if (storedFile == null){
//		
//			System.out.println("Extracting: " + fileName);
//			
//			content = cle.getOutput(lclg.getLynxCommandLine(new File(fileName),newName));
//			
//			try {
//				Thread.sleep(WAITING_TIME);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			
//			result = "";
//			
//			try {
//				
//				System.out.println("About to call Web Service");
//				
//				result = new CalaisLocator().getcalaisSoap().enlighten(licenseID, content, paramsXML);
//			
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			} catch (ServiceException e) {
//				e.printStackTrace();
//			}
//			
//			String outputFile = "";
//			
//			synchronized (this) {
//				
//				outputFile = new String(PREFIX + NEXT_DOC_ID++ + SUFFIX);
//				
//				SaveNextDocId();
//				
//			}
//			
//			FileHandlerUtils.writeFile(result, outputFile);
//			
//			append(fileName, outputFile);
//			
//			extractedIn.put(fileName,outputFile);
//			
//
//		} else {
//			
//			result = FileHandlerUtils.loadFileasString(storedFile);
//			
//		}
//		
//		return result;
//
//	}
//	
//	public ArrayList<ArrayList<String>> extractTuples(String Relation) {
//		
//		ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
//				
//		String patternStr = "(<!--" + Relation + ":.*?; -->)"; 
//		// Compile and use regular expression 
//		Pattern pattern = Pattern.compile(patternStr); 
//		Matcher matcher = pattern.matcher(result); 
//		String groupStr;
//		while (matcher.find()) { 
//			// Get all groups for this match 
//			for (int i=1; i<=matcher.groupCount(); i++) { 
//				
//				groupStr = matcher.group(i); 
//				
//				ret.add(parseTuple(groupStr,Relation));
//				
//			} 
//			
//		} 		
//		
//		return ret;
//		
//	}
//	
//	public ArrayList<ArrayList<String>> extractTuples(String type, String content){
//		result = content;
//		return extractTuples(type);
//	}
//	
//	private ArrayList<String> parseTuple(String text,String type) {
//		
//		ArrayList<String> ret = new ArrayList<String>();
//		
//		String aux = text.replace("<!--" + type + ": ", "").replace(" -->", "").replace("; ", ";").replace(": ", ":").replaceAll("\\s+", " ");
//		
//		ArrayList<String> attributes = getAttributes(aux);
//		
//		
//		for (int i = 0; i < attributes.size(); i++) {
//			
//			ret.add(attributes.get(i));
//			
//		}
//		
//		return ret;
//		
//	}
//
//	private static ArrayList<String> getAttributes(String tuple) {
//		
//		ArrayList<String> ret = new ArrayList<String>();
//		
//		String aux = tuple;
//		
//		while (!aux.trim().equals("")){
//
//			int index = nextIndex(aux);
//			
//			ret.add(aux.substring(0, index));
//			
//			aux = aux.substring(index+1);
//			
//		}
//		
//		return ret;
//	}
//
//	private static int nextIndex(String aux) {
//		
//		int index = aux.indexOf(';');
//		
//		while (!isProper(index,aux)){
//			index = aux.substring(index+1).indexOf(';') + index + 1;
//		}
//		
//		return index;
//	}
//
//	private static boolean isProper(int index, String aux) {
//		
//		for (int i = index+1; i < aux.length(); i++) {
//			if (aux.charAt(i)==':'){
//				return true;
//			} else if (aux.charAt(i)==' '|| aux.charAt(i)==';'){
//				return false;
//			}
//		}
//		return true;
//	}
//	
//	public String extractMainContent() {
//		
//		String patternStr = "\\[CDATA\\[(.*?)\\]\\]"; 
//
////		String patternStr = "<body>(.*?)</body>";
//		String res = "";
//		// Compile and use regular expression 
//		Pattern pattern = Pattern.compile(patternStr); 
//		Matcher matcher = pattern.matcher(result); 
//		String groupStr = "";
//		boolean matchFound = matcher.find(); 
//		if (matchFound) { 
//			// Get all groups for this match 
//			for (int i=1; i<=matcher.groupCount(); i++) { 
//				groupStr = matcher.group(i); 
//				
//				res = res + " " + groupStr;
//				
//			} 
//			
//		} 
//		
//		return res;
//		
//		//return res.replaceAll("(<body>|</body>|</p>|<p>|\\W)", " ");
//	}
//	
//	private void append(String docHandle, String outputFile) {
//		
//		FileHandlerUtils.appendEntry(table,docHandle + TABLE_SEPARATOR + outputFile);
//		
//	}
//
//	public static String[] processLine(String line) {
//		
//		String[] pair = new String[2];
//			
//		int ind = line.lastIndexOf(TABLE_SEPARATOR);
//		
//		pair[0] = line.substring(0, ind);
//		pair[1] = line.substring(ind+1);
//		
//		return pair;
//	}

	
}
