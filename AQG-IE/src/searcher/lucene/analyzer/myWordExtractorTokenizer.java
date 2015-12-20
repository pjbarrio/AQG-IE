package searcher.lucene.analyzer;
import java.io.BufferedReader;
import java.io.IOException;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

import utils.word.extraction.WordExtractorAbs;

/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.4.1
 * on 9/4/08 6:49 PM from the specification file
 * <tt>/tango/mike/src/lucene.standarddigit/src/java/org/apache/lucene/analysis/standard/StandardTokenizerImpl.jflex</tt>
 */
class myWordExtractorTokenizer {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;
  private static final int TOKEN = 1;

  /** the input device */
  private java.io.Reader reader;

private WordExtractorAbs we;

private String[] tokens;

private int tokenIndex;
private String lastToken;
private String text;
private int index;

public final int yychar()
{
    int aux = index;
    
    index = lastToken.length() + 1;
    
	return aux;
}

/**
 * Fills Lucene token with the current token text.
 */
final void getText(Token t) {
	t.setTermBuffer(lastToken);
//	t.setTermBuffer(zzBuffer, zzStartRead, zzMarkedPos-zzStartRead);
}

/**
 * Fills TermAttribute with the current token text.
 */
final void getText(TermAttribute t) {
  
	t.setTermBuffer(lastToken);
//	t.setTermBuffer(zzBuffer, zzStartRead, zzMarkedPos-zzStartRead);
}


  /**
   * Creates a new scanner
   * There is also a java.io.InputStream version of this constructor.
   *
   * @param   in  the java.io.Reader to read input from.
 * @param unique 
 * @param stemmed 
 * @param lowercase 
   */
	myWordExtractorTokenizer(java.io.Reader in, WordExtractorAbs we, boolean unique, boolean lowercase, boolean stemmed) {
    this.reader = in;
    this.we = we;
    try {
		init(unique,lowercase,stemmed);
	} catch (IOException e) {
		e.printStackTrace();
	}
  }

  private void init(boolean unique, boolean lowercase, boolean stemmed) throws IOException {
	text = "";
	
	BufferedReader br = new BufferedReader(reader);
	
	String line = br.readLine();
	
	while (line != null){
		
		text += line;
		
		line = br.readLine();
	}
	
	br.close();
	
	tokens = we.getWords(text, unique, lowercase, stemmed);
	
	tokenIndex = 0;
	
	index = 0;
	
}

/**
   * Creates a new scanner.
   * There is also java.io.Reader version of this constructor.
   *
   * @param   in  the java.io.Inputstream to read input from.
 * @param unique 
 * @param lowercase 
 * @param stemmed 
   */
	myWordExtractorTokenizer(java.io.InputStream in, WordExtractorAbs we, boolean unique, boolean lowercase, boolean stemmed) {
    this(new java.io.InputStreamReader(in),we,unique,lowercase,stemmed);
  }

  /**
   * Closes the input stream.
   */
  public final void yyclose() throws java.io.IOException {
    if (reader != null)
      reader.close();
  }


  /**
   * Resets the scanner to read from a new input stream.
   * Does not close the old reader.
   *
   * All internal variables are reset, the old input stream 
   * <b>cannot</b> be reused (internal buffer is discarded and lost).
   * Lexical state is set to <tt>ZZ_INITIAL</tt>.
   *
   * @param reader   the new input stream 
   */
  public final void yyreset(java.io.Reader reader) {
    this.reader = reader;
    tokenIndex = 0;
    lastToken = null;
    index = 0;
  
  }

  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    
	  return lastToken.length();

  }

  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public int getNextToken() throws java.io.IOException {

	  
	  
	  if (thereAreMoreTokens()){
		  lastToken = tokens[tokenIndex];
		  tokenIndex++;
		  return TOKEN;
	  }
	  else{
		  tokens = new String[0];
		  return YYEOF;
	  }
}

private boolean thereAreMoreTokens() {
	return tokenIndex < tokens.length;
}


}