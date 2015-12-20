package searcher.lucene.analyzer;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

import utils.word.extraction.WordExtractorAbs;

/**
 * Filters {@link StandardTokenizer} with {@link StandardFilter}, {@link
 * LowerCaseFilter} and {@link StopFilter}, using a list of
 * English stop words.
 *
 * <a name="version"/>
 * <p>You must specify the required {@link Version}
 * compatibility when creating StandardAnalyzer:
 * <ul>
 *   <li> As of 2.9, StopFilter preserves position
 *        increments
 *   <li> As of 2.4, Tokens incorrectly identified as acronyms
 *        are corrected (see <a href="https://issues.apache.org/jira/browse/LUCENE-1068">LUCENE-1608</a>
 * </ul>
 */
public class JavaSegmenterAnalyzer extends Analyzer {
  private Set<?> stopSet;

  /**
   * Specifies whether deprecated acronyms should be replaced with HOST type.
   * See {@linkplain https://issues.apache.org/jira/browse/LUCENE-1068}
   */
  private final boolean replaceInvalidAcronym,enableStopPositionIncrements;

  /** An unmodifiable set containing some common English words that are usually not
  useful for searching. */
  public static final Set<?> STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET; 
  private final Version matchVersion;

private WordExtractorAbs we;

private boolean unique;

private boolean lowercase;

private boolean stemmed;

  /** Builds an analyzer with the default stop words ({@link
   * #STOP_WORDS_SET}).
   * @param matchVersion Lucene version to match See {@link
   * <a href="#version">above</a>}
   */
  public JavaSegmenterAnalyzer(Version matchVersion, WordExtractorAbs we, boolean unique, boolean lowercase, boolean stemmed) {
    this(matchVersion, STOP_WORDS_SET,we,unique,lowercase,stemmed);
  }

  /** Builds an analyzer with the given stop words.
   * @param matchVersion Lucene version to match See {@link
   * <a href="#version">above</a>}
   * @param stopWords stop words */
  public JavaSegmenterAnalyzer(Version matchVersion, Set<?> stopWords, WordExtractorAbs we, boolean unique, boolean lowercase, boolean stemmed) {
    stopSet = stopWords;
    setOverridesTokenStreamMethod(JavaSegmenterAnalyzer.class);
    enableStopPositionIncrements = StopFilter.getEnablePositionIncrementsVersionDefault(matchVersion);
    replaceInvalidAcronym = matchVersion.onOrAfter(Version.LUCENE_24);
    this.matchVersion = matchVersion;
    this.we = we;
    this.unique=unique;
    this.lowercase = lowercase;
    this.stemmed = stemmed;
  }

  /** Builds an analyzer with the stop words from the given file.
   * @see WordlistLoader#getWordSet(File)
   * @param matchVersion Lucene version to match See {@link
   * <a href="#version">above</a>}
   * @param stopwords File to read stop words from */
  public JavaSegmenterAnalyzer(Version matchVersion, File stopwords, WordExtractorAbs we, boolean unique, boolean lowercase, boolean stemmed) throws IOException {
    this(matchVersion, WordlistLoader.getWordSet(stopwords),we,unique,lowercase,stemmed);
  }

  /** Builds an analyzer with the stop words from the given reader.
   * @see WordlistLoader#getWordSet(Reader)
   * @param matchVersion Lucene version to match See {@link
   * <a href="#version">above</a>}
   * @param stopwords Reader to read stop words from */
  public JavaSegmenterAnalyzer(Version matchVersion, Reader stopwords, WordExtractorAbs we, boolean unique, boolean lowercase, boolean stemmed) throws IOException {
    this(matchVersion, WordlistLoader.getWordSet(stopwords),we,unique,lowercase,stemmed);
  }

  /** Constructs a {@link StandardTokenizer} filtered by a {@link
  StandardFilter}, a {@link LowerCaseFilter} and a {@link StopFilter}. */
  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {
    myJavaTokenizer tokenStream = new myJavaTokenizer(matchVersion, reader,we,unique,lowercase,stemmed);
    tokenStream.setMaxTokenLength(maxTokenLength);
//    TokenStream result = new myJavaFilter(tokenStream);
    TokenStream result = new LowerCaseFilter(tokenStream);
    result = new StopFilter(enableStopPositionIncrements, result, stopSet);
    return result;
  }

  private static final class SavedStreams {
	myJavaTokenizer tokenStream;
    TokenStream filteredTokenStream;
  }

  /** Default maximum allowed token length */
  public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

  private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;

  /**
   * Set maximum allowed token length.  If a token is seen
   * that exceeds this length then it is discarded.  This
   * setting only takes effect the next time tokenStream or
   * reusableTokenStream is called.
   */
  public void setMaxTokenLength(int length) {
    maxTokenLength = length;
  }
    
  /**
   * @see #setMaxTokenLength
   */
  public int getMaxTokenLength() {
    return maxTokenLength;
  }

  @Override
  public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
	  
	  return tokenStream(fieldName, reader);
	  
//    if (overridesTokenStreamMethod) {
//      // LUCENE-1678: force fallback to tokenStream() if we
//      // have been subclassed and that subclass overrides
//      // tokenStream but not reusableTokenStream
//      return tokenStream(fieldName, reader);
//    }
//    SavedStreams streams = (SavedStreams) getPreviousTokenStream();
//    if (streams == null) {
//      streams = new SavedStreams();
//      setPreviousTokenStream(streams);
//      streams.tokenStream = new myJavaTokenizer(matchVersion, reader,we,unique,lowercase,stemmed);
////      streams.filteredTokenStream = new myJavaFilter(streams.tokenStream);
//      streams.filteredTokenStream = new LowerCaseFilter(streams.tokenStream);
//      streams.filteredTokenStream = new StopFilter(enableStopPositionIncrements,
//                                                   streams.filteredTokenStream, stopSet);
//    } else {
//      streams.tokenStream.reset(reader);
//    }
//    streams.tokenStream.setMaxTokenLength(maxTokenLength);
//    
//    streams.tokenStream.setReplaceInvalidAcronym(replaceInvalidAcronym);
//
//    return streams.filteredTokenStream;
  }
}
