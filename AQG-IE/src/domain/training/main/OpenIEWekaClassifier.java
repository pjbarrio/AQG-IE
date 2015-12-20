package domain.training.main;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import domain.training.main.resource.ReVerbClassifierTrainer;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.SerializationHelper;
import weka.core.converters.ArffSaver;
import edu.columbia.cs.ref.model.constraint.role.impl.EntityTypeConstraint;
import edu.columbia.cs.ref.model.core.impl.resource.OperableStructureSetInputStream;
import edu.columbia.cs.ref.model.core.structure.OperableStructure;
import edu.columbia.cs.ref.model.relationship.RelationshipType;
import edu.columbia.cs.ref.tool.io.CoreReader;
import edu.washington.cs.knowitall.extractor.conf.LabeledBinaryExtractionReader;


public class OpenIEWekaClassifier {

	/**
	 * Trains a classifier using the examples in the given file,
	 * and saves the model to disk. The examples must be in the format described in
	 * <code>LabeledBinaryExtractionReader</code>.
	 * 
	 * An optional third parameter can be passed that writes the training data in 
	 * Weka's ARFF file format to disk.
	 * 
	 * @param args
	 * @throws Exception
	 */

	
	public static void main(String[] args) throws Exception {
		
		//In the class Reverb Features, I can add more ...
		
		Classifier classifier = new J48();
		
		String rType = "PersonTravel";
		
		RelationshipType relationshipType = new RelationshipType(rType,"ofPerson","DestinationOf");
		Set<RelationshipType> relationshipTypes = new HashSet<RelationshipType>();
		relationshipTypes.add(relationshipType);
		
		String name = "OIE-CR";
		
		Set<OperableStructure> trainingData = CoreReader.readOperableStructures("C:\\Documents\\TrainingRERemote\\operableStructures" + rType + "-" + name +  ".ser");
		
		InputStream in = new OperableStructureSetInputStream(trainingData,true,relationshipTypes);//new FileInputStream(args[0]);
		
		LabeledBinaryExtractionReader reader = new LabeledBinaryExtractionReader(in);
		
		ReVerbClassifierTrainer trainer = new ReVerbClassifierTrainer(reader.readExtractions(),classifier);
		
		
		//Writes the binary model to a file
		SerializationHelper.write("C:\\Documents\\TrainingRERemote\\j48.model", classifier);
		
		//These are the operable Structures as in arff that are to be saved...
		//We can save them as operable structures or as arff ...
//		if (args.length > 2) {
			ArffSaver saver = new ArffSaver();
			saver.setInstances(trainer.getDataSet().getWekaInstances());
			saver.setFile(new File("C:\\Documents\\TrainingRERemote\\j48.arff"));
			saver.writeBatch();
//		}

		
	}
	
}
