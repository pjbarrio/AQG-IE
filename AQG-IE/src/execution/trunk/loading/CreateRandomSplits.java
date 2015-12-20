package execution.trunk.loading;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class CreateRandomSplits {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		List<String> dbs = FileUtils.readLines(new File("/home/pjbarrio/dbs.txt"));

		persistentWriter pW = PersistenceImplementation.getWriter();
		
		int split_size = 50;
		
		Collections.shuffle(dbs);
		
		int split = 0;
		
		for (int i = 0 ; i <dbs.size(); i++){
			if ((i % split_size) == 0)
				split++;
			pW.insertInteractionError(Integer.valueOf(dbs.get(i)),1);
			pW.writeExperimentSplit(split,Integer.valueOf(dbs.get(i)));
			
		}
		
	}

}
