package domain.training.main.splits;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import edu.columbia.cs.ref.algorithm.CandidatesGenerator;
import edu.columbia.cs.ref.model.Dataset;
import edu.columbia.cs.ref.model.Document;
import edu.columbia.cs.ref.model.constraint.role.impl.EntityTypeConstraint;
import edu.columbia.cs.ref.model.relationship.RelationshipType;
import edu.columbia.cs.ref.tool.collection.splitter.impl.KFoldSplitter;
import edu.columbia.cs.ref.tool.document.splitter.impl.OpenNLPMESplitter;
import edu.columbia.cs.ref.tool.loader.document.DocumentLoader;
import edu.columbia.cs.ref.tool.loader.document.impl.SimpleDocumentLoader;
import edu.columbia.cs.ref.tool.loader.document.impl.ace2005.ACE2005Loader;

public class SplitCollection {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String prefix = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/RELTraining/Remote/TrainingRERemote/";
		
		String rType = args[0];
		
//		String rType = "PersonTravel";
		
//		String rType = "ManMadeDisaster";
		
//		String rType = "NaturalDisaster";
	
//		String rType = "Indictment-Arrest-Trial";
		
//		RelationshipType relationshipType = new RelationshipType(rType,"ofPerson","DestinationOf");
//		relationshipType.setConstraints(new EntityTypeConstraint("LOCATION"), "DestinationOf");
//		relationshipType.setConstraints(new EntityTypeConstraint("PERSON"), "ofPerson");
		Set<RelationshipType> relationshipTypes = new HashSet<RelationshipType>();
//		relationshipTypes.add(relationshipType);
		DocumentLoader l = new SimpleDocumentLoader(relationshipTypes);
		File AIFDir = new File(prefix + "Splits/" + rType + ".txt");
		Dataset<Document> aif = new Dataset<Document>(l,AIFDir,true);
		
		String outputFolder = prefix + "Splits/" + rType + "/";
		
		KFoldSplitter docSplitter = new KFoldSplitter(5);

		docSplitter.split(aif, new File(outputFolder));
		
	}

}
