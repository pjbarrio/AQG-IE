package execution.workload.impl.condition;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import execution.workload.impl.WorkLoad;
import execution.workload.impl.WorkLoadMatcher;
import execution.workload.impl.matchable.Matchable;
import execution.workload.impl.matchable.MatchableTuple;
import execution.workload.impl.matchable.StringMatchable;
import execution.workload.tuple.Tuple;
import execution.workload.tuple.TupleReader;


public class WorkLoadCondition extends UsefulCondition {

	private static final String STRINGMATCHABLE = "StringMatchable";

	private Hashtable<String,String> tableDescriptor;
	
	private String file;

	private WorkLoad wl;

	private int numberOfTuples;
	
	public WorkLoadCondition(String wORKLOADFILE, String description) {
		this.file = wORKLOADFILE;
		loadDescription(description);
		loadWorkLoad(this.file);
		
	}

	private void loadDescription(String description) {

		tableDescriptor = new Hashtable<String, String>();
		
		BufferedReader br;
		
		try {
			br = new BufferedReader(new FileReader(new File(description)));
			
			String line = br.readLine();
			
			while (line!=null){
				
				String[] spl = line.split(",");

				tableDescriptor.put(spl[0], spl[1]);
				
				line = br.readLine();
				
			}
			
			br.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	
		
	}

	private void loadWorkLoad(String workloadFile) {
		
		TupleReader tr = new TupleReader();
		Tuple[] tuplesArray = tr.readTuples(workloadFile);
		
		ArrayList<MatchableTuple> tuples = new ArrayList<MatchableTuple>();
		
		for (Tuple tuple : tuplesArray) {
			tuples.add(createMatchableTuple(tuple));
		}
		
		numberOfTuples = tuples.size();
		
		wl = new WorkLoad(tuples);
		
		
	}

	private MatchableTuple createMatchableTuple(Tuple tuple) {
		
		String[] fields = tuple.getFieldNames();
		
		MatchableTuple mct = new MatchableTuple();
		
		for (String field : fields) {
			
			Matchable mt = generateInstance(field);
			
			mt.setValueFromString(tuple.getFieldValue(field));

			mct.setTupleField(field, mt);
		}
		
		return mct;
		
	}

	private Matchable generateInstance(String field) {
		
		String descriptor = tableDescriptor.get(field);
		
		if (descriptor.equals(STRINGMATCHABLE)){
			return new StringMatchable();
		}
		
		return null;
		
	}

	@Override
	public boolean isItUseful(Tuple[] currTuples) {
		
		for (Tuple tuple : currTuples) {
			if (WorkLoadMatcher.Match(wl, tuple))
				return true;

		}
		
		return false;
	}

	@Override
	public boolean matchCondition(Tuple tuple) {
		
		return WorkLoadMatcher.Match(wl, tuple);
	
	}

	@Override
	public boolean isItUseless(Tuple[] currTuples) {
		
		if (currTuples == null || currTuples.length == 0)
			return false;
		
		for (Tuple tuple : currTuples) {
			if (WorkLoadMatcher.Match(wl, tuple))
				return false;
		}
		
		return true;
	}

	public boolean[] getMatchingArray(Tuple tuple) {
		
		return WorkLoadMatcher.getMatchingArray(wl,tuple);
		
	}

	public int getNumberOfTuples() {
		return numberOfTuples;
	}

	
	
}
