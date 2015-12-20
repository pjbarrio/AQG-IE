package contentsummary.comparator;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import contentsummary.reader.ContentSummaryReader;

import exploration.model.Database;
import exploration.model.Sample;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import exploration.model.enumerations.SimilarityFunctionEnum;
import exploration.model.enumerations.VersionEnum;
import exploration.model.source.similarity.CosineSimilarity;
import exploration.model.source.similarity.SimilarityFunction;

import sample.generation.model.impl.DummySampleConfiguration;
import utils.persistence.databaseWriter;
import utils.persistence.persistentWriter;

import net.sf.javaml.core.Instance;
import net.sf.javaml.core.SparseInstance;
import net.sf.javaml.distance.AbstractSimilarity;

public class ContentSummaryComparator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		SimilarityFunctionEnum sfe = SimilarityFunctionEnum.COSINE_SIMILARITY;
		
		SimilarityFunction sf = SimilarityFunction.generateInstance(sfe.name());
		
		databaseWriter pW = new databaseWriter("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/");
		
		String[] websites = {"http://www.mauconline.net/","http://www.carmeuse.com","http://diversifiedproduct.com/","http://joehollywood.com/",
				"http://sociologically.net","http://northeasteden.blogspot.com/","http://www.paljorpublications.com/","http://www.brannan.co.uk/",
				"http://www.improv.ca/","http://www.avclub.com/","http://www.shopcell.com/","http://keep-racing.de","http://www.123aspx.com/",
				"http://www.infoaxon.com/","http://www.canf.org/","http://www.thecampussocialite.com/","http://www.jamesandjames.com","http://www.time.com/"};
		
		int sample_number = 1;
		
		int wload = 6;
		
		WorkloadModel wm = new WorkloadModel(wload,"/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/"+wload+"/Workload.ds","/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/"+wload+"/Workload.wl","/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/"+wload+"/WorkloadQueries","/proj/dbNoBackup/pjbarrio/Experiments/workload/finalRun/workload/"+wload+"/WorkloadRelations");
		
		Version v = Version.generateInstance(VersionEnum.INDEPENDENT.name(), wm);
				
		for (int i = 0; i < websites.length; i++) {
			
			Database db = pW.getDatabaseByName(websites[i]);
			
//			Sample sample = Sample.getSample(db, v, wm, sample_number,new DummySampleConfiguration(1));

			Sample sample = null;
			
			String csSample = pW.getContentSummaryFile(sample);

			ContentSummaryReader cr1 = new ContentSummaryReader(csSample);
			
			Map<String,Integer> map = loadMap(cr1);
			
			for (int j = 0; j < websites.length; j++) {
				
				Database db2 = pW.getDatabaseByName(websites[j]);
				
//				Sample sample2 = Sample.getSample(db2, v, wm, sample_number,new DummySampleConfiguration(1));
	
				Sample sample2 = null;
				
				String csSample2 = pW.getContentSummaryFile(sample2);
				
				ContentSummaryReader cr2 = new ContentSummaryReader(csSample2);
				
				Map<String,Integer> map2 = loadMap(cr2);

				double diff = new ContentSummaryComparator().execute(sf,map,map2);
				
				pW.saveSimilarity(db, db2, sfe, diff, v, sample_number, wm);
				
			}
			
		}
		
	}

	private double execute(SimilarityFunction sf, Map<String, Integer> map, Map<String, Integer> map2) {
		
		return sf.calculate(map, map2);
		
	}

	private static Map<String, Integer> loadMap(ContentSummaryReader cr1) {
		
		Map<String,Integer> map = new HashMap<String, Integer>();
		
		for(Enumeration<String> e = cr1.enumeration(); e.hasMoreElements();){
			
			String word = e.nextElement();
			
			map.put(word, (int)cr1.getFrequency(word));
			
		}
		
		return map;
		
	}

}
