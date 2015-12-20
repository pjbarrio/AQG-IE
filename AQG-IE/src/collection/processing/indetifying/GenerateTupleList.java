package collection.processing.indetifying;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import execution.workload.tuple.Tuple;
import extraction.relationExtraction.impl.RDFRelationExtractor;

public class GenerateTupleList {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		File file = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/tfidf/SizeOrderedExtractions/extractionList");
		
		String relation = "PersonCareer";
		
		File out = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/tfidf/SizeOrderedExtractions/" + relation);
		
		RDFRelationExtractor rdfExtractor = new RDFRelationExtractor();
		
		List<String> files = FileUtils.readLines(file);		

		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		
		int i = 0;
		
		for (String str : files) {
			
			if (i%1000 == 0)
				System.out.println("Processing: " + i + " out of: " + files.size());
			
			File f = new File(str);
			
			String content = FileUtils.readFileToString(f);
			
			List<Tuple> tuples = rdfExtractor.extract(relation, content);
			
			for (Tuple tuple : tuples) {
//				System.out.println(tuple);
				bw.write(tuple.toString());
				bw.newLine();
			}
			
			i++;
			
		}
		
		bw.close();
		
	}

}
