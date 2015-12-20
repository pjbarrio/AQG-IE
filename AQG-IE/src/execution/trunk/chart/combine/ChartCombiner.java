package execution.trunk.chart.combine;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.gdata.util.common.base.Pair;

public class ChartCombiner {

	private static final String ADDED_BY_PROCESSED = "GenerationAddedDocumentByProcessedDocuments.R";
	
	private static final String ADDED_BY_PROCESSED_NORMALIZED = "GenerationAddedDocumentByProcessedDocumentsNormalized.R";
	
	private static final String EX_ADDED_BY_PROCESSED = "ExecutionIndependentAddedDocumentByProcessedDocuments.R";
	
	private static final String EX_ADDED_BY_PROCESSED_NORMALIZED = "ExecutionIndependentAddedDocumentByProcessedDocumentsNormalized.R";;
	
	private static final String ADDED_BY_ISSUED = "GenerationAddedDocumentsBySentQueries.R";
	
	private static final String EX_ADDED_BY_ISSUED = "ExecutionIndependentAddedDocumentsBySentQueries.R";

	private static final String EX_PROCESSED_BY_ADDED = "ExecutionProcessedDocumentsByAddedDocuments.R";
	
	private static final String EX_ISSUED_BY_ADDED = "ExecutionSentQueriesByAddedDocuments.R";
	
	private static final double MAX_DOC = 2000.0;
	
	private static final double MAX_DOC_NORMALIZED = 1000.0;
	
	private static final double MAX_QUE = 500.0;
	
	private static double MAX_ADDED = 80;
	
	private static final double DOCS_INC = 5.0;
	
	private static final double DOCS_INC_NORMALIZED = 0.001;
	
	private static final double QUE_INC = 2.0;
	
	private static final String TECHNIQUES_SIMPLE = "techniques_simple";
	
	private static final String TUPLES_SIMPLE = "tuples_simple";
	
	private static final String RELATIONS_SIMPLE = "relations_simple";
	
	private static final String QDSPACES_SIMPLE = "qdspaces";
	
	private static final String USELESS_SIMPLE = "useless_simple";
		
	private static final String AFFINITY = "affinity";
	
	private static final String QDAFFINITY = "qdaffinity";
	
	private static final String PREFIX = "/local/pjbarrio/Files/Projects/AutomaticQueryGeneration/Writing/forCharts/";
	
	private static final String PREFIX_OUT = "/local/pjbarrio/Files/Projects/AutomaticQueryGeneration/Writing/forChartsCombined/";

	private static final String QDSPACES_SAMPLE = "qdspacessample";

	private static final String QDAFFINITY_SAMPLE = "qdaffinitysample";

	private static final String EX_SAMPLE_BY_ADDED = "ExecutionSamplesByAddedDocument.R";

	private static Object RELATIONS = "relations";

	private static Object LIMITS = "limit";

	private static String[] RELATIONS_ARRAY = {"_PC","_ND","_MMD","_IAT","_VR"};

	private static String[] LIMITS_ARRAY = {"_10","_20","_30","_40","_50"};


	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		System.setOut(new PrintStream(new File(new File(PREFIX_OUT),"out.txt")));
		
