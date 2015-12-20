package searcher.lucene.indexer;

import java.io.File;

import searcher.lucene.demo.IndexHTML;
import utils.word.extraction.WordExtractor;





public class LuceneIndexer {
	
	public static final WordExtractor we = new WordExtractor();
	
	public static final boolean unique = false;
	public static final boolean lowercase = true;
	public static final boolean stemmed = false;

	public void Index(String database, String IndexFile,boolean isNew, String stopWords,String newName){
	    
	        File index = new File(IndexFile);
	        boolean create = isNew;
	        File root = new File(database);

	        IndexHTML ihtml = new IndexHTML(root,index,create,stopWords);
	        
	        try {
				ihtml.start(newName);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}
