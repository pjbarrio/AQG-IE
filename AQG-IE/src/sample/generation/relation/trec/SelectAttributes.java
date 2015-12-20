package sample.generation.relation.trec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import sample.generation.relation.attributeSelection.impl.ChiSquaredWithYatesCorrectionAttributeEval;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.Ranker;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class SelectAttributes {

	public static void main(String[] args) throws Exception {

		String extractor = "OpenCalais";
		
		String collection = "TREC";
		
		int size = 5000;
		
		int[] spl = {1,2,3,4,5};

		boolean[] tuplesAsStopWords = {false};//new boolean[]{Boolean.valueOf(args[5])};//{true,false};
		
		persistentWriter pW = PersistenceImplementation.getWriter();
			
		AttributeSelection attsel = new AttributeSelection();

		ASEvaluation[] eval = new ASEvaluation[]{/*new InfoGainAttributeEval(),*/new ChiSquaredWithYatesCorrectionAttributeEval()/*,new SMOAttributeEval()*/};

		ASSearch search = new Ranker();

		for (int eve = 0; eve < eval.length; eve++) {

			attsel.setEvaluator(eval[eve]);

			attsel.setSearch(search);

			int splits = 1;

			for (int rel = 0; rel < GenerateDocumentsList.relations.length; rel++) {

				for (int tasw = 0; tasw < tuplesAsStopWords.length; tasw++) {

					for (int a=0;a<spl.length; a++) {

						System.gc();

						int j = spl[a];

						System.out.println("Split: " + j);

						for (int i = 1; i <= splits; i++) {

							int realsize = size * i;

							System.out.println("Size: " + realsize);

							File input = new File(pW.getReducedArffModelForSplit(collection,GenerateDocumentsList.relations[rel],realsize,j,tuplesAsStopWords[tasw],extractor));

							if (input.exists()){

								File output = new File(pW.getRelationWordsFromSplitModel(collection,GenerateDocumentsList.relations[rel],realsize,j,eval[eve],search,tuplesAsStopWords[tasw],extractor));

								if (output.exists()){
									System.out.println("output exists");
									continue;
								}
								else{

									File outpWords = pW.getRelationKeywordsFile(collection,GenerateDocumentsList.relations[rel], tuplesAsStopWords[tasw], j, eval[eve],extractor);

									outpWords.createNewFile();
									
									System.err.println(output);
									
									output.createNewFile();

									selectAttributes(input,output,attsel,outpWords);
									
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
