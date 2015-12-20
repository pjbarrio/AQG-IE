package online.cleaner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.HtmlSerializer;
import org.htmlcleaner.SimpleHtmlSerializer;
import org.w3c.dom.Document;


public class Cleaner {

	public class CleanerRunnable implements Runnable{

		private File input;
		private File output;

		public CleanerRunnable(File input, File output) {
			this.input = input;
			this.output = output;
		}

		@Override
		public void run() {
			
			try {
				
				CleanerProperties props = new CleanerProperties();

				HtmlCleaner cleaner = new HtmlCleaner(props);
				
				org.htmlcleaner.TagNode node = cleaner.clean(input);
			
				HtmlSerializer htmlSerializer = new SimpleHtmlSerializer(props);
				
				props.setOmitXmlDeclaration(true);
				
				BufferedWriter sw;
				
				sw = new BufferedWriter(new FileWriter(output));
				htmlSerializer.write(node, sw, "UTF-8");
				sw.close();
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
		
		String type = "smalltraining";
		
		File input = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/tfidf/QueryResult/" + type + "/");
		
		File output = new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/tfidf/CleanQueryResult/" + type + "/");
		
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
				
				new Cleaner().execute(files[j],exit);
				
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