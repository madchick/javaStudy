package kr.daulsoft.neotest5.searchEngine;



import java.io.File;
import java.util.Hashtable;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.TermQuery;



public class NT5SearchEngineDocDuplicator 
{
	File indexDir ;
	String indexPath ;
	
	
	
	public void setIndexDataPath(String strPath)
	{
		indexDir = new File(strPath);
		indexPath = strPath ;
	}
	
	public void docDuplicate(int itemKey,int duplicatedItemKey,List<String> fieldNames) throws Exception
	{
        IndexReader reader = IndexReader.open(FSDirectory.open(indexDir), true);
        Searcher searcher = new IndexSearcher(reader);

        Query query = new TermQuery(new Term("uniqNo", Integer.toString(itemKey))) ;
        TopDocs docs = searcher.search(query, 100000);
        ScoreDoc[] scores = docs.scoreDocs;  
        Document doc = searcher.doc(scores[0].doc);
        
    	Hashtable<String,String> hashTable ;	
    	hashTable = new Hashtable<String,String>() ;      
    	for(int i=0 ; i<fieldNames.size() ; i++)
    	{
    		String fieldName = fieldNames.get(i) ;
    		hashTable.put(fieldName,doc.get(fieldName)) ;    		
    	}
    	
    	NT5SearchEngineIndexer indexer = new NT5SearchEngineIndexer() ;
    	indexer.setIndexDataPath(indexPath) ;
    	
    	indexer.indexDoc(duplicatedItemKey, hashTable) ;
    }	
}



