package sample.contentSummary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import sample.generation.model.impl.DummySampleConfiguration;
import utils.persistence.PersistenceImplementation;
import utils.persistence.databaseWriter;

import contentsummary.reader.ContentSummaryReader;
import exploration.model.Database;
import exploration.model.Sample;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import exploration.model.enumerations.SimilarityFunctionEnum;
import exploration.model.enumerations.VersionEnum;
import exploration.model.source.similarity.SimilarityFunction;


public class BasedContentSummaryComparator {

	private static BufferedWriter bw;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
//		String[] summarizeddatabase = {"Bloomberg","TheCelebrityCafe","TheEconomist","UsNews","Variety"};
		
//		String[] Sampleddatabase = {"Bloomberg","TheCelebrityCafe","TheEconomist","UsNews","Variety"};

		int[] sample_number = {1,2,3,4,5};
		
		int[] workload = {1,2,3,4,5};
				
		databaseWriter pW = (databaseWriter)PersistenceImplementation.getWriter();
		
		List<Database> summarizeddatabases = pW.getSearchableDatabases();
		
		List<Database> sampledDatabases = pW.getSearchableDatabases();
				
		for (int wl : workload) {
			
			WorkloadModel wlmodel = pW.getWorkloadModel(wl);
			
			for (VersionEnum ver : VersionEnum.values()) {
				
//				initializeMainStructure(version[i],summarizeddatabase);
				
				Version version = Version.generateInstance(ver.name(), wlmodel);
				
				for (int sn : sample_number) {
				
					for (Database sampledDatabase : sampledDatabases) {
						
	//					initializeStructure(Sampleddatabase[j],version[i]);
	
						Sample sample = null;
						
//						Sample sample = Sample.getSample(sampledDatabase, version, wlmodel, sn,new DummySampleConfiguration(1));
						
						String CS = pW.getBasedOnContentSummary(sample, sampledDatabase);
						
//						String CS = "/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/samplebasedcs/" + Sampleddatabase[j] + "-basedOn-" + Sampleddatabase[j] + "1stVersion" + version[i] + "-cs.txt";
						
						double[] cs = loadContentSummary(CS);
						
						for (Database summarizedDatabase : summarizeddatabases) {
							
							if (summarizedDatabase.equals(sampledDatabase)) continue;
							
//							String compCS = "/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/samplebasedcs/" + summarizeddatabase[k] + "-basedOn-" + Sampleddatabase[j] + "1stVersion" + version[i] + "-cs.txt";
							
//							Sample compSample = Sample.getSample(summarizedDatabase, version, wlmodel, sn,new DummySampleConfiguration(1));
							
							Sample compSample = null;
							
							String compCS = pW.getBasedOnContentSummary(compSample, sampledDatabase);
							
							double distance = CalculateDistance(cs,loadContentSummary(compCS));
							
//							System.out.println(summarizeddatabase[k] + " -BasedOn- " + Sampleddatabase[j] + " - " + version[i] + " - " + distance);
							
							pW.saveSimilarity(sampledDatabase,summarizedDatabase,SimilarityFunctionEnum.COSINE_SIMILARITY,distance,version,sn,wlmodel);
							
//							saveDistance(summarizeddatabase[k],distance);
							
						}
										
					}
					
					
				}
				
			}
		}
		
	}


	private static double CalculateDistance(double[] vector1,
			double[] vector2) {

		double dp = getDotProduct(vector1,vector2);
		
		double m1 = getModule(vector1);
		
		double m2 = getModule(vector2);
		
		return dp/(m1*m2);
		
	}

	private static double getDotProduct(double[] vector1, double[] vector2) {
		
		double ret = 0;
		
		for (int i = 0; i < vector2.length; i++) {
		
			ret = ret + vector1[i]*vector2[i];
			
		}
		
		return ret;
	}

	private static double getModule(double[] vector) {
		
		double dp = getDotProduct(vector, vector);
		
		double sqrt = Math.sqrt(dp);
		
		return sqrt;
	
	}

	private static double[] loadContentSummary(String cS) {
		
		ContentSummaryReader csr = new ContentSummaryReader(cS);
		
		double[] ret = new double[(int)csr.size()];
		
		int i = 0;
		
		for (Enumeration<String> e = csr.enumeration(); e.hasMoreElements();){
			
			ret[i] = csr.getFrequency(e.nextElement());
			
			i++;
			
		}
		
		return ret;
		
	}

	private static void initializeStructure(String Generatordatabase, String version) throws IOException {
		
		bw.write("\n" + Generatordatabase);
		
	}

}
