package online.navigation.generation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import online.tagcleaner.HTMLTagCleaner;
import online.tagcleaner.impl.HTMLCleanerBasedCleaner;

import org.apache.commons.io.FileUtils;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.HtmlSerializer;
import org.htmlcleaner.SimpleHtmlSerializer;


public class CleanHTMLTags_Forth {

	public class CleanerRunnable implements Runnable{

		private File input;
		private File output;

		public CleanerRunnable(File input, File output) {
			this.input = input;
			this.output = output;
		}

		@Override
		public void run() {
			
			HTMLTagCleaner htmltc = new HTMLCleanerBasedCleaner();
			
			try {
				
				FileUtils.write(output, htmltc.clean(FileUtils.readFileToString(input)));
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String type = "alltypes";
		
		File input = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/tfidf/QueryResult/" + type + "/");
		
		File output = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/tfidf/QueryResultHTMLClean/" + type + "/");
		
//		File input = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/tfidf/QueryResultsEmpty/" + type + "/");
//		
//		File output = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/tfidf/QueryResultsEmptyHTMLClean/" + type + "/");
		
		File[] dbFolds = input.listFiles();
				
		for (int i = 0; i < dbFolds.length; i++) {
			
			String name = dbFolds[i].getName();
			
			System.out.println("DATABASE: "  + i + " - " + name);
			
			File outpFold = new File(output,name);

			if (outpFold.exists())
				continue;

			outpFold.mkdir();
						
			File[] files = dbFolds[i].listFiles();
			
			for (int j = 0; j < files.length; j++) {
				
				System.out.println("FILE: " + j);
				
				String fileName = files[j].getName();
				
				File exit = new File(outpFold,fileName);
				
				new CleanHTMLTags_Forth().execute(files[j],exit);
				
			}
			
		}
		
	}

	private void execute(File input, File output) {
		
		Thread t = new Thread(new CleanerRunnable(input,output));
		
		t.start();
		
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
