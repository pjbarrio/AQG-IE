package utils.word.extraction;

import java.io.IOException;
import java.util.Arrays;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.DummyContentExtractor;
import online.documentHandler.contentExtractor.impl.LynxOutputContentExtractor;
import online.documentHandler.contentLoader.ContentLoader;
import online.documentHandler.contentLoader.impl.LynxContentLoader;
import online.documentHandler.contentLoader.impl.SgmlContentLoader;
import utils.persistence.persistentWriter;
import exploration.model.Document;

public class WordExtractor extends WordExtractorAbs {

	protected ContentExtractor contentExtractor;
	protected ContentLoader contentLoader;
	private WordTokenizer wt;
	
	public WordExtractor(){
		
		this(new LynxOutputContentExtractor(),new LynxContentLoader());
		
	}
	
	public WordExtractor(ContentExtractor ce,ContentLoader cl) {
		
		super();
			
		contentExtractor = ce;
		contentLoader = cl;
		wt = new WordTokenizer();
	
	}

	@Override
	protected String[] _getWords(Document document,persistentWriter pW){
		
		document.getContent(contentLoader,pW); //caches it

		return extract(document, pW);
	
	}

	private String[] extract(Document document, persistentWriter pW) {
		
		return document.getWords(wt, contentExtractor, pW);
		
	}

	@Override
	protected String[] _getWords(String string) {
		return wt.getWords(string);
	}

	public static void main(String[] args) throws IOException {
		
//		String content = FileUtils.readFileToString(new File("/proj/db-files2/NoBackup/pjbarrio/Dataset/TREC/CleanCollection/tipster_vol_3/ap/ap900824-9"));
		
		String content = "pepsi’s vice president";
		
		WordExtractor we = new WordExtractor(new DummyContentExtractor(),new SgmlContentLoader());
		
		System.out.println(Arrays.toString(we.getWords(content,true,true,false)));
	
//		System.out.println(Arrays.toString(we.getWords(new File("/proj/db-files2/NoBackup/pjbarrio/Dataset/TREC/CleanCollection/tipster_vol_3/ap/ap900824-9"), "")));
		
	}
	
}
