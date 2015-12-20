package domain.training.main;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import edu.columbia.cs.ref.algorithm.StructureGenerator;
import edu.columbia.cs.ref.algorithm.feature.generation.FeatureGenerator;
import edu.columbia.cs.ref.algorithm.feature.generation.impl.EntityBasedChunkingFG;
import edu.columbia.cs.ref.algorithm.feature.generation.impl.OpenNLPPartOfSpeechFG;
import edu.columbia.cs.ref.algorithm.feature.generation.impl.OpenNLPTokenizationFG;
import edu.columbia.cs.ref.algorithm.feature.generation.impl.SpansToStringsConvertionFG;
import edu.columbia.cs.ref.model.CandidateSentence;
import edu.columbia.cs.ref.model.Span;
import edu.columbia.cs.ref.model.StructureConfiguration;
import edu.columbia.cs.ref.model.core.impl.BagOfNGramsKernel;
import edu.columbia.cs.ref.model.core.impl.DependencyGraphsKernel;
import edu.columbia.cs.ref.model.core.impl.OpenInformationExtractionCore;
import edu.columbia.cs.ref.model.core.impl.ShortestPathKernel;
import edu.columbia.cs.ref.model.core.impl.SubsequencesKernel;
import edu.columbia.cs.ref.model.core.structure.OperableStructure;
import edu.columbia.cs.ref.model.feature.impl.SequenceFS;
import edu.columbia.cs.ref.tool.io.CandidatesSentenceReader;
import edu.columbia.cs.ref.tool.io.CandidatesSentenceWriter;
import edu.columbia.cs.ref.tool.io.CoreWriter;

public class OperableStructureGeneration {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		String rType = args[0];
		
		int ind = Integer.valueOf(args[1]);
		
		String prefix = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/RELTraining/Remote/TrainingRERemote/";
		
		File files = new File(prefix + "Splits/" + rType + ".txt");
		
		List<String> fstring = FileUtils.readLines(files);

		StructureConfiguration[] confArr = new StructureConfiguration[5];
		
		String nameArr[] = new String[]{"SPK-CR","BONG-CR","SSK-CR","DG-CR","OIE-CR"};

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

		StructureConfiguration conf = confArr[ind];
		String name = nameArr[ind];
		
		if (ind != 4) //not open extraction
			conf.addFeatureGenerator(fgPOS);
		
//		System.setErr(new PrintStream(new File(prefix + "Output/Os-"+rType + "-" + name)));
		
		for (int i = 0; i < fstring.size(); i++) {
			
			if (new File(prefix + "OperableStructures/" + rType + "-" + name + "-" +  fstring.get(i) + ".ser").exists())
				continue;
			
			Set<CandidateSentence> candidates = CandidatesSentenceReader.readCandidateSentences(prefix + "Candidates/" + rType + "-" + fstring.get(i) + ".candsent");
			
			//Now that we have the candidate sentences, we need to convert them to
			//the structure used by the core and enrich it with additional features.
			//In this example we will consider only one additional features: the POS
			//tags of each word in the sentence. However, since the POS feature extractor
			//depends on other features, we need to create these dependencies:
			
			Set<OperableStructure> trainingData = StructureGenerator.generateStructures(candidates, conf);

			CoreWriter.writeOperableStructures(trainingData, prefix + "OperableStructures/" + rType + "-" + name + "-" +  fstring.get(i) + ".ser");
		
		}
		
				

	}

}
