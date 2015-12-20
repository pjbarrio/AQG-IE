package sample.generation.sskgm.significantPhrases.tokenizer;

import utils.word.extraction.WordValidator;

import com.aliasi.tokenizer.ModifyTokenTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

public class ValidWordTokenizer extends ModifyTokenTokenizerFactory {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8617648392427969727L;
	private WordValidator wv;

	public ValidWordTokenizer(TokenizerFactory factory) {
		super(factory);
		wv = new WordValidator();
	}

	@Override
    public String modifyToken(String token) {
        
		return wv.isValid(token)
            ? token
            : null;
    }
	
}
