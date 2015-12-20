package sample.generation.cluster;

import init.initialization.similarity.FileSelector;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.Cobweb;
import net.sf.javaml.clustering.FarthestFirst;
import net.sf.javaml.clustering.KMeans;

import org.apache.commons.io.FileUtils;

import sample.generation.cluster.impl.ClassificationBasedClusterGenerator;
import sample.generation.cluster.impl.JAVAMLClusterGenerationWrapper;
import utils.id.DatabaseCategoryHandler;
import utils.id.DatabaseIndexHandler;


public class ClusterGeneration {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String type = "allPaper";
		
		File file = new File("/proj/dbNoBackup/pjbarrio/Experiments/Wrappers/" + type);
		
		List<String> databases = FileUtils.readLines(file);
		
		File databaseIndex = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalDataIndex.txt");
		
		Map<Integer,String> indexTable = DatabaseIndexHandler.loadInvertedDatabaseIndex(databaseIndex);
				
		File outputFolder = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/SampleGeneration/Clusters/Category/" + type);
		
		File catFile = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/SampleGeneration/FilteredCategories/CategoryIndex.txt");
		
		ClusterGenerator cg = new ClassificationBasedClusterGenerator(catFile);
		
//		File prefix = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/WordsDistribution/");
		
//		FileSelector fs = new FileSelector(prefix, type);
		
//		ClusterGenerator cg = new JAVAMLClusterGenerationWrapper(new Cobweb(), FileSelector.RESULT_PAGE, fs, DatabaseIndexHandler.loadDatabaseIndex(databaseIndex));
		
		for (String string : databases) {
			
			Integer dbIndex = Integer.valueOf(string);
			
			String dbName = indexTable.get(dbIndex);
			
			cg.addDatabase(dbName);
			
		}
		
		Map<String,Set<String>> clusters = cg.getClusters();
				
		System.out.println(clusters);
		
		for (Entry<String,Set<String>> cluster : clusters.entrySet()) {
			
			File output = new File(outputFolder,cluster.getKey());

			FileUtils.writeLines(output, cluster.getValue());
			
		}
		
	}

	
	
}
