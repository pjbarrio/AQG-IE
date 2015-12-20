package external.javaML;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.core.SparseInstance;

public class JavaMLInstanceCreator {

    private static Instance inst1;
	private static Instance inst2;

	public static <T> void createInstances(Map<String, T> wf1,
            Map<String, T> wf2) {
       
        Map<String,Integer> ind = new HashMap<String, Integer>();
       
        List<Double> i1 = new ArrayList<Double>();
        List<Double> i2 = new ArrayList<Double>();
       
        for (String word : wf1.keySet()) {
           
            i1.add(new Double(wf1.get(word).toString()));
           
            if (wf2.containsKey(word)){
                i2.add(new Double(wf2.get(word).toString()));
            }else{
                i2.add(0.0);
            }
           
            ind.put(word, ind.size());
                           
        }
       
        for (String word : wf2.keySet()) {
           
            if (!ind.containsKey(word)){
               
                i1.add(0.0);
               
                i2.add(new Double(wf2.get(word).toString()));
               
                ind.put(word, ind.size());
            }
           
        }
   
        inst1 = new DenseInstance(createDoubleArray(i1));
       
        inst2 = new SparseInstance(createDoubleArray(i2));
       
    }

	public static Instance getFirstInstance(){
		return inst1;
	}
	
	public static Instance getSecondInstance(){
		return inst2;
	}
	
    private static double[] createDoubleArray(List<Double> list) {
        
    	double[] ret = new double[list.size()];
    	
    	for (int i = 0; i < ret.length; i++) {
			ret[i] = list.get(i);
		}
     	 
    	return ret;
    	
    }
	
	
}
