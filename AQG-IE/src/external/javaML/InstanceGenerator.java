package external.javaML;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import online.cluster.features.selection.ForClusterFeatureSelector;
import online.cluster.features.selection.impl.FrequencyBasedFeatureSelector;

import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.core.SparseInstance;
import net.sf.javaml.distance.CosineDistance;
import net.sf.javaml.featureselection.ranking.RecursiveFeatureEliminationSVM;
import net.sf.javaml.featureselection.scoring.GainRatio;
import net.sf.javaml.featureselection.subset.GreedyForwardSelection;

public class InstanceGenerator {

	private static final int KEEP = 1000;
	private Map<String, Integer> index;
	private Map<Integer,Map<Integer, Double>> ds;

	ForClusterFeatureSelector fcfs;
	
	public InstanceGenerator() {
		
		index = new HashMap<String, Integer>();
	
		ds = new HashMap<Integer, Map<Integer,Double>>();
		
		fcfs = new FrequencyBasedFeatureSelector(0.2,0.8);
		
	}
	
	public <T> void addInstance(Map<String, T> map, int classValue) {
		
		Map<Integer,Double> newInst = new HashMap<Integer, Double>();
		
		for (Entry<String,T> entry : map.entrySet()) {
			
			Integer ind = index.get(entry.getKey());
			
			if (ind == null){
				ind = index.size();
				index.put(entry.getKey(),ind);
			}
			
			newInst.put(ind, new Double(entry.getValue().toString()));
			
		}
		
		ds.put(classValue, newInst);

	}

	public Dataset getDataset() {
		
		Dataset ret = new DefaultDataset();
		
		for (Entry<Integer,Map<Integer,Double>> entry : ds.entrySet()) {
			
			ret.add(createInstance(entry.getValue(),index.size(),entry.getKey()));
			
		}
		
		if (ret.noAttributes() > KEEP && KEEP > 0){
		
//			RecursiveFeatureEliminationSVM svmrfe = new RecursiveFeatureEliminationSVM(0.2);
//			
//			svmrfe.build(ret);
//			
//			List<Integer> keep = new ArrayList<Integer>();
//			
//			for (int i = 0; i < svmrfe.noAttributes(); i++) {
//				
//				if (svmrfe.rank(i)<KEEP){
//					keep.add(i);
//				}
//				
//			}
			
			GreedyForwardSelection gfs = new GreedyForwardSelection(KEEP, new CosineDistance());
			
			gfs.build(ret);
			
			Set<Integer> keep = gfs.selectedAttributes();
			
			Dataset toret = new DefaultDataset();
			
			for (int i = 0; i < ret.size(); i++) {
				
				toret.add(createInstance(new ArrayList<Integer>(keep),ret.instance(i)));
				
			}
			
			return toret;

		}
		
		return ret;
	}

	private Instance createInstance(List<Integer> keep, Instance instance) {
		
		double[] values = new double[keep.size()];
		
		for (int i = 0; i < values.length; i++) {
			
			values[i] = instance.value(keep.get(i));
			
		}
		
		return new SparseInstance(values,instance.classValue());
		
	}

	private Instance createInstance(Map<Integer, Double> map, int size, int classValue) {
		
		double[] ret = new double[size];
		
		for (Entry<Integer,Double> entry : map.entrySet()) {
			
			ret[entry.getKey()] = entry.getValue();
			
		}
		
		return new SparseInstance(ret,Integer.valueOf(classValue));
		
	}

	

}
