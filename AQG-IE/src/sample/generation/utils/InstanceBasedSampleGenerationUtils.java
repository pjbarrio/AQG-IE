package sample.generation.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;

import exploration.model.Document;
import exploration.model.Sample;
import sample.generation.model.SampleBuilderParameters;
import utils.arff.myArffHandler;
import utils.document.DocumentHandler;
import utils.persistence.persistentWriter;
import weka.core.Instance;
import weka.core.InstanceComparator;
import weka.core.Instances;

public class InstanceBasedSampleGenerationUtils extends SampleGenerationUtils {

	private Instances instances;

	public InstanceBasedSampleGenerationUtils(
			SampleBuilderParameters sampleBuilderParameters, Instances instances) {
		super(sampleBuilderParameters);
		this.instances = instances;
	}

	@Override
	public void tailorAttributes(Sample sample, persistentWriter pW,int uselessSample)
			throws Exception {
		
		System.out.println("Tailoring ...");
		
		instances = tailorAttributes(instances, pW, sample, uselessSample);
	}
	
	@Override
	public void runSVM(Sample sample, persistentWriter pW,int uselessSample) throws Exception {
		
		System.out.println("Running SVM ...");
		
		runSVM(sample, pW, instances,uselessSample);
	}
	
	@Override
	public void removeAttributesBasedOnSVM(Sample sample, persistentWriter pW,
			int idDatabase, int uselessSample) throws Exception {
		
		System.out.println("Removing Attributes ...");
		
		instances = removeAttributesBasedOnSVM(sample, pW, idDatabase, instances, uselessSample);
		
	}
	
	@Override
	public void generateTrueModel(Sample sample, persistentWriter pW, int uselessSample)
			throws Exception {
		
		System.out.println("Generating True Model ...");
		
		generateTrueModel(sample, pW, sp, instances, uselessSample);
	}
	
	@Override
	public void removeDuplicates(Sample sample, persistentWriter pW, DocumentHandler dh,int uselessSample, List<Document> documents)
			throws IOException {
		
		System.out.println(" Removing Duplicates ...");
		
//		List<Document> documents = pW.getSampleDocuments(sample,sp.getUsefulDocuments(),sp.getUselessDocuments(),dh);
		
		Set<Instance> instancesSet = new TreeSet<Instance>(new InstanceComparator());

		int currentIndex = 0;
		
		for (int i = 0; i < instances.numInstances(); i++) {
			
			if (!instancesSet.add(instances.instance(i))){
			
				instances.delete(currentIndex);
				
				documents.remove(currentIndex);
				
			}else{
				
				currentIndex++;
			
			}
				
		}
		
		String output = pW.getArffFilteredRawModel(sample,sp,uselessSample);
		
//		pW.writeFilteredDocuments(sample, sp, uselessSample, documents);
		
		myArffHandler.saveInstances(output, instances);
		
	}
}
