package domain.training.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;


import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.VotedPerceptron;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.J48;

import com.davidsoergel.conja.Parallel;


import edu.columbia.cs.ref.algorithm.CandidatesGenerator;
import edu.columbia.cs.ref.algorithm.StructureGenerator;
import edu.columbia.cs.ref.algorithm.feature.generation.FeatureGenerator;
import edu.columbia.cs.ref.algorithm.feature.generation.impl.EntityBasedChunkingFG;
import edu.columbia.cs.ref.algorithm.feature.generation.impl.OpenNLPPartOfSpeechFG;
import edu.columbia.cs.ref.algorithm.feature.generation.impl.OpenNLPTokenizationFG;
import edu.columbia.cs.ref.algorithm.feature.generation.impl.SpansToStringsConvertionFG;
import edu.columbia.cs.ref.engine.Engine;
import edu.columbia.cs.ref.engine.impl.JLibSVMBinaryEngine;
import edu.columbia.cs.ref.engine.impl.WekaClassifierEngine;
import edu.columbia.cs.ref.model.CandidateSentence;
import edu.columbia.cs.ref.model.Dataset;
import edu.columbia.cs.ref.model.Document;
import edu.columbia.cs.ref.model.Span;
import edu.columbia.cs.ref.model.StructureConfiguration;
import edu.columbia.cs.ref.model.constraint.role.impl.EntityTypeConstraint;
import edu.columbia.cs.ref.model.core.impl.BagOfNGramsKernel;
import edu.columbia.cs.ref.model.core.impl.DependencyGraphsKernel;
import edu.columbia.cs.ref.model.core.impl.OpenInformationExtractionCore;
import edu.columbia.cs.ref.model.core.impl.ShortestPathKernel;
import edu.columbia.cs.ref.model.core.impl.SubsequencesKernel;
import edu.columbia.cs.ref.model.core.structure.OperableStructure;
import edu.columbia.cs.ref.model.entity.Entity;
import edu.columbia.cs.ref.model.feature.impl.SequenceFS;
import edu.columbia.cs.ref.model.re.Model;
import edu.columbia.cs.ref.model.relationship.RelationshipType;
import edu.columbia.cs.ref.tool.document.splitter.impl.OpenNLPMESplitter;
import edu.columbia.cs.ref.tool.io.CandidatesSentenceReader;
import edu.columbia.cs.ref.tool.io.CandidatesSentenceWriter;
import edu.columbia.cs.ref.tool.io.CoreReader;
import edu.columbia.cs.ref.tool.loader.document.impl.AIFLoader;
import edu.columbia.cs.ref.tool.loader.document.impl.ace2005.ACE2005Loader;
import edu.columbia.cs.ref.tool.tagger.entity.EntityTagger;
import edu.columbia.cs.ref.tool.tagger.entity.coref.CorefEntityTagger;
import edu.columbia.cs.ref.tool.tagger.entity.impl.StanfordNLPCoreference;
import edu.columbia.cs.ref.tool.tagger.entity.impl.StanfordNLPTagger;
import edu.columbia.cs.ref.tool.tagger.span.impl.EntitySpan;
import edu.columbia.cs.utils.AlternativeOpenIEFeatures;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

