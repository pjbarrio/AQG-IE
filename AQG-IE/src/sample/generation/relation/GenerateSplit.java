package sample.generation.relation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import execution.workload.tuple.Tuple;
import extraction.relationExtraction.impl.RDFRelationExtractor;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class GenerateSplit {

	private static final int ERROR = 0;
	private static final int USEFUL = 1;
	private static final int USELESS = -1;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		//First Algorithm of the sequence. Detects Useful Documents. Continue to GenerateDocumentList
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		String collection = "TREC";
		
		Set<String> files = new HashSet<String>(FileUtils.readLines(new File(pW.getDocumentListForCollection(collection))));

		String[] relations = new String[]{/*"PersonCareer","NaturalDisaster","ManMadeDisaster","Indictment","Arrest","Trial","PersonTravel",
				"VotingResult","ProductIssues",*/"Quotation","PollsResult"};
			
		List<String> alreadyProcessed = FileUtils.readLines(new File(pW.getDocumentListForCollection(collection)+ ".old"));
		
		System.out.println(files.size());
		
		System.out.println(alreadyProcessed.size());
		
		for (int i = 0; i < alreadyProcessed.size(); i++) {
			
			files.remove(alreadyProcessed.get(i));
			
		}
		
		System.out.println(files.size());
		
//		System.exit(0);
		
		RDFRelationExtractor rdfRelationExtractor = new RDFRelationExtractor();
		
		for (int rel = 0; rel < relations.length; rel++) {
			
			List<String> usefulFiles = new ArrayList<String>();
			List<String> uselessFiles = new ArrayList<String>();
			
			int processed = 0;
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(pW.getMatchingTuplesWithSources(collection,relations[rel]),true));
			
			for (String string : files) {
				
				processed++;
				
				if (processed % 1000 == 0)
					System.out.println("Processed: " + processed);
				
				File f = new File(string);
				
				int output = ContainsRelation(relations[rel],f);
				
				if (output == USEFUL){
					usefulFiles.add(string);
					List<Tuple> tuples = rdfRelationExtractor.extract(relations[rel], FileUtils.readFileToString(new File(string)));
					for (int i = 0; i < tuples.size(); i++) {
						bw.write(string + "," + tuples.get(i).toString());
						bw.newLine();
					}
				}else if (output == USELESS){
					uselessFiles.add(string);
				}
				
			}
			
			bw.close();
			
			File useful = new File(pW.getUsefulDocumentsForCollection(collection,relations[rel]));
			
			File useless = new File(pW.getUselessDocumentsForCollection(collection,relations[rel]));
			
			FileUtils.writeLines(useful, usefulFiles,true);
			
			FileUtils.writeLines(useless, uselessFiles,true);
			
			
		}
		

	}

	private static int ContainsRelation(String relation, File f) throws IOException {
		
		FileReader br = new FileReader(f);
		
		int ch;
		
		StringBuilder sb = new StringBuilder();
		
		while (br.ready() && (ch = br.read())!='\n'){
			
			sb.append((char)ch);
			
		}
		
		String line = sb.toString();
		
		br.close();
		
		if (line.length() > 214){
			
			String shortt= line.substring(214);
			
			if (shortt.startsWith("uages") || shortt.contains("</Error>")){
				return ERROR;
			}else{
				
				if (line.contains(relation))
					return USEFUL;
				else
					return USELESS;
			}
						
		}
		
		return USELESS;
			
		
		
	}

}
