package domain.training.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.columbia.cs.ref.algorithm.CandidatesGenerator;
import edu.columbia.cs.ref.model.CandidateSentence;
import edu.columbia.cs.ref.model.Dataset;
import edu.columbia.cs.ref.model.Document;
import edu.columbia.cs.ref.model.constraint.role.impl.EntityTypeConstraint;
import edu.columbia.cs.ref.model.entity.Entity;
import edu.columbia.cs.ref.model.relationship.RelationshipType;
import edu.columbia.cs.ref.tool.document.splitter.impl.OpenNLPMESplitter;
import edu.columbia.cs.ref.tool.io.CandidatesSentenceWriter;
import edu.columbia.cs.ref.tool.loader.document.impl.AIFLoader;
import edu.columbia.cs.ref.tool.tagger.entity.EntityTagger;
import edu.columbia.cs.ref.tool.tagger.entity.coref.CorefEntityTagger;
import edu.columbia.cs.ref.tool.tagger.entity.impl.StanfordNLPCoreference;
import edu.columbia.cs.ref.tool.tagger.entity.impl.StanfordNLPTagger;
import edu.columbia.cs.ref.tool.tagger.span.impl.EntitySpan;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.util.CoreMap;

public class CandidateGeneration {

	private static RelationshipType relationshipType;
	
	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * @throws ClassCastException 
	 */
	public static void main(String[] args) throws ClassCastException, IOException, ClassNotFoundException {
		
		String prefix = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/RELTraining/Remote/TrainingRERemote/";
		
		//First, we need to define the type of relationship we want to extract
		//as well as the constraints imposed to the entities involved in the
		//relationship. Our objective is to extract a relationship tagged as
		//ORG-AFF in the training data and for which the first argument ("Arg-1")
		//is a person ("PER") and the second argument ("ARG-2") is an organization
		//("ORG"). Note that you may not introduce constraints but if you decide
		//not to do it then the candidate entities will include all combinations
		//of all entity types
		
		

		//We need a sentence splitter to create the candidates generator
		OpenNLPMESplitter splitter = new OpenNLPMESplitter("en-sent.bin");
		CandidatesGenerator generator = new CandidatesGenerator(splitter);

		EntityTagger<EntitySpan, Entity>[] entityTaggers = new EntityTagger[1];
		
		Set<String> tags = new HashSet<String>();
		
		Map<String,String> taskTable = new HashMap<String,String>();
		
		Map<String,String> typeTable = new HashMap<String, String>();

		Set<RelationshipType> relationshipTypes = new HashSet<RelationshipType>();
		

		String rType = args[0];
		
//		System.setErr(new PrintStream(new File(prefix + "Output/cand-" + rType)));
		
		initialize(rType,tags,typeTable,taskTable);
		
		relationshipTypes.add(relationshipType);
		
		entityTaggers[0] = new StanfordNLPTagger<CoreMap>(tags,CRFClassifier.getClassifier(new File("model/english.all.3class.distsim.crf.ser.gz")));
		
		CorefEntityTagger[] coref = new CorefEntityTagger[1];
		
		coref[0] = new StanfordNLPCoreference(tags);
		
		//In order to load the data we need a DocumentLoader. In this case, we
		//use the ACE2005Loader but you may use your own loader for your own data
		
		
		AIFLoader l = new AIFLoader(relationshipTypes,typeTable, taskTable, coref, entityTaggers);

		
		File AIFDir = new File(prefix + rType + "/1/");

		Dataset<Document> pertrav = new Dataset<Document>(l,AIFDir,false);
		
		Set<CandidateSentence> candidates = new HashSet<CandidateSentence>();

		for(Document d : pertrav){
			
			if (new File(prefix + "Candidates/" + rType + "-" + d.getFilename() + ".candsent").exists())
				continue;
			
			Set<CandidateSentence> sent = generator.generateCandidates(d, relationshipTypes);
			
			CandidatesSentenceWriter.writeCandidateSentences(sent, prefix + "Candidates/" + rType + "-" + d.getFilename() + ".candsent");
			
			candidates.addAll(sent);	
		}

	}

