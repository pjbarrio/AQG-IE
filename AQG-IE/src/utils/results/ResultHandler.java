package utils.results;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import utils.id.TuplesLoader;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import exploration.model.Execution;

public class ResultHandler {

	private static final String VALUES_SEPARATOR = ",";
	private BufferedReader br;
	private boolean availableResults = false;
	private String query;
	private String tp;
	private String fp;
	private String rIds;
	private ArrayList<Long> TPs;
	private ArrayList<Long> FPs;
	private ArrayList<Long> All;
	private int nextEvaluation;
	private Execution execution;
	private Hashtable<Long, ArrayList<String>> docTuples;
	private persistentWriter pW;
	

	public ResultHandler(Execution execution, persistentWriter pW) throws IOException {
		
		this.pW = pW;
		
		this.execution = execution;
		
		nextEvaluation = 1;
		
//XXX class not used anymore		docTuples = TuplesLoader.loadIdtuplesTable(pW.getMatchingTuplesWithSourcesFile(execution.getEvaluations().get(0).getEvaluableDatabase().getName(), 
//				execution.getEvaluations().get(0).getCombination().getVersion().getName(),execution.getEvaluations().get(0).getCombination().getWorkload()));
		
		start();
		
	}

	private void start() throws IOException {
		
		TPs = new ArrayList<Long>();
		FPs = new ArrayList<Long>();
		All = new ArrayList<Long>();
		
		br = new BufferedReader(new FileReader(new File(pW.getOutputFile(execution,nextEvaluation))));
		
		checkAvailability();
		
	}

	private boolean ReadNext() throws IOException {
		
		if (!br.ready())
			return false;
		
		query = br.readLine();
		
		if (query == null){
			br.close();
			return false;
		}
		
		while ("".equals(query))
			query = br.readLine();
		
		tp = br.readLine();
		
		if (tp == null){
			br.close();
			return false;
		}
		
		while ("".equals(tp)){
			tp = br.readLine();
		}
		
		fp = br.readLine();
		
		if (fp == null){
			br.close();
			return false;
		}
		
		while ("".equals(fp)){
			fp = br.readLine();
		}
			
		rIds = br.readLine();
		
		if (br == null){
			br.close();
			return false;
		}
				
		return true;
	
	}

	public ArrayList<Long> processList(String documents){
		
		ArrayList<Long> ret = new ArrayList<Long>();
		
		if ("".equals(documents.trim())){
			return ret;
		}
		
		
		
		String[] docs =  documents.trim().split(VALUES_SEPARATOR);

		for (int i = 0; i < docs.length; i++) {
			
			ret.add(Long.valueOf(docs[i]));
			
		}
		
		return ret;
		
	}
	
	public boolean hasMoreResults() throws IOException {
		
		checkAvailability();
		
		return availableResults;
	
	}

	private void checkAvailability() throws FileNotFoundException, IOException {
		
		while (!ReadNext() && nextEvaluation <= execution.getEvaluations().size()){
			
			nextEvaluation++;
			
//XXX class not used anymore			docTuples = TuplesLoader.loadIdtuplesTable(PersistenceImplementation.getWriter().getMatchingTuplesWithSourcesFile(execution.getEvaluations().get(nextEvaluation).getEvaluableDatabase().getName(), 
//					execution.getEvaluations().get(nextEvaluation).getCombination().getVersion().getName(),execution.getEvaluations().get(nextEvaluation).getCombination().getWorkload()));
			
			br = new BufferedReader(new FileReader(new File(pW.getOutputFile(execution,nextEvaluation))));
		
		}

		availableResults = (nextEvaluation <= execution.getEvaluations().size());
		
		
		
	}

	public ArrayList<Long> getNextNoProcessedDocs(){

		return All;
	
	}
	
	public ArrayList<Long> getNextTPNoProcessedDocs(){
		
		return TPs;
	
	}
	
	public ArrayList<Long> getNextFPNoProcessedDocs(int limit){
		
		return FPs;

	}
	
	public String getNextQuery() {
	
		return query;
	
	}

	public long getNextTruePositivesSize() {
		
		return getSize(tp);
	}

	private long getSize(String list) {
		if (list.trim().equals("")){
			return 0;
		}
		return list.split(VALUES_SEPARATOR).length;
	}

	public long getNextFalsePositivesSize() {

		return getSize(fp);

	}

	public long getNextResultListSize(){
		return getSize(rIds);
	}
	
	public static <E> String generateSequence(ArrayList<E> set) {
		
		String ret = "";
		
		for (E number : set) {
			
			ret = ret + number + ",";
			
		}
		
		if (set.isEmpty())
			return "";
		
		return ret.substring(0,ret.length()-1);
		
	}

	public static String generateTriple(double time, String query, String tpString,
			String fpString, String resIds) {

		return "(" + time + ") --> " + query + "\n" + tpString + "\n" + fpString + "\n" + resIds + "\n";
		
	}

	public void closeStream(){
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<String> getTuples(Long doc) {
		
		return docTuples.get(doc);
	
	}

}
