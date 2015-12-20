package sample.generation.catching.tuples;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import online.documentHandler.OnlineDocumentHandler;
import online.documentHandler.navigation.impl.ClusterHeuristicNavigationHandler;
import online.navigation.utils.NavigationUtils;
import online.queryResultPageHandler.impl.TreeEditDistanceBasedWrapper;
import online.resultHandler.impl.AllHrefResultDocumentHandler;
import online.sample.wordsDistribution.WordsDistributionLoader;
import online.tagcleaner.HTMLTagCleaner;
import online.tagcleaner.impl.HTMLCleanerBasedCleaner;
import searcher.Searcher;
import searcher.impl.OnLineSearcher;
import searcher.interaction.formHandler.TextQuery;
import utils.persistence.InteractionPersister;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import utils.persistence.impl.DiskBasedInteractionPersister;
import utils.word.extraction.WordValidator;
import exploration.model.Database;
import exploration.model.enumerations.ExperimentStatusEnum;

public class InsertQueriesForTuples {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		persistentWriter pW = PersistenceImplementation.getWriter();

		List<Database> databases = pW.getSamplableDatabases(88);

		int max_number_of_queries = 50;

		Set<String> stopWords = new HashSet<String>(FileUtils.readLines(new File(pW.getStopWords())));
		
		WordValidator wv = new WordValidator();
		
		for (int i = 0; i < databases.size(); i++) {

			List<String> words = WordsDistributionLoader.loadWordsOnly(pW.getTfIdfWordsFileForWebsite(databases.get(i)));

			List<List<String>> queries = new ArrayList<List<String>>(words.size()); 

			for (int j = 0; j < words.size() && queries.size() <= max_number_of_queries; j++) {

				String word = words.get(j).toLowerCase();
				
				if (!stopWords.contains(word) && word.length() < 400){
					if (wv.isValid(word))
						queries.add(Arrays.asList(word));
				}
			}

			for (int j = 0; j < queries.size() && j < max_number_of_queries; j++) {

//				System.out.println("Processing: " + databases.get(i).getId() + " - " + j);

				TextQuery tq = new TextQuery(queries.get(j));

				System.out.println(i + " - " + databases.get(i).getId() + " - " + j + " - " + tq.getText());
				
				long qId = pW.getTextQuery(tq);
				
				pW.insertQueryForTuplesGeneration(getSearchRoundId(),databases.get(i),qId,j);
				
			}

		}

	}

	private static int getSearchRoundId() {
		return 3;
	}

}
