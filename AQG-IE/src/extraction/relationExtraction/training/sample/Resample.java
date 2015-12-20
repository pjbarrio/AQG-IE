package extraction.relationExtraction.training.sample;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class Resample {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		String collection = "TREC";
		String relation = "Trial";
		
		File selectedUsefuls = new File(pW.getSelectedUsefulDocumentsForCollection(collection, relation, 650));
		
		List<String> list = FileUtils.readLines(selectedUsefuls);

		Collections.shuffle(list);
		
		System.setOut(new PrintStream(new String("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/RELTraining/Dataset/" + relation + ".txt")));
		
		for (int i = 0; i < list.size() && i < 215; i++) {
			
			System.out.println(new File(list.get(i)).getName() + ".sgml");
			
		}
		
		
		
	}

}