public class TrainSystems {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * @throws ClassCastException 
	 */
	public static void main(String[] args) throws ClassCastException, IOException, ClassNotFoundException {

		String prefix = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/RELTraining/Remote/TrainingRERemote/";
		
		String rType = args[0];
		int ind = Integer.valueOf(args[1]);
		
		RelationshipType relationshipType = getRelationShipType(rType);
				
		Set<RelationshipType> relationshipTypes = new HashSet<RelationshipType>();
		relationshipTypes.add(relationshipType);
		
		StructureConfiguration[] confArr = new StructureConfiguration[5];
		String nameArr[] = new String[]{"SPK-CR","BONG-CR","SSK-CR","DG-CR","OIE-CR"};
		Classifier[] classifierArr = new Classifier[]{new J48(),new NaiveBayes(),new MultilayerPerceptron(),new JRip(),new VotedPerceptron(),new SMO()};
				
		confArr[0] = new StructureConfiguration(new ShortestPathKernel());
		
		confArr[1] = new StructureConfiguration(new BagOfNGramsKernel());
		
		confArr[2] = new StructureConfiguration(new SubsequencesKernel());
		
		confArr[3] = new StructureConfiguration(new DependencyGraphsKernel());

		FeatureGenerator<SequenceFS<Span>> tokenizer = new OpenNLPTokenizationFG("en-token.bin");
		FeatureGenerator<SequenceFS<Span>> fgChunk = new EntityBasedChunkingFG(tokenizer);
		FeatureGenerator<SequenceFS<String>> fgChuckString = new SpansToStringsConvertionFG(fgChunk);
		FeatureGenerator<SequenceFS<String>> fgPOS = new OpenNLPPartOfSpeechFG("en-pos-maxent.bin",fgChuckString);
		
//		conf.addFeatureGenerator(fgPOS);
		
		confArr[4] = new StructureConfiguration(new OpenInformationExtractionCore());
		
		int indCl = 5;
		
		StructureConfiguration conf = confArr[ind];
		String name = nameArr[ind];
		
		Classifier classifier = null;
		AlternativeOpenIEFeatures fset = null;
		Engine classificationEngine = null;
		if (ind != 4){ //not open extraction
			
			conf.addFeatureGenerator(fgPOS);
			

		}else{ //Open Information Extraction
			
			classifier = classifierArr[indCl];
			
			fset = new AlternativeOpenIEFeatures();

			
		}

		int splits = 6;
		
		for (int i = 5; i < splits; i++) {
			
			System.out.println(i);
			
			if (ind != 4){
				
				if (new File(prefix + "Models/"+rType+"-"+name+"-"+i+".model").exists())
					continue;
				
			}else{
				
				if (new File(prefix + "Models/"+rType+"-"+name+classifier.getClass().getSimpleName()+"-"+ i +".model").exists())
					continue;
				
			}
			
			List<String> files = FileUtils.readLines(new File(prefix + "Splits/" + rType + "/train-" + i));
			
			Set<OperableStructure> trainingData = new HashSet<OperableStructure>();

			System.out.print("Loading ...");
			
			for (int j = 0; j < files.size(); j++) {
				
				System.out.print(".");
				
				trainingData.addAll(CoreReader.readOperableStructures(prefix + "OperableStructures/" + rType + "-" + name + "-" +  files.get(j) + ".ser"));
				
			}
			
			System.out.println("\nDone Loading.");
			
			if (ind != 4){
				
				//The engine is responsible for the training. In this case, we are using
				//an engine based on the JLibSVM library. You may want to create your own
				//engine based on any other library.
				
				classificationEngine = new JLibSVMBinaryEngine(conf, relationshipTypes);

				
				Model svmModel = classificationEngine.train(trainingData);

				//Finally, we can store the model in order to use it later
				edu.columbia.cs.ref.tool.io.SerializationHelper.write(prefix + "Models/"+rType+"-"+name+"-"+i+".model", svmModel);

				
			}else{ //Open InformationExtraction
				
				for (int j = 0; j < classifierArr.length; j++) {
					
					classifier = classifierArr[j];
					
					fset = new AlternativeOpenIEFeatures();
					
					classificationEngine = new WekaClassifierEngine(classifier, fset.getFeatureSet(), conf, relationshipTypes);
					
					Model wekaModel = classificationEngine.train(trainingData);
					
					edu.columbia.cs.ref.tool.io.SerializationHelper.write(prefix + "Models/"+rType+"-"+name+classifier.getClass().getSimpleName()+"-"+ i +".model", wekaModel);

					
				}
				
				
			}
		
		}
		
		System.out.println("DONE!");
		
		Parallel.shutdown();

	}

	private static RelationshipType getRelationShipType(String rType) {
		
		if (rType.equals("PersonTravel")){
		
			RelationshipType ret = new RelationshipType(rType,"ofPerson","DestinationOf");
			ret.setConstraints(new EntityTypeConstraint("LOCATION"), "DestinationOf");
			ret.setConstraints(new EntityTypeConstraint("PERSON"), "ofPerson");
			return ret;
		} else if (rType.equals("ManMadeDisaster")){
			
			RelationshipType relationshipType = new RelationshipType(rType,"ManmadeDisasterAt","atLocation");
			relationshipType.setConstraints(new EntityTypeConstraint("LOCATION"), "atLocation");
			relationshipType.setConstraints(new EntityTypeConstraint("MANMADEDISASTER"), "ManmadeDisasterAt");
			return relationshipType;
		} else if (rType.equals("NaturalDisaster")){
			
			RelationshipType relationshipType = new RelationshipType(rType,"NaturalDisasterAt","atLocation");
			relationshipType.setConstraints(new EntityTypeConstraint("LOCATION"), "atLocation");
			relationshipType.setConstraints(new EntityTypeConstraint("NATURALDISASTER"), "NaturalDisasterAt");
			return relationshipType;
		} else if (rType.equals("Indictment-Arrest-Trial")){
			
			RelationshipType relationshipType = new RelationshipType(rType,"ChargeTo","toPerson");
			relationshipType.setConstraints(new EntityTypeConstraint("PERSON"), "toPerson");
			relationshipType.setConstraints(new EntityTypeConstraint("CHARGE"), "ChargeTo");
			return relationshipType;
		} else if (rType.equals("PersonCareer")){
			
			RelationshipType relationshipType = new RelationshipType(rType,"CareerOf","ofPerson");
			relationshipType.setConstraints(new EntityTypeConstraint("PERSON"), "ofPerson");
			relationshipType.setConstraints(new EntityTypeConstraint("CAREER"), "CareerOf");
			return relationshipType;
			
		} else if (rType.equals("VotingResult")){
			
			RelationshipType relationshipType = new RelationshipType(rType,"ofPoliticalEvent","WinningCandidateOf");
			relationshipType.setConstraints(new EntityTypeConstraint("PERSON"), "WinningCandidateOf");
			relationshipType.setConstraints(new EntityTypeConstraint("POLITICALEVENT"), "ofPoliticalEvent");
			return relationshipType;
		}
		
		return null;
		
	}

	
}
