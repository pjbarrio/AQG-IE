package online.navigation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import online.navigation.textTransformer.NumberReplaceMent;
import online.navigation.textTransformer.TextTransformer;
import online.navigation.thread.SummaryCreatorRunnable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import utils.execution.AbsoluteNumberNameComparator;
import utils.execution.IsInListFileFilter;

public class SummaryCreator {

	private static List<String> toProcess;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String type = "alltypes";
		
		String encoding = "UTF-8";
		
		toProcess = FileUtils.readLines(new File("/proj/dbNoBackup/pjbarrio/Experiments/Wrappers/" + type));
		
		File nav = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/tfidf/QueryResultHTMLClean/" + type + "/");
		
		File navEmpty = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/tfidf/QueryResultsEmptyHTMLClean/" + type + "/");
		
		File outFold = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/navigationHandler/CHNH/");
		
		String tfidffolder = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/tfidf/"+type+"/";
		
		FileFilter ff = new IsInListFileFilter(toProcess);
			
		File[] files = nav.listFiles(ff);
		
//		files = new File[1];
//		
//		files[0]= new File("/proj/dbNoBackup/pjbarrio/Experiments/Wrappers/QueryResultsHTMLClean/"+type+"/2086/");
		
		TextTransformer tt = new NumberReplaceMent();
		
		Comparator<File> cc = new AbsoluteNumberNameComparator<File>();
		
		Arrays.sort(files,cc);
		
		for (int i = 0; i < files.length; i++) {
			
			System.out.println("DATABASE: " + i + " - " + files[i].getName());
			
			System.gc();
			
			File folder = files[i];
			
			File outputLinks = new File(outFold,folder.getName() + ".links.ser");
			
			if (outputLinks.exists())
				continue;
			
			File[] follows = ArrayUtils.addAll(folder.listFiles(),new File(navEmpty,folder.getName()).listFiles()); 
			
			
			File outputText = new File(outFold,folder.getName() + ".text.ser");
			
			File outputFeatures = new File(outFold,folder.getName() + ".features.ser");
			
			File outputNumber = new File(outFold,folder.getName() + ".number.ser");
			
			List<String> tfidfs = getTfIdfs(FileUtils.readLines(new File(tfidffolder,files[i].getName() + ".txt")));
			
			new SummaryCreator().execute(follows,tfidfs,tt,outputLinks,outputText,outputFeatures,outputNumber,encoding);
			
		}

	}

	private static List<String> getTfIdfs(List<String> readLines) {
		
		List<String> ret = new ArrayList<String>();
		
		for (String string : readLines) {
			
			ret.add(string.split(" ")[0]);
			
		}
		
		return ret;
	}

	private void execute(File[] follows, List<String> tfidfs,
			TextTransformer tt, File outputLinks, File outputText,
			File outputFeatures, File outputNumber, String encoding) {
		
		Thread t = new Thread(new SummaryCreatorRunnable(follows,tfidfs,tt, outputLinks,outputText,outputFeatures,outputNumber,encoding));
		
		t.start();
		
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
