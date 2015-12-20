package sample.generation.relation.attributeSelection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import online.documentHandler.contentExtractor.impl.SgmlContentExtraction;
import domain.caching.candidatesentence.tool.RelationConfiguration;
import extraction.relationExtraction.RelationExtractionSystem;

import sample.generation.relation.attributeSelection.impl.ChiSquaredWithYatesCorrectionAttributeEval;
import sample.generation.relation.attributeSelection.impl.SMOAttributeEval;
import sample.generation.relation.attributeSelection.impl.WeightSearch;
import searcher.interaction.formHandler.TextQuery;
import utils.arff.myArffHandler;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.ChiSquaredAttributeEval;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;

public class SelectAttributes {

	public static void main(String[] args) throws Exception {

		int ieSystem = Integer.valueOf(args[0]); //17 to 20
		
		int version = 1;
		
		int relationExperiment = Integer.valueOf(args[1]);
		
		String collection = args[2];//"TREC";
		
		int db = Integer.valueOf(args[3]); //3000
		
		int size = Integer.valueOf(args[4]);
		
		boolean[] tuplesAsStopWords = new boolean[]{Boolean.valueOf(args[5])};//{true,false};
		
		int queries = 500;
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
//		String[] relations = new String[]{/*"PersonCareer","NaturalDisaster","ManMadeDisaster","Indictment","Arrest","Trial","PersonTravel",
//				"VotingResult","ProductIssues","Quotation","PollsResult",*/"Indictment-Arrest-Trial"};
		
		RelationExtractionSystem tr = RelationConfiguration.getRelationExtractionSystem(relationExperiment, pW, ieSystem, true, false, db, new SgmlContentExtraction());


//		String[] relations = new String[]{"PersonCareer","NaturalDisaster","ManMadeDisaster"/*,"Indictment","Arrest","Trial"*/,"PersonTravel",
//				"VotingResult"/*,"ProductIssues","Quotation","PollsResult"*/,"Indictment-Arrest-Trial"};

		String[] relations = new String[]{RelationConfiguration.getRelationName(relationExperiment)};
		
		int relationConf = RelationConfiguration.getRelationConf(relationExperiment);
		
		int[] workload = new int[]{RelationConfiguration.getWorkload(relationExperiment)};
		
		AttributeSelection attsel = new AttributeSelection();

		ASEvaluation[] eval = new ASEvaluation[]{/*new InfoGainAttributeEval(),*/new ChiSquaredWithYatesCorrectionAttributeEval()/*,new SMOAttributeEval()*/};

		//	    ASEvaluation eval = new ChiSquaredAttributeEval();

		//	    ASEvaluation eval = ;

		ASSearch search = new Ranker();

		//	    ASSearch search = new WeightSearch();

		for (int eve = 0; eve < eval.length; eve++) {

			attsel.setEvaluator(eval[eve]);

			attsel.setSearch(search);

			int splits = 1;

			int[] spl = {1,2,3,4,5/*,6,7,8,9,10*/};

			for (int rel = 0; rel < relations.length; rel++) {

				for (int tasw = 0; tasw < tuplesAsStopWords.length; tasw++) {

					for (int a=0;a<spl.length; a++) {

						System.gc();

						int j = spl[a];

						System.out.println("Split: " + j);

						for (int i = 1; i <= splits; i++) {

							int realsize = size * i;

							System.out.println("Size: " + realsize);

							File input = new File(pW.getReducedArffModelForSplit(collection,relations[rel],realsize,j,tuplesAsStopWords[tasw],tr.getName()));

							if (input.exists()){

								File output = new File(pW.getRelationWordsFromSplitModel(collection,relations[rel],realsize,j,eval[eve],search,tuplesAsStopWords[tasw],tr.getName()));

								if (output.exists()){
									System.out.println("output exists");
									continue;
								}
								else{

									File outpWords = pW.getRelationKeywordsFile(collection,relations[rel], tuplesAsStopWords[tasw], j, eval[eve],tr.getName());

									outpWords.createNewFile();
									
									System.err.println(output);
									
									output.createNewFile();

									selectAttributes(input,output,attsel,outpWords);

									List<String> words = FileUtils.readLines(outpWords);

									for (int l = 0; l < words.size() && l < queries; l++) {
										
//											TextQuery qq = new TextQuery(Arrays.asList(relationQG.generateQuery(rel.get(l))));
										
										TextQuery qq = new TextQuery(Arrays.asList(words.get(l)));
										
//											System.out.println(rel.get(l));
										
										pW.writeRelationKeyword(ieSystem,relationConf,collection, workload[rel],version,j,tuplesAsStopWords[tasw],eval[eve],l+1,qq,realsize);
										
									}
									
								}

							}else{
								System.out.println("input dos not exist");
							}

						}


					}


				}

			}


		}


	}

	private static void selectAttributes(File input, File output,
			AttributeSelection attsel, File outpWords) throws Exception {

		DataSource source = new DataSource(new FileInputStream(input));

		Instances data = source.getDataSet();

		data.setClassIndex(0);

		attsel.SelectAttributes(data);

		double[][] indices = attsel.rankedAttributes();

		BufferedWriter bW = new BufferedWriter(new FileWriter(output));

		BufferedWriter bW2 = new BufferedWriter(new FileWriter(outpWords));

		List<String> remaining = new ArrayList<String>();

		List<String> rem2 = new ArrayList<String>();

		for (int i = 0; i < indices.length; i++) {

			Attribute att = data.attribute((int)indices[i][0]);

			double[] values = data.attributeToDoubleArray((int)indices[i][0]);

			int usefuls = 0;

			int useless = 0;

			for (int ins = 0; ins < values.length; ins++) {

				if (data.instance(ins).value(att) == 1.0)
					if (data.instance(ins).classValue() == 1.0)
						usefuls++;
					else
						useless++;

			}

			if (usefuls > useless){ //the attribute separates

				//		    	bW.write(indices[i][1] + "," + usefuls + "," + useless + "," + att.name());

				bW.write(indices[i][1] + "," + att.name());

				bW2.write(att.name());

				bW2.newLine();

				bW.newLine();

			} else {

				remaining.add(indices[i][1] + "," + att.name());

				rem2.add(att.name());

			}

		}

		for (int i = 0; i < remaining.size(); i++) {

			bW.write(remaining.get(remaining.size()-1-i));

			bW2.write(rem2.get(rem2.size()-1-i));

			if (i>0){
				bW.newLine();
				bW2.newLine();
			}
		}

		bW.close();

		bW2.close();

	}



}
