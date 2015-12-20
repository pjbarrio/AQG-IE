package sample.AttributeTailoring.preprocessing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.Hashtable;

import utils.arff.myArffHandler;
import weka.core.Instances;

public class AttributeFrequencyGraphGenerator {

	private static final String HEAD_2 = "Frequency";
	private static final String HEAD_1 = "NumberOfOcurrences";

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		String database = "TheCelebrityCafe";
		
		String version = "";
		
		args = new String[2];
		
		args[0] = "/proj/db/NoBackup/pjbarrio/Experiments/workload/finalRun/arff/randomSample" +database+ "1stVersion" + version + ".arff";
		
		args[1] = "/proj/db/NoBackup/pjbarrio/Experiments/workload/finalRun/Distribution" + database + "1stVersion" + version + ".csv";
		
		Instances data = myArffHandler.loadInstances(args[0]);
		
		Hashtable<Integer, Integer> freqs = new Hashtable<Integer, Integer>();
		
		int val;
		
		int[] nC;
		
		Integer freq;
		
		for (int i=0;i<data.numAttributes();i++){
			
			System.out.println(i + " out of: " + data.numAttributes());
			
			nC = data.attributeStats(i).nominalCounts;
			
			if (nC.length>1) 
				val = data.numInstances() - nC[0];
			else 
				val = 0;
			
			freq = freqs.get(Integer.valueOf(val));
			
			if (freq==null){
				freq = new Integer(0);
			}
			
			freq++;
			
			freqs.put(val, freq);
		
		}
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(args[1])));

		bw.write(HEAD_1 + "," + HEAD_2 + "\n");
		
		Integer valule;
		
		for(Enumeration<Integer> e = freqs.keys();e.hasMoreElements();){
			
			valule = e.nextElement();
			
			freq = freqs.get(valule);
			
			bw.write(valule + "," + freq + "\n");
			
		}
		
		bw.close();
	}

}
