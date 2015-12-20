package execution.workload.impl;

import java.util.ArrayList;

import execution.workload.impl.matchable.Matchable;
import execution.workload.impl.matchable.MatchableTuple;
import execution.workload.tuple.Tuple;


public class WorkLoadMatcher {

	private static MatchableTuple wlT;
	private static String[] fields;
	private static Matchable matchable;
	private static String tvalue;

	public static boolean Match(WorkLoad wl, Tuple t){
		
		wl.start();
		
		while (wl.hasNext()){
			wlT = wl.next();
			if (Match(wlT,t)){
				return true;
			}
		}
		
		return false;
	}

	private static boolean Match(MatchableTuple wlT, Tuple t) {
	
		fields = wlT.getFieldNames();
	
		boolean matchingall = true;
		
		for (String string : fields) {
			
			 matchable = wlT.getMatchableFieldValue(string);
			 
			 tvalue = t.getFieldValue(string);
			 
			 if (tvalue!=null){
				 if (!matchable.match(tvalue))
					 matchingall = false;
			 } else {
				 matchingall = false;
			 }
			
		}
		
		return matchingall;
		
	}

	public static boolean[] getMatchingArray(WorkLoad wl, Tuple tuple) {
		
		ArrayList<Boolean> arr = new ArrayList<Boolean>();
		
		wl.start();
		
		while (wl.hasNext()){
			MatchableTuple wlT = wl.next();
			if (Match(wlT,tuple)){
				arr.add(true);
			}else {
				arr.add(false);
			}
		}
		
		boolean[] b = new boolean[arr.size()];
		
		for (int i = 0; i < arr.size(); i++) {
			
			Boolean bool = arr.get(i);
			
			b[i] = bool.booleanValue();
			
		}
		
		return b;
	}
	
}
