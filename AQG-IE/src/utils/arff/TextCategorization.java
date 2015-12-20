package utils.arff;

import java.io.File;
import java.io.IOException;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.TextDirectoryLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class TextCategorization {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		 // convert the directory into a dataset
	    TextDirectoryLoader loader = new TextDirectoryLoader();
	    loader.setDirectory(new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/WekaTextCategorization/test"));
	    
	    Instances dataRaw = loader.getDataSet();
	    //System.out.println("\n\nImported data:\n\n" + dataRaw);

	    // apply the StringToWordVector
	    // (see the source code of setOptions(String[]) method of the filter
	    // if you want to know which command-line option corresponds to which
	    // bean property)
	    StringToWordVector filter = new StringToWordVector();
	    
	    filter.setOptions(new String[]{"-L","-S"});
	    
	    filter.setInputFormat(dataRaw);
	    
	    Instances dataFiltered = Filter.useFilter(dataRaw, filter);
	    //System.out.println("\n\nFiltered data:\n\n" + dataFiltered);

	    
	    ArffSaver saver = new ArffSaver();
	    saver.setInstances(dataRaw);
	    saver.setFile(new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/WekaTextCategorization/testNoFilter.arff"));
	    saver.writeBatch();
	    
	    saver = new ArffSaver();
	    saver.setInstances(dataFiltered);
	    saver.setFile(new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/WekaTextCategorization/test.arff"));
	    saver.writeBatch();
	    
	}

}
