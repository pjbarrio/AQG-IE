package init.initialization.similarity;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import online.sample.wordsDistribution.WordsDistributionLoader;
import utils.persistence.databaseWriter;
import exploration.model.Database;
import exploration.model.dummy.DummyDatabase;
import exploration.model.enumerations.SimilarityFunctionEnum;
import exploration.model.source.similarity.SimilarityFunction;

public class SimilarityCalculator {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		databaseWriter dW = new databaseWriter("");
		
		SimilarityFunctionEnum se = SimilarityFunctionEnum.COS_SIMILARITY_DEEP_RES;
		
		int level = FileSelector.DEEP_RESULT_PAGE;
		
		SimilarityFunction sf = SimilarityFunction.generateInstance(se.name());
		
		WordsDistributionLoader wdl = new WordsDistributionLoader();
		
		String type = "smalltraining";
		
		File files = new File("/proj/dbNoBackup/pjbarrio/Experiments/Wrappers/" + type);
		
		File prefix = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/WordsDistribution/");
		
		List<String> toProcess = FileUtils.readLines(files);
		
		FileSelector fs = new FileSelector(prefix,type);
		
		for (int i = 0; i < toProcess.size(); i++) {
			
			Integer db1 = Integer.valueOf(toProcess.get(i));
			
			Database d1 = dW.getDatabaseByName(db1.toString());
			
			File f1 = fs.getWordFrequency(db1,level);
			
			Map<String,Long> wf1;
			if (f1 == null || !f1.exists()) 
				wf1 = new HashMap<String, Long>();
			else{
				wf1 = wdl.loadFile(f1);
			}
			for (int j = 0; j < toProcess.size(); j++) {
				
				Integer db2 = Integer.valueOf(toProcess.get(j));
				
				Database d2 = dW.getDatabaseByName(db2.toString());
				
				File f2 = fs.getWordFrequency(db2,level);
				
				Map<String,Long> wf2;
				
				if (f2 == null || !f2.exists())
					wf2 = new HashMap<String, Long>();
				else
					wf2 = wdl.loadFile(f2);
				
				double diff = sf.calculate(wf1,wf2);
				
				if (new Double(diff).equals(Double.NaN)){
					diff=0;
				}
				
				System.out.println(db1 + "," + db2 +"," + diff);
				
				dW.saveGlobalSimilarity(d1, d2, se, diff);
				
			}
			
		}

	}

}
