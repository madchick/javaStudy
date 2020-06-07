package org.apache.lucene.analysis.kr;


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

import org.apache.lucene.analysis.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Set;

/**
 * Filters {@link StandardTokenizer} with {@link StandardFilter}, {@link
 * LowerCaseFilter} and {@link StopFilter}, using a list of English stop words.
 *
 * @version $Id: KoreanAnalyzer.java,v 1.4 2009/12/22 01:48:56 smlee0818 Exp $
 */
public class KoreanAnalyzer extends Analyzer {
	
	  private Set stopSet;
	  
	  private boolean bigrammable = true;
	  
	  private boolean hasOrigin = true;

	  /** An array containing some common English words that are usually not
	  useful for searching. */
	  public static final String[] STOP_WORDS = StopAnalyzer.ENGLISH_STOP_WORDS;
	  
	  public static final String[] KOR_STOP_WORDS = new String[]{"이","그","저","것","수","등","들"};
	  
	  public static final String DIC_ENCODING = "UTF-8";


  /** Default maximum allowed token length */
    public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;
	  
	public KoreanAnalyzer() {
	    this(STOP_WORDS);
	}

	public KoreanAnalyzer(String[] stopWords) {

	    stopSet = StopFilter.makeStopSet(stopWords);
	    stopSet.addAll(
	    		StopFilter.makeStopSet(KOR_STOP_WORDS));
	    
	}

  /** Builds an analyzer with the stop words from the given file.
   * @see WordlistLoader#getWordSet(File)
   */
	public KoreanAnalyzer(File stopwords) throws IOException {
		InputStream is = new FileInputStream(stopwords);
        InputStreamReader reader = new InputStreamReader(is, DIC_ENCODING);		
        stopSet = WordlistLoader.getWordSet(reader);
	}

  /** Builds an analyzer with the stop words from the given file.
   * @see WordlistLoader#getWordSet(File)
   */
	public KoreanAnalyzer(File stopwords, String encoding) throws IOException {
		InputStream is = new FileInputStream(stopwords);
        InputStreamReader reader = new InputStreamReader(is, encoding);		
        stopSet = WordlistLoader.getWordSet(reader);
	}
		
	/** Builds an analyzer with the stop words from the given reader.
	 * @see WordlistLoader#getWordSet(Reader)
	*/
	public KoreanAnalyzer(Reader stopwords) throws IOException {
	   stopSet = WordlistLoader.getWordSet(stopwords);
	}

	public TokenStream tokenStream(String fieldName, Reader reader) {
		KoreanTokenizer tokenStream = new KoreanTokenizer(reader);
		TokenStream result = new KoreanFilter(tokenStream, bigrammable, hasOrigin);
	    result = new LowerCaseFilter(result);
	    result = new StopFilter(result, stopSet);
	    return result;	    
	}
	
	/**
	 * determine whether the bigram index term is returned or not if a input word is failed to analysis
	 * If true is set, the bigram index term is returned. If false is set, the bigram index term is not returned.
	 * @param is
	 */
	public void setBigrammable(boolean is) {
		bigrammable = is;
	}
	
	/**
	 * determin whether the original term is returned or not if a input word is analyzed morphically.
	 * @param has
	 */
	public void setHasOrigin(boolean has) {
		hasOrigin = has;
	}
}
