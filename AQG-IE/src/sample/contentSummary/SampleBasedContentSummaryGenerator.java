package sample.contentSummary;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sample.generation.model.impl.DummySampleConfiguration;
import utils.SVM.RFeaturesLoader.SVMFeaturesLoader;
import utils.persistence.PersistenceImplementation;
import utils.persistence.databaseWriter;
import utils.persistence.persistentWriter;
import contentsummary.generator.ContentSummaryGeneratorInterface;
import contentsummary.reader.ContentSummaryReader;
import exploration.model.Database;
import exploration.model.Sample;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import exploration.model.enumerations.VersionEnum;

public class SampleBasedContentSummaryGenerator {

	static databaseWriter pW;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		int[] workload = {1,2,3,4,5};
		
		int[] sample_number = {1,2,3,4,5};
		
//		String[][] database = {{"Business","Bloomberg"},{"Trip","TheCelebrityCafe"},{"Business","TheEconomist"},{"General","UsNews"},{"Trip","Variety"}};

//		String[][] Sampleddatabase = {{"Business","Bloomberg"},{"Trip","TheCelebrityCafe"},{"Business","TheEconomist"},{"General","UsNews"},{"Trip","Variety"},{"","GlobalSample"}};
		
		int cant = 700;
		
		pW = (databaseWriter)PersistenceImplementation.getWriter();
		
		List<Database> list = pW.getSearchableDatabases();
		
		List<Database> crossable = pW.getCrossableDatabases();
		
		for (int wl : workload) {
			
			WorkloadModel wlmodel = pW.getWorkloadModel(wl);
			
			for (int sn : sample_number) {
				
				for (Database db : list) {
					
//					String cs = "/local/pjbarrio/Files/Research-Dataset/LuceneSites/"+ database[i][0] +"-" + database[i][1] + "-cs.txt";
					
					String cs = pW.getContentSummaryFile(db);
					
					ContentSummaryReader csr = new ContentSummaryReader(cs);
					
					for (VersionEnum version : VersionEnum.values()){
						
						Version v = Version.generateInstance(version.name(), wlmodel);
						
						for (Database dbcross : crossable) {
							
//							System.out.println(database[i][1] + " - " + version[j] + " - " + Sampleddatabase[k][1]);
							
//							String SMO = "/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/WekaOutput/SMO"+Sampleddatabase[k][1]+"1stVersion" + version[j];
							
//							Sample sample = Sample.getSample(dbcross, v, wlmodel, sn,new DummySampleConfiguration(1));
							
							Sample sample = null;
							
							String SMO = pW.getSMOWekaOutput(sample,null);
							
							SVMFeaturesLoader.loadFeatures(new File(SMO),null);
							
							ArrayList<String> feats = SVMFeaturesLoader.getFeatures();
							
							String newCS = pW.getBasedOnContentSummary(sample,db);
							
//							String newCS = "/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/samplebasedcs/" + database[i][1] + "-basedOn-" + Sampleddatabase[k][1] + "1stVersion" + version[j] + "-cs.txt"; 
							
							GenerateSampleBasedContentSummary(csr,feats,newCS,cant);
							
							System.out.println(newCS);
							
						}
						
					}
					
				}
				
			}
		}

		

		
	}

	private static void GenerateSampleBasedContentSummary(
			ContentSummaryReader csr, ArrayList<String> feats, String newCS, int cant) throws IOException {
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(newCS)));
		
		String feat;
		
		long freq;
		
		for (int i = 0; i < cant; i++) {
			
			feat = feats.get(i);
			
			freq = csr.getFrequency(feat);
			
			bw.write(ContentSummaryGeneratorInterface.generateLine(feat, freq));
			
		}
		
		bw.close();
	}

}
