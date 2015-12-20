package sample.contentSummary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import contentsummary.generator.ContentSummaryGeneratorInterface;

import sample.generation.model.impl.DummySampleConfiguration;
import utils.arff.myArffHandler;
import utils.persistence.InteractionPersister;
import utils.persistence.databaseWriter;
import utils.persistence.persistentWriter;
import utils.persistence.impl.DiskBasedInteractionPersister;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffLoader.ArffReader;
import exploration.model.Sample;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import exploration.model.enumerations.VersionEnum;

public class ContentSummaryGenerator {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		//Generates a content summary with the frequency meaning the number of documents that contain the word.
		
		persistentWriter pW = new databaseWriter("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/");
		
		String[] websites = {"http://www.mauconline.net/","http://www.carmeuse.com","http://diversifiedproduct.com/","http://joehollywood.com/",
				"http://sociologically.net","http://northeasteden.blogspot.com/","http://www.paljorpublications.com/","http://www.brannan.co.uk/",
				"http://www.improv.ca/","http://www.avclub.com/","http://www.shopcell.com/","http://keep-racing.de","http://www.123aspx.com/",
				"http://www.infoaxon.com/","http://www.canf.org/","http://www.thecampussocialite.com/","http://www.jamesandjames.com","http://www.time.com/"};
		
		int sample_number = 1;
		
		int wload = 6;
		
		WorkloadModel wm = new WorkloadModel(wload,"/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/"+wload+"/Workload.ds","/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/"+wload+"/Workload.wl","/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/"+wload+"/WorkloadQueries","/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/"+wload+"/WorkloadRelations");
		
		Version v = Version.generateInstance(VersionEnum.INDEPENDENT.name(), wm);
				
		for (int i = 0; i < websites.length; i++) {
			
//			Sample sample = Sample.getSample(pW.getDatabaseByName(websites[i]), v, wm, sample_number,new DummySampleConfiguration(1));
			
			Sample sample = null;
			
			Instances instances = myArffHandler.loadInstances(pW.getArffRawModel(sample));
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(pW.getContentSummaryFile(sample))));
			
			for (int j = 0; j < instances.numAttributes(); j++) {
				
				if (j != instances.classIndex())
					bw.write(ContentSummaryGeneratorInterface.generateLine(instances.attribute(j).name(), sum(instances.attributeToDoubleArray(j))));
				
			}
			
			bw.close();
		}

	}

	private static long sum(double[] values) {
		
		long ret = 0;
		
		for (int i = 0; i < values.length; i++) {
			
			ret += values[i];
			
		}
		
		return ret;
	}

}
