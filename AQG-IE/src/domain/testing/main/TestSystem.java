package domain.testing.main;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.VotedPerceptron;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.J48;

import edu.columbia.cs.ref.algorithm.evaluation.Evaluator;
import edu.columbia.cs.ref.algorithm.evaluation.measure.Measure;
import edu.columbia.cs.ref.algorithm.evaluation.measure.impl.FMeasure;
import edu.columbia.cs.ref.algorithm.evaluation.measure.impl.NumberOfExpectedPositiveAnswers;
import edu.columbia.cs.ref.algorithm.evaluation.measure.impl.NumberOfPositiveAnswers;
import edu.columbia.cs.ref.algorithm.evaluation.measure.impl.NumberOfTruePositives;
import edu.columbia.cs.ref.algorithm.evaluation.measure.impl.Precision;
import edu.columbia.cs.ref.algorithm.evaluation.measure.impl.Recall;
import edu.columbia.cs.ref.model.StructureConfiguration;
import edu.columbia.cs.ref.model.core.impl.DependencyGraphsKernel;
import edu.columbia.cs.ref.model.core.structure.OperableStructure;
import edu.columbia.cs.ref.model.re.Model;
import edu.columbia.cs.ref.tool.io.CoreReader;

public class TestSystem {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {

		String prefix = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/RELTraining/Remote/TrainingRERemote/";
		
		String rType = args[0];
		
		int ind = Integer.valueOf(args[1]);
		
		String nameArr[] = new String[]{"SPK-CR","BONG-CR","SSK-CR","DG-CR","OIE-CR"};
		
		Classifier[] classifierArr = new Classifier[]{new J48(),new NaiveBayes(),new MultilayerPerceptron(),new JRip(),new VotedPerceptron(), new SMO()};
		
		int splits = 5;
		
		int clInd = 5;
		
		String name = nameArr[ind];
		
		Model model = null;
		
		for (int i = 0; i < splits; i++) {

			if (ind != 4){
				
				model = (Model) edu.columbia.cs.ref.tool.io.SerializationHelper.read(prefix + "Models/"+rType+"-"+name+"-"+i+".model");
				
				
			}else{
				
				model = (Model) edu.columbia.cs.ref.tool.io.SerializationHelper.read(prefix + "Models/"+rType+"-"+name+classifierArr[clInd].getClass().getSimpleName()+"-"+ i +".model");
				
				
			}
			
			List<String> files = FileUtils.readLines(new File(prefix + "Splits/" + rType + "/test-" + i));
			
			Set<OperableStructure> testingFiles = new HashSet<OperableStructure>();
			
			for (int j = 0; j < files.size(); j++) {
				
				testingFiles.addAll(CoreReader.readOperableStructures(prefix + "OperableStructures/" + rType + "-" + name + "-" +  files.get(j) + ".ser"));
				
			}
			
			System.out.println("Loaded " + testingFiles.size() + " testing sentences");
			
			Evaluator eval = new Evaluator();
			Measure tp = new NumberOfTruePositives();
			eval.addMeasure(tp);
			Measure pa = new NumberOfPositiveAnswers();
			eval.addMeasure(pa);
			Measure epa = new NumberOfExpectedPositiveAnswers();
			eval.addMeasure(epa);
			Measure rec = new Recall();
			eval.addMeasure(rec);
			Measure pre = new Precision();
			eval.addMeasure(pre);
			Measure f = new FMeasure(1.0);
			eval.addMeasure(f);
			
			
			eval.printEvaluationReport(new ArrayList<OperableStructure>(testingFiles), model);
			
			System.out.println("Model: " + name);

			
		}
		
		

	}

}
