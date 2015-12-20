package extraction.relationExtraction.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import execution.workload.tuple.Tuple;
import extraction.com.clearforest.OpenCalaisRelationExtractor;

public class RDFRelationExtractor {

	private static final String ATTRIBUTE_VALUE_SEPARATOR = ":";

	public List<Tuple> extract(String relation, String content) {
		
		ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
		
		String patternStr = "(<!--" + relation + ":(?:.|\\n)*?; -->)"; 
		// Compile and use regular expression 
		Pattern pattern = Pattern.compile(patternStr); 
		Matcher matcher = pattern.matcher(content); 
		String groupStr;
		while (matcher.find()) { 
			// Get all groups for this match 
			for (int i=1; i<=matcher.groupCount(); i++) { 
				
				groupStr = matcher.group(i); 
				
				ret.add(parseTuple(groupStr,relation));
				
			} 
			
		} 		
		
		return getTuples(ret);
		
	}

	private List<Tuple> getTuples(ArrayList<ArrayList<String>> tuples) {
		
		List<Tuple> ts = new ArrayList<Tuple>();
		
		for (ArrayList<String> arrayList : tuples) {
			
			Tuple t = new Tuple();
			
			for (String attributeValue : arrayList) {
				
				String[] attv = processAttributeValue(attributeValue);
				
				if (attv.length > 1)
				
					t.setTupleField(attv[0], attv[1]);
				
			}
		
			ts.add(t);
			
		}
		
		return ts;
		
	}

	public static String[] processAttributeValue(String attributeValue) {
		String[] pair = new String[2];
		int ind = attributeValue.indexOf(ATTRIBUTE_VALUE_SEPARATOR);
		
		if (ind == -1){
			System.out.println("AAAAAAAAAAAAAAAAAAAAAA: " + attributeValue);
			return new String[0];
		}
		
		pair[0] = attributeValue.substring(0,ind);
		pair[1] = attributeValue.substring(ind+1);
		
		return pair;
		
	}
	
	private ArrayList<String> parseTuple(String text,String type) {
		
		ArrayList<String> ret = new ArrayList<String>();
		
		String aux = text.replace("<!--" + type + ": ", "").replace(" -->", "").replace("; ", ";").replace(": ", ":").replaceAll("\\s+", " ");
		
		ArrayList<String> attributes = getAttributes(aux);
		
		
		for (int i = 0; i < attributes.size(); i++) {
			
			ret.add(attributes.get(i));
			
		}
		
		return ret;
		
	}

	private static ArrayList<String> getAttributes(String tuple) {
		
		ArrayList<String> ret = new ArrayList<String>();
		
		String aux = tuple;
		
		while (!aux.trim().equals("")){

			int index = nextIndex(aux);
			
			ret.add(aux.substring(0, index));
			
			aux = aux.substring(index+1);
			
		}
		
		return ret;
	}
	
	private static int nextIndex(String aux) {
		
		int index = aux.indexOf(';');
		
		while (!isProper(index,aux)){
			index = aux.substring(index+1).indexOf(';') + index + 1;
		}
		
		return index;
	}

	private static boolean isProper(int index, String aux) {
		
		for (int i = index+1; i < aux.length(); i++) {
			if (aux.charAt(i)==':'){
				return true;
			} else if (aux.charAt(i)==' '|| aux.charAt(i)==';'){
				return false;
			}
		}
		return true;
	}
	
	public static void main(String[] args) throws IOException {
		
		RDFRelationExtractor rdfRelationExtractor = new RDFRelationExtractor();
		
		List<Tuple> tuples = rdfRelationExtractor.extract("PersonCareer", 
				FileUtils.readFileToString(new File("/proj/db-files2/NoBackup/pjbarrio/Dataset/TREC/Extraction/tipster_vol_3/ap/ap901124-119.rdf")));
		
		for (int i = 0; i < tuples.size(); i++) {
			System.out.println(tuples.toString());
		}
		
	}
	
}