		int intervals = 10;
/*
		generate("",TECHNIQUES_SIMPLE,ADDED_BY_PROCESSED,intervals,DOCS_INC,MAX_DOC, true, 1,false,-1);
		generate("Q",TECHNIQUES_SIMPLE,ADDED_BY_ISSUED,intervals,QUE_INC,MAX_QUE, true, 1,false,-1);
		
		
		generate("",TUPLES_SIMPLE,ADDED_BY_PROCESSED,intervals,DOCS_INC,MAX_DOC, true, 1 ,false,-1);
		generate("Q",TUPLES_SIMPLE,ADDED_BY_ISSUED,intervals,QUE_INC,MAX_QUE, true, 1 ,false,-1);

		
		generate("",RELATIONS_SIMPLE,ADDED_BY_PROCESSED,intervals,DOCS_INC,MAX_DOC, false,2,true,10);
		generate("Q",RELATIONS_SIMPLE,ADDED_BY_ISSUED,intervals,QUE_INC,MAX_QUE, false,2,true,10);
		
		
		generate("",USELESS_SIMPLE,ADDED_BY_PROCESSED,intervals,DOCS_INC,MAX_DOC,false,1,false,-1);
		generate("Q",USELESS_SIMPLE,ADDED_BY_ISSUED,intervals,QUE_INC,MAX_QUE,false,1,false,-1);
*/			
		//TASW for PC, add.
		
/*		generate("",QDSPACES_SIMPLE,EX_PROCESSED_BY_ADDED,intervals,DOCS_INC,MAX_ADDED,true,2,true,10);
		generate("Q",QDSPACES_SIMPLE,EX_ISSUED_BY_ADDED,intervals,DOCS_INC,MAX_ADDED,true,2,true,10);
*/		
		//affinity
/*		
		generate("",AFFINITY,ADDED_BY_PROCESSED_NORMALIZED,intervals,DOCS_INC,MAX_DOC_NORMALIZED,true,4,true,12);
		generate("Q",AFFINITY,ADDED_BY_ISSUED,intervals,DOCS_INC,MAX_DOC,true,4,true,12);
		
		//QD affinity
*/		
		generate("",QDAFFINITY,EX_PROCESSED_BY_ADDED,intervals,DOCS_INC,MAX_ADDED,true,1,true,12);
		generate("Q",QDAFFINITY,EX_ISSUED_BY_ADDED,intervals,DOCS_INC,MAX_ADDED,true,4,true,12);
		
