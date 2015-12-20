package online.navigation.generation;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import exploration.model.Database;
import exploration.model.Document;

import searcher.interaction.Interaction;
import searcher.interaction.factory.InteractionFactory;
import searcher.interaction.formHandler.TextQuery;
import utils.persistence.databaseWriter;

public class DocumentsToAnalyzeRetrieval_Second {

	public class Searcher implements Runnable{

		private static final int ATTEMPT_NUMBER = 3;
		private String encoding;
		private Database website;
		private String formFile;
		private List<String> words;
		private String inputNameFiles;
		private String outputFile;

		public Searcher(String encoding, Database website, List<String> words, String formFile, String inputNamesFile, String outputFile){
			this.encoding = encoding;
			this.website = website;
			this.words = words;
			this.formFile = formFile;
			this.inputNameFiles = inputNamesFile;
			this.outputFile = outputFile;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			try {
				submitQuery(encoding, website, words, formFile, inputNameFiles, outputFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		private void submitQuery(String encoding, Database website, List<String> words, String formFile, String inputNamesFile, String outputFile) throws IOException {
			
			List<String> inputNames = new ArrayList<String>();

			BufferedReader input = new BufferedReader(new FileReader(new File(inputNamesFile)));
			
			String line;
			
			while ((line=input.readLine())!=null){
				inputNames.add(line);
			}
			
			input.close();
			
			Interaction interaction = InteractionFactory.generateInstance(website, formFile, encoding, inputNames);
			
			TextQuery texQuery = new TextQuery(words);
			
			Document rd = null;
			
			int attempts = 0;
			
			while (rd == null && attempts<ATTEMPT_NUMBER){
			
				rd = interaction.submitQuery(texQuery);
			
				attempts++;
				
			}
			
			if (rd != null){
			
				BufferedReader br = new BufferedReader(rd);
				
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputFile)));
				
				while ((line=br.readLine())!=null){
					bw.write(line);
					bw.newLine();
				}
				
				br.close();
	
				bw.close();

			}
				
		}
	}

	private static Hashtable<Integer, Database> indexTable;
	
	
	private static databaseWriter dW;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		dW = new databaseWriter("");
		
//		hardCodedQueries();
		
		tfIdfQuery();
	
	}

	private static void tfIdfQuery() throws IOException {
		
		boolean real = true;
		
		String type = "alltypes";//"smalltraining"; //"training","validation"
		
		String inputNamePrefix = "/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalSelection/FinalInputNames/";

		String formPrefix = "/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalSelection/FinalForms/";

		String databaseIndex = "/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalDataIndex.txt";

//		String alreadyDir = "/local/pjbarrio/Files/Projects/AutomaticQueryGeneration/Forms/FinalSelection/AntiTFIDF/";
		
		String resultsPrefix = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/tfidf/QueryResult/" + type + "/";

		String tfidffolder = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/tfidf/" + type + "/";
		
		int offset = 0;
		
		int numberOfQueries = 50;
		
		loadDatabaseIndex(databaseIndex,dW);

		int[] files = loadFiles(new File("/proj/dbNoBackup/pjbarrio/Experiments/Wrappers/"+type));

		for (int j = 0; j < files.length; j++) {

			int i = files[j];

			new File(resultsPrefix,Integer.toString(i)).mkdir();
			
			String website = null;
			
			Database db = null;
			
			synchronized (indexTable) {
				
				db = indexTable.get(i);
				
			}
			

			String formFile = formPrefix + i + ".html";

			if (!(new File(formFile).exists()))
				continue;

			System.out.println("Processing..." + i);

			String inputNameFiles = inputNamePrefix + i + ".txt";

			String encoding = "UTF-8";

			List<String> queries = loadQueries(new File(tfidffolder + i + ".txt"));
			
//			if (!real){
			for (int k = 0; k < offset && queries.size() > 0; k++) {
				queries.remove(0); //clean the ones that were already retrieved
			}
//			}
			
			List<String> words = new ArrayList<String>();
			
			Set<Integer> already = new HashSet<Integer>();
			
			for (int in = offset; in < numberOfQueries && already.size()<queries.size(); in++) {
				
				words.clear();
				
				int k = in;
				
				if (!real){
					k = (int) Math.round(Math.random()*Math.min(100, queries.size()-1));
					while (already.contains(k)){
						k = (int) Math.round(Math.random()*Math.min(100, queries.size()-1));
					}
					already.add(k);
				}else{
					already.add(k);
				}
				
				words.add(queries.get(k));
				
				String outputFile;
				
				if (real){
					
					outputFile = resultsPrefix + i + "/" + (k) + ".html";;

				}else{
				
					outputFile = resultsPrefix + i + "/" + (k+numberOfQueries) + ".html";
					
				}
				
				if (new File(outputFile).exists())
					continue;
				
				new DocumentsToAnalyzeRetrieval_Second().submitQuery(encoding,db,words,formFile,inputNameFiles,outputFile);

				
			}


		}

		
	}

	private static int[] loadFiles(File file) throws IOException {
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String line;
		
		List<Integer> li = new ArrayList<Integer>();
		
		while ((line = br.readLine())!=null){
			
			li.add(Integer.valueOf(line));
			
		}
		
		br.close();
		
		int[] ret = new int[li.size()];
		
		for (int i = 0; i < ret.length; i++) {
			ret[i] = li.get(i);
		}
		
		return ret;
	}

	private static List<String> loadQueries(File file) throws IOException {
		
		List<String> ret = new ArrayList<String>();
		
			
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String line;
		
		while ((line=br.readLine())!=null){
			
			String[] l = line.split(" ");
			
			ret.add(l[0]);
			
		}
		
		br.close();
		
		return ret;
		
	}
	
	private static void loadDatabaseIndex(String databaseIndex, databaseWriter dW) throws IOException {

		indexTable = new Hashtable<Integer, Database>();
		
		String line;
		
		BufferedReader br = new BufferedReader(new FileReader(databaseIndex));
		
		while ((line=br.readLine())!=null){
			
			int index = Integer.valueOf(line.substring(0,line.indexOf(',')));
			
			String website = line.substring(line.indexOf(',')+1);
			
			System.out.println(index + " - " + website);
			
			indexTable.put(index, dW.getDatabase(index));
		}

		br.close();
		
	}

	private void submitQuery(String encoding, Database website,
			List<String> words, String formFile, String inputNameFiles,
			String outputFile) {
		
		Thread t = new Thread(new Searcher(encoding, website, words, formFile, inputNameFiles, outputFile));
		
		t.start();
		
		try {
			t.join(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	

}
