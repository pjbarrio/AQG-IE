package searcher.lucene.demo;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Date;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import utils.FileAnalyzer;


/** Indexer for HTML files. */
public class IndexHTML {
  private static final int MAX_DOCS_PROCESSED = 100;
private File root;
private File index;
private boolean create;
private int processed;

  public IndexHTML(File root, File index, boolean create,String stopWords) {
	
	  this.root = root;
	  this.index = index;
	  this.create = create;
	  this.stopWords = new File(stopWords);
	  processed = 0;
  }

private boolean deleting = false;	  // true during deletion pass
  private IndexReader reader;		  // existing index
  private IndexWriter writer;		  // new index being built
  private TermEnum uidIter;		  // document id iterator
private File stopWords;

  /** Indexer for HTML files.*/
  /* Walk directory hierarchy in uid order, while keeping uid iterator from
  /* existing index in sync.  Mismatches indicate one of: (a) old documents to
  /* be deleted; (b) unchanged documents, to be left alone; or (c) new
  /* documents, to be indexed.
   */

  private void indexDocs(File file, File index, boolean create, String newName)
       throws Exception {
    if (!create) {				  // incrementally update

      reader = IndexReader.open(FSDirectory.open(index), false);		  // open existing index
      uidIter = reader.terms(new Term("uid", "")); // init uid iterator

      indexDocs(file,newName);

      if (deleting) {				  // delete rest of stale docs
        while (uidIter.term() != null && uidIter.term().field() == "uid") {
          System.out.println("deleting " +
              HTMLDocument.uid2url(uidIter.term().text()));
          reader.deleteDocuments(uidIter.term());
          uidIter.next();
        }
        deleting = false;
      }

      uidIter.close();				  // close uid iterator
      reader.close();				  // close existing index

    } else					  // don't have exisiting
      indexDocs(file,newName);
  }

  private void indexDocs(File file,String newName) throws Exception {
    
	System.out.println(file.getPath());
	  
	if (processed++==MAX_DOCS_PROCESSED){
		processed = 0;
		runGarbageCollector();
			
	}
	  
//	  if (file.getPath().equals("/proj/db/NoBackup/pjbarrio/sites/Movie/Empire/www.empireonline.com/features/50-worst-movies-ever/default.asp"))
//		  System.out.println("Here we are!");
	  
	  if (file.isDirectory()) {			  // if a directory
      String[] files = file.list();		  // list its files
      Arrays.sort(files);			  // sort the files
      for (int i = 0; i < files.length; i++)	  // recursively index them
        indexDocs(new File(file, files[i]),newName);

    } /*else if (file.getPath().endsWith(".html") || // index .html files
      file.getPath().endsWith(".htm") || // index .htm files
      file.getPath().endsWith(".txt")) { // index .txt files*/
    else if (FileAnalyzer.isSummarizable(file,newName)){


    	
      if (uidIter != null) {
        String uid = HTMLDocument.uid(file);	  // construct uid for doc

        while (uidIter.term() != null && uidIter.term().field() == "uid" &&
            uidIter.term().text().compareTo(uid) < 0) {
          if (deleting) {			  // delete stale docs
            System.out.println("deleting " +
                HTMLDocument.uid2url(uidIter.term().text()));
            reader.deleteDocuments(uidIter.term());
          }
          uidIter.next();
        }
        if (uidIter.term() != null && uidIter.term().field() == "uid" &&
            uidIter.term().text().compareTo(uid) == 0) {
          uidIter.next();			  // keep matching docs
        } else if (!deleting) {			  // add new docs
          Document doc = HTMLDocument.Document(file,newName);
          System.out.println("adding " + doc.get("path"));
          writer.addDocument(doc);
        }
      } else {					  // creating a new index
    	Document doc = HTMLDocument.Document(file,newName);
        System.out.println("adding " + doc.get("path"));
        writer.addDocument(doc);		  // add docs unconditionally
      }
    }
  }
  
	private void runGarbageCollector() {
		Runtime r = Runtime.getRuntime();
		System.out.println("cleanning");
		r.gc();
	
}

	public void start(String newName) throws Exception {
		
		Date start = new Date();
		
        if (!create) {				  // delete stale docs
	          deleting = true;
	          indexDocs(root, index, create,newName);
	    }
	    writer = new IndexWriter(FSDirectory.open(index), new StandardAnalyzer(Version.LUCENE_CURRENT,stopWords), create, 
	                                 new IndexWriter.MaxFieldLength(1000000));
	    indexDocs(root, index, create,newName);		  // add new docs

	    System.out.println("Optimizing index...");
	    writer.optimize();
	    writer.close();

	    Date end = new Date();

	    BufferedWriter bw = new BufferedWriter(new FileWriter(new File("outputTimes.txt"),true));
	    
	    bw.write("\n\n");
	    bw.write(new Long(end.getTime() - start.getTime()).toString());
	    bw.write(" total milliseconds for " + index.getAbsolutePath());

	    bw.close();
	    
	    runGarbageCollector();
	    
	} 
		

	
}
