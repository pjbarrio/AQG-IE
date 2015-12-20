package sample.generation.catching;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import searcher.interaction.formHandler.TextQuery;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class GenerateRandomQueriesList {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String collection = "UBUNTU";
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		Set<String> stopWords = new HashSet<String>(FileUtils.readLines(new File(pW.getStopWords())));
		
		int splits = 5;
		
		Set<String> usedWords = new HashSet<String>();
		
		for (int i = 1; i <= splits; i++) {

			List<String> words = FileUtils.readLines(new File("/proj/db-files2/NoBackup/pjbarrio/Dataset/" + collection + "/word_distribution.txt"));
			
			words.removeAll(stopWords);
			
			Collections.shuffle(words);

			pW.saveInitialRandomQueries(collection, i, words);
			
			usedWords.addAll(words.subList(0, 1000));
			
		}
		
		for (String string : usedWords) {
			
			pW.writeTextQuery(new TextQuery(Arrays.asList(string)));
			
		}
		
	}

}
