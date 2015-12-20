package execution.trunk.test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import edu.columbia.cs.ref.tool.io.CandidatesSentenceReader;

public class CandSentTest {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		List<String> files = FileUtils.readLines(new File("/proj/dbNobackup/pjbarrio/Yago2/cs.txt"));
		
		for (String string : files) {
			
			System.err.println("Extracting..." + string);
			
			CandidatesSentenceReader.readCandidateSentences(string);
			
		}

	}

}
