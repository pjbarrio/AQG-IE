package sample.generation.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import sample.AttributeTailoring.In_FrequentRemovalFilter;
import sample.generation.model.SampleBuilderParameters;
import utils.SVM.RFeaturesLoader.SVMFeaturesLoader;
import utils.arff.myArffHandler;
import utils.document.DocumentHandler;
import utils.persistence.persistentWriter;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.SubsetByExpression;
import exploration.model.Document;
import exploration.model.Sample;

public class SampleGenerationUtils {

	protected SampleBuilderParameters sp;

	public SampleGenerationUtils(SampleBuilderParameters sampleBuilderParameters) {
		this.sp = sampleBuilderParameters;
	}

	public void removeDuplicates(Sample sample, persistentWriter pW, DocumentHandler dh, int uselessSample, List<Document> documents) throws IOException {
		
		String input = pW.getArffRawModel(sample,sp,uselessSample);
		
		String output = pW.getArffFilteredRawModel(sample,sp,uselessSample);
		
		BufferedReader br = new BufferedReader(new FileReader(new File(input)));
		
		String line = br.readLine();
		
		ArrayList<String> lines = new ArrayList<String>();
		
		while (!line.toLowerCase().equals("@data")){
			
			lines.add(line);
			
			line = br.readLine();

		}
		
		lines.add(line); //@data
		
		line = br.readLine(); //first line
		
		while (line != null && line.trim().equals("")){
			
			lines.add(line);
			
			line = br.readLine();
		}
		
		int d = 0;
		
//		List<Document> documents = pW.getSampleDocuments(sample,sp.getUsefulDocuments(),sp.getUselessDocuments(),dh);
		
		List<Document> filteredDocuments = new ArrayList<Document>();
		
		Document lineSample = documents.get(0);
		
		int i = 0;
		
		while (line != null){
			
			if (!lines.contains(line)){
				
				lines.add(line);
				
				filteredDocuments.add(lineSample);
				
			} else {
				
				d++;
				
				System.out.println(sample.getDatabase().getId() + "---> Duplicated!" + d);
			
			}
			
			line = br.readLine();
		
			i++;
			
			lineSample = documents.get(i);
			
		}

		br.close();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(output)));
		
		for (String string : lines) {
			
			bw.write(string + "\n");
			
		}
		
		bw.close();
		
//		pW.writeFilteredDocuments(sample,sp,sp.getUselessDocuments(),filteredDocuments);
		
	}
	
	public void tailorAttributes(Sample sample, persistentWriter pW, int uselessSample) throws Exception {
				
		Instances data = myArffHandler.loadInstances(pW.getArffFilteredRawModel(sample,sp,uselessSample));

		Instances res = tailorAttributes(data,pW,sample,uselessSample);
		
		
	}

	protected Instances tailorAttributes(Instances data, persistentWriter pW, Sample sample, int uselessSample) {
		
		String output = pW.getArffTailoredModel(sample,sp,uselessSample);
		
		In_FrequentRemovalFilter ifr = new In_FrequentRemovalFilter();
		
		ifr.setMinFrequencyvalue(sp.getMinFrequency());
		ifr.setMaxFrequencyvalue(sp.getMaxFrequency());
		
		try {
			ifr.setInputFormat(data);
			
			Instances res = ifr.process(data);
			
			myArffHandler.saveInstances(output,res);
			
			return res;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
		
	}

	public void runSVM(Sample sample, persistentWriter pW, int uselessSample) throws Exception {
		
		Instances data = myArffHandler.loadInstances(pW.getArffTailoredModel(sample,sp, uselessSample));
		
		runSVM(sample,pW,data, uselessSample);
				
	}
	
	protected void runSVM(Sample sample, persistentWriter pW, Instances data, int uselessSample) {
		
		try {
	
			SMO scheme = new SMO();

			scheme.buildClassifier(data);
			
			FileUtils.writeStringToFile(new File(pW.getSMOWekaOutput(sample,sp,uselessSample)), scheme.toString() + "\n");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void removeAttributesBasedOnSVM(Sample sample, persistentWriter pW, int idDatabase, int uselessSample) throws Exception {
		
		Instances data = myArffHandler.loadInstances(pW.getArffTailoredModel(sample,sp, uselessSample));
		
		Instances save = removeAttributesBasedOnSVM(sample,pW,idDatabase,data, uselessSample);
		
	}
	
	protected Instances removeAttributesBasedOnSVM(Sample sample,
			persistentWriter pW, int id, Instances data, int uselessSample) {
		
		try {
			
			SVMFeaturesLoader.loadFeatures(new File(pW.getSMOWekaOutput(sample,sp,uselessSample)),data);
			
			ArrayList<String> feats = SVMFeaturesLoader.getFeatures();

			int[] noRemove = new int[Math.min(sp.getFeatures()+1, feats.size() + 1)];
			
			int i;
			
			for (i = 0; i < sp.getFeatures() && i < feats.size(); i++) {
				
				System.out.println(id + " --> " + i + " - Feature: " + feats.get(i));
				
				noRemove[i] = data.attribute(feats.get(i)).index();
				
			}
			
			noRemove[i]= data.classIndex();
			
			Remove r = new Remove();
			
			r.setInvertSelection(true);
			
			r.setAttributeIndicesArray(noRemove);
			
			r.setInputFormat(data);
			
			Instances res = Filter.useFilter(data, r);
			
			myArffHandler.saveInstances(pW.getArffBooleanModel(sample,sp,uselessSample), res);
			
			return res;
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}
	public void generateTrueModel(Sample sample, persistentWriter pW, int uselessSample) throws Exception {
		
		String booleanModel = pW.getArffBooleanModel(sample,sp, uselessSample);
		
		Instances data = myArffHandler.loadInstances(booleanModel);
		
		generateTrueModel(sample,pW,sp,data, uselessSample);
		
	}

	protected void generateTrueModel(Sample sample, persistentWriter pW,
			SampleBuilderParameters sp, Instances data, int uselessSample) {
		
		try {
			
			String trueModel = pW.getArffBooleanTrueModel(sample,sp,uselessSample);
			
			SubsetByExpression filter = new SubsetByExpression();
			
			filter.setExpression("CLASS is '1'");
			
			filter.setInputFormat(data);
			
			myArffHandler.saveInstances(trueModel,Filter.useFilter(data,filter));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
}
