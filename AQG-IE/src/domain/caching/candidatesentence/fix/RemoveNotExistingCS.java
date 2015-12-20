package domain.caching.candidatesentence.fix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.google.gdata.util.common.base.Pair;

import utils.persistence.PersistenceImplementation;
import utils.persistence.databaseWriter;
import utils.persistence.persistentWriter;

public class RemoveNotExistingCS {

	public static void main(String[] args) throws IOException {
		
		String prefix = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/CandidateSentences/4/3000/";
		
		List<String> lines = FileUtils.readLines(new File("/home/pjbarrio/cs.txt"));
		
		Set<String> slines = createSet(prefix,lines);
		
		databaseWriter pW = (databaseWriter)PersistenceImplementation.getWriter();
		
		List<Pair<Long,String>> cands = pW.getCandidateSentences(3000,3,4);
		
		List<Long> docsToRemove = new ArrayList<Long>();
		
		for (Pair<Long, String> pair : cands) {
			
			if (!slines.contains(pair.getSecond())){
				docsToRemove.add(pair.getFirst());
			}
			
		}

		for (int i = 0; i < docsToRemove.size(); i++) {
			
			if ((i % 1000) == 0)
				System.out.println(i);
			
			pW.performStatement("delete from GeneratedCandidateSentence where idDatabase = 3000 and idRelationConfiguration = 4 and idDocument = " + docsToRemove.get(i));
			pW.performStatement("delete from CandidateSentence where idDatabase = 3000 and idRelationConfiguration = 4 and idDocument = " + docsToRemove.get(i));
			
		}
		
	}

	private static Set<String> createSet(String prefix, List<String> lines) {
		
		Set<String> ret = new HashSet<String>();
		
		for (int i = 0; i < lines.size(); i++) {
			
			ret.add(prefix + lines.get(i).substring(2));
			
		}
		
		
		
		return ret;
		
	}
	
}
