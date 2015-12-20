package utils.word.extraction;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WordTokenizer {

	private weka.core.tokenizers.WordTokenizer wt = new weka.core.tokenizers.WordTokenizer();
	
	private WordValidator wv = new WordValidator();
	
	public synchronized String[] getWords(String content){
		
		wt.tokenize(content);
		
		List<String> words = new ArrayList<String>();
		
		while (wt.hasMoreElements()){
			
			String word = ((String)wt.nextElement()).intern();
			
			if (wv.isValid(word))
				words.add(word);
			
		}
		
		return words.toArray(new String[words.size()]);
	
	}
	
//	private static Locale currentLocale = new Locale ("en","US");
//	
//	public static String[] getWords(String content){
//		
//		return extractWords(content).toArray(new String[0]);
//		
//	}
//	
//	private static ArrayList<String> extractWords(String target) {
//
//		BreakIterator wordIterator =
//		    BreakIterator.getWordInstance(currentLocale);
//		
//		ArrayList<String> words = new ArrayList<String>();
//		
//		wordIterator.setText(target);
//	    
//		int start = wordIterator.first();
//	    
//		int end = wordIterator.next();
//
//	    while (end != BreakIterator.DONE) {
//			String word = target.substring(start,end);
//			if (Character.isLetterOrDigit(word.charAt(0))) {
//			    words.add(word);
//			}
//			start = end;
//			end = wordIterator.next();
//	    }
//	    
//	    return words;
//	}
	
}
