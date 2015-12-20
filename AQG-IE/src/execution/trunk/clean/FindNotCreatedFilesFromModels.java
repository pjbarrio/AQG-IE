package execution.trunk.clean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class FindNotCreatedFilesFromModels {

	public FindNotCreatedFilesFromModels() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		List<String> files = pW.getBooleanModelFiles();
		
		Collections.sort(files);
		
		System.err.println("checking files");
		
		System.setOut(new PrintStream(new File("notTrue.txt")));
		
		for (int i = 0; i < files.size(); i++) {
			
			if (i % 1000 == 0)
				System.err.println(i);
			
			if (!new File(files.get(i)).exists()){
				System.err.println(files.get(i));
				System.out.println(files.get(i));
			}
			
		}
		

	}

}