	/*
		generate("",QDAFFINITY_SAMPLE,EX_SAMPLE_BY_ADDED,intervals,DOCS_INC,MAX_ADDED,true,1,true,8);

		
		generate("",QDSPACES_SAMPLE,EX_SAMPLE_BY_ADDED,intervals,DOCS_INC,MAX_ADDED,true,1,true,5);
		
		*/		
		
	}

	private static void generate(String prefix, String techniquesSimple, String addedBy, int intervals, double inc, double max, boolean inOrder, int every, boolean group, int numb) throws IOException {
		
		System.out.println("\n\n" + techniquesSimple + " - " + prefix + "\n\n");
		
		List<String> lines = FileUtils.readLines(new File(PREFIX + techniquesSimple));
		int separatedby = Integer.valueOf(lines.remove(0));
		
		String[] ls = lines.remove(0).split(",");
		
		int fixedPosition = Integer.valueOf(ls[0]);
		
		int[] rest = new int[ls.length - 1];
		
		for (int i = 0; i < rest.length; i++) {
			rest[i] = Integer.valueOf(ls[i+1]);
		}
		
		String[] orderByArr = lines.remove(0).split(",");
		String orderby = orderByArr[0];
		String[] after = {""};
		if (orderByArr.length > 1){
			after = Arrays.copyOfRange(orderByArr, 1, orderByArr.length);
		}
		
		String[] prefixes = generatePrefixes(orderby,after); 
		
		List<Pair<String,List<Double>>> list = new ArrayList<Pair<String,List<Double>>>();
		
		double fixed = -0.0;
		
		if (inOrder){ 		
		
			for (int i = 0; i < prefixes.length; i++) {
				
				double fixedAux = read(prefix,lines.get(i),fixedPosition,list,prefixes[i],addedBy,rest);
				
				if (fixedAux > fixed)
					fixed = fixedAux;
				
			}

		} else{ //Alternate
			
			List<List<Pair<String,List<Double>>>> bigAux = new ArrayList<List<Pair<String,List<Double>>>>();
			
			int total = 0;
			
			for (int i = 0; i < prefixes.length; i++) {
				
				List<Pair<String,List<Double>>> listAux = new ArrayList<Pair<String,List<Double>>>();
				
				double fixedAux = read(prefix, lines.get(i),fixedPosition,listAux,prefixes[i],addedBy,rest);
				
				if (fixedAux > fixed)
					fixed = fixedAux;
				
				total = listAux.size(); //All have the same size ... :)
				
				bigAux.add(listAux);
				
			}
		
			//Proceed to Alternate
			
			for (int i = 0; i < total; i+=every) {
				
				for (int j = 0; j < bigAux.size(); j++) {
				
					for (int k = 0; k < every; k++) {
						
						list.add(bigAux.get(j).get(i+k));
						
					}
		
				}
				
			}
			
		}
		
		int pass = fixedPosition-2;
		
		if (inOrder && every > 1){
			pass*=every;
		}
		
		List<Pair<String,List<Double>>> reduced = reduceList(list,intervals,inc,separatedby,pass,max,group,numb);
		
		reduced.add(generateFixed(fixed,intervals,separatedby));
		
		reduced.add(generateFixed2(fixed,intervals));
		
		for (int i = 0; i < reduced.size(); i++) {
			
			System.out.println(reduced.get(i).getFirst() + "<-c(" + reduced.get(i).getSecond().toString().replace("[", "").replace("]", "").replace("NaN", "NA").replace(" ","") + ")");
			
		}
		
		
	}

	private static Pair<String,List<Double>> generateFixed2(double fixed, int intervals) {
		
		return new Pair<String,List<Double>>("Fixed2",generateListFixed2(fixed,intervals));
		
	}

	private static List<Double> generateListFixed2(double fixed, int intervals) {
		
		List<Double> ret = new ArrayList<Double>(intervals+1);
		
		for (int i = 0; i < intervals+1; i++) {
			
			ret.add(fixed);
			
		}
		
		return ret;
		
	}

	private static Pair<String,List<Double>> generateFixed(double fixed, int intervals,
			int separatedby) {
		
		return new Pair<String,List<Double>>("Fixed",generateListFixed(fixed,intervals,separatedby));
		
	}

	private static List<Double> generateListFixed(double fixed, int intervals,
			int separatedby) {
		
		List<Double> ret = new ArrayList<Double>(intervals*(separatedby+1) + 1);
		
		for (int i = 0; i < intervals*(separatedby+1) + 1; i++) {
			ret.add(fixed);
		}
		
		return ret;
	}

	private static List<Pair<String, List<Double>>> reduceList(
			List<Pair<String, List<Double>>> list, int intervals, double inc, int separatedby,int inEach, double max, boolean group,int numb) {

		int[] indexes = null;
		
		if (!group){		
			indexes = findIndexes(list,max,intervals,inc);
		}
		
		List<Pair<String, List<Double>>> ret = new ArrayList<Pair<String,List<Double>>>(list.size());
		
		int counter = 0;
		
		int pre = -1;
		
		int grNumb = -1;
		
		for (int i = 0; i < list.size(); i++) {
			
			if (counter % inEach == 0)
				pre++;
			
			if (counter % numb == 0){
				
				if (group){
					indexes = findIndexes(list.subList(i, i+numb),max,intervals,inc);
				}
				
				grNumb++;
			}
			
			ret.add(reduceList(list.get(i),indexes,separatedby,pre,group,grNumb));
			
			counter++;
			
			
		}
		
		return ret;
		
	}

	private static int[] findIndexes(List<Pair<String, List<Double>>> list, double max, int intervals,double inc) {
		
		int maxSize = -1;
		
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getSecond().size() > maxSize){
				maxSize = list.get(i).getSecond().size();
			}
		}
		
		int[] indexes = findIndex(Math.min(maxSize, max),intervals);
			
		System.out.println("");
		
		for (int i = 0; i < indexes.length; i++) {
			System.out.print("\"" + ((indexes[i]+1) * inc) + "\",");
		}
		
		System.out.println("");
		
		return indexes;
	}

	private static Pair<String, List<Double>> reduceList(
			Pair<String, List<Double>> pair, int[] indexes, int separateby, int pre, boolean group, int grNumb) {
		
		return new Pair<String, List<Double>>(pair.getFirst(), reduceList(pair.getSecond(),indexes,separateby,pre,group,grNumb));
		
	}

	private static List<Double> reduceList(List<Double> second, int[] indexes,int separatedby, int pre, boolean group, int grNumb) {
		
		boolean hasUsedLast = false;
		
		List<Double> ret = new ArrayList<Double>();
		
		if (!group){
		
			for (int i = 0; i < indexes.length && !hasUsedLast; i++) {
			
				//first
				ret.add(Double.NaN);
				
				for (int h = 0; h < pre; h++) {
					
					ret.add(Double.NaN);
					
				}
				
				if (indexes[i] >= second.size()-1){
					hasUsedLast = true;
					ret.add(second.get(second.size()-1));
				}else{
					ret.add(second.get(indexes[i]));
				}
				
				for (int j = 0; j < separatedby - pre-1; j++) {
					ret.add(Double.NaN);
				}
				
			}

		} else {
			
			for (int i = 0; i < separatedby; i++) {
				
				ret.add(Double.NaN);
				
//				ret.add(Double.NaN); Another one for the label in y?
				
				if (grNumb == i){
					//add values
					hasUsedLast = false;
					
					for (int j = 0; j < indexes.length; j++) {
						if (!hasUsedLast && indexes[j] >= second.size()-1){
							hasUsedLast = true;
							if (!second.isEmpty()){
								ret.add(second.get(second.size()-1));
							}{
								ret.add(Double.NaN);
							}
						}else if (hasUsedLast && indexes[j] >= second.size()-1){
							ret.add(Double.NaN);
						} else{
							ret.add(second.get(indexes[j]));
						}
					}
					
				}else{
					for (int j = 0; j < indexes.length; j++) {
						ret.add(Double.NaN);
					}
				}
				
			}
			
		}
		//add the end
		
		ret.add(Double.NaN);
		
		return ret;
		
	}

	private static int[] findIndex(double maxSize, double intervals) {
		
		double inc = maxSize / intervals;
		
		int[] ret = new int[(int)intervals];
		
		double current = 0.0;
		
		int index = 0;
		
		while (current < maxSize && index < intervals){
			
			current += inc;
			
			ret[index] = (int)Math.round(current) - 1;
			
			index++;
			
		}
		
		return ret;
	}

	private static double read(String prefixName, String file, int fixedPosition,
			List<Pair<String, List<Double>>> list, String prefix, String addedBy, int[] rest) throws IOException {
		
		List<String> lines = FileUtils.readLines(new File(file + addedBy));
		
		int restCurrentIndex = 0;
		
		for (int i = 1; i < fixedPosition-1; i++) { //skip first line
			
			if (rest.length > 0 ){
				if (restCurrentIndex < rest.length && rest[restCurrentIndex] == i){
					restCurrentIndex++;
					list.add(generatePair(prefixName, lines.get(i),prefix));
				}
			}else{
				list.add(generatePair(prefixName, lines.get(i),prefix));
			}
		}
		
		return recoverFixedValue(lines.get(fixedPosition-1));
	}

	private static Pair<String, List<Double>> generatePair(String prefixName, String string, String prefix) {
		
		String name = prefixName + string.substring(0, string.indexOf('<')).trim() + prefix;
		
		String[] values = string.substring(string.indexOf('(')+1,string.indexOf(')')).split(",");
		
		List<Double> vals = new ArrayList<Double>(values.length);
		
		for (int i = 1; i < values.length; i++) { //To skip 0
			vals.add(Double.valueOf(values[i]));
		}
		
		return new Pair<String, List<Double>>(name, vals);
		
	}

	private static double recoverFixedValue(String string) {
		
		return Double.valueOf(string.substring(string.indexOf('(')+1).split(",")[0]);
		
	}

	private static String[] generatePrefixes(String orderby, String[] after) {
		
		String[] prefixesAux = getInitialPrefixes(orderby);
		
		String[] ret = new String[prefixesAux.length * after.length];
		
		int index = 0;
		
		for (int i = 0; i < prefixesAux.length; i++) {
			for (int j = 0; j < after.length; j++) {
				ret[index] = prefixesAux[i] + after[j];
				index++;
			}
			
		}
		
		return ret;
	}

	private static String[] getInitialPrefixes(String orderby) {
		
		if (orderby.equals(RELATIONS)){
			return RELATIONS_ARRAY;
		}else if (orderby.equals(LIMITS)){
			return LIMITS_ARRAY;
		}
	
		return null;
		
	}

}