	private static void initialize(String rType, Set<String> tags, Map<String, String> typeTable, Map<String, String> taskTable) {
		

		if (rType.equals("PersonTravel")){
			//Person Travel
			tags.add("LOCATION");
			tags.add("PERSON");
			taskTable.put(rType,"/home/pjbarrio/Software/Callisto/tasks/org.mitre.example.persontravel.jar");
			typeTable.put("ofPerson", "PERSON");
			typeTable.put("DestinationOf", "LOCATION");
			relationshipType = new RelationshipType(rType,"ofPerson","DestinationOf");
			relationshipType.setConstraints(new EntityTypeConstraint("LOCATION"), "DestinationOf");
			relationshipType.setConstraints(new EntityTypeConstraint("PERSON"), "ofPerson");
		} else if (rType.equals("ManMadeDisaster")){
			
			tags.add("LOCATION");
			tags.add("MANMADEDISASTER");
			taskTable.put(rType,"/home/pjbarrio/Software/Callisto/tasks/org.mitre.example.manmadedisaster.jar");
			typeTable.put("ManmadeDisasterAt", "MANMADEDISASTER");
			typeTable.put("atLocation", "LOCATION");
			relationshipType = new RelationshipType(rType,"ManmadeDisasterAt","atLocation");
			relationshipType.setConstraints(new EntityTypeConstraint("LOCATION"), "atLocation");
			relationshipType.setConstraints(new EntityTypeConstraint("MANMADEDISASTER"), "ManmadeDisasterAt");

			
		} else if (rType.equals("NaturalDisaster")){
			
//			NaturalDisaster
			tags.add("LOCATION");
			tags.add("NATURALDISASTER");
			taskTable.put(rType,"/home/pjbarrio/Software/Callisto/tasks/org.mitre.example.natdisrelation.jar");
			typeTable.put("NaturalDisasterAt", "NATURALDISASTER");
			typeTable.put("atLocation", "LOCATION");
			relationshipType = new RelationshipType(rType,"NaturalDisasterAt","atLocation");
			relationshipType.setConstraints(new EntityTypeConstraint("LOCATION"), "atLocation");
			relationshipType.setConstraints(new EntityTypeConstraint("NATURALDISASTER"), "NaturalDisasterAt");
			
		} else if (rType.equals("Indictment-Arrest-Trial")){
			
			tags.add("PERSON");
			tags.add("CHARGE");
			taskTable.put(rType,"/home/pjbarrio/Software/Callisto/tasks/org.mitre.example.charges.jar");
			typeTable.put("ChargeTo", "CHARGE");
			typeTable.put("toPerson", "PERSON");
			relationshipType = new RelationshipType(rType,"ChargeTo","toPerson");
			relationshipType.setConstraints(new EntityTypeConstraint("PERSON"), "toPerson");
			relationshipType.setConstraints(new EntityTypeConstraint("CHARGE"), "ChargeTo");
			
		} else if (rType.equals("PersonCareer")){
			
			tags.add("PERSON");
			tags.add("CAREER");
			taskTable.put(rType,"/home/pjbarrio/Software/Callisto/tasks/org.mitre.example.personcareer.jar");
			typeTable.put("CareerOf", "CAREER");
			typeTable.put("ofPerson", "PERSON");
			relationshipType = new RelationshipType(rType,"CareerOf","ofPerson");
			relationshipType.setConstraints(new EntityTypeConstraint("PERSON"), "ofPerson");
			relationshipType.setConstraints(new EntityTypeConstraint("CAREER"), "CareerOf");
			
		} else if (rType.equals("VotingResult")){
			
			tags.add("PERSON");
			tags.add("POLITICALEVENT");
			taskTable.put(rType,"/home/pjbarrio/Software/Callisto/tasks/org.mitre.example.votingresult.jar");
			typeTable.put("ofPoliticalEvent", "POLITICALEVENT");
			typeTable.put("WinningCandidateOf", "PERSON");
			relationshipType = new RelationshipType(rType,"ofPoliticalEvent","WinningCandidateOf");
			relationshipType.setConstraints(new EntityTypeConstraint("PERSON"), "WinningCandidateOf");
			relationshipType.setConstraints(new EntityTypeConstraint("POLITICALEVENT"), "ofPoliticalEvent");
			
		}
		
	}

}
