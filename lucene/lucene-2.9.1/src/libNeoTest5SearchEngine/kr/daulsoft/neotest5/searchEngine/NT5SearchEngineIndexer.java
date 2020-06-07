package kr.daulsoft.neotest5.searchEngine;



import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.io.FileNotFoundException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;

import org.apache.lucene.analysis.kr.KoreanAnalyzer;



public class NT5SearchEngineIndexer 
{
	File indexDir ;
	
	public void setIndexDataPath(String strPath)
	{
		indexDir = new File(strPath);
	}
	
    public void indexDoc(int itemKey, Hashtable<String,String> indexData) throws IOException
    {
        if (!indexDir.exists() || !indexDir.isDirectory()) 
        {
            throw new IOException(indexDir + " does not exist or is not a directory");
        }
        
        
        
        deleteDoc(itemKey) ;
        
        
        
        IndexWriter writer ;
        KoreanAnalyzer koreanAnalyzer = new KoreanAnalyzer();
        koreanAnalyzer.setBigrammable(true) ;         
        try
        {
            writer = new IndexWriter(new NIOFSDirectory(indexDir),
        	    new KoreanAnalyzer(),
        		false, IndexWriter.MaxFieldLength.UNLIMITED) ;
        }
        catch(FileNotFoundException e)
        {
            writer = new IndexWriter(new NIOFSDirectory(indexDir),
            	koreanAnalyzer,
            	true, IndexWriter.MaxFieldLength.UNLIMITED) ;        	
        }
        
        
        
        Document doc = new Document();
        
        String itemKeyString = Integer.toString(itemKey) ;
        Field uniqNo = new Field("uniqNo", itemKeyString, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO) ;
	    doc.add(uniqNo);        

	    String allContentsData = "" ;
	    java.util.Iterator<String> iterator = indexData.keySet().iterator() ;
        while(iterator.hasNext())
        {
        	String keyName = (String)iterator.next() ;
        	String contentsData = (String)indexData.get(keyName) ;
        	allContentsData += " " + contentsData ;
	        Field contents = new Field(keyName, contentsData, Field.Store.YES, Field.Index.ANALYZED) ;
	        // contents.setBoost(1.2f) ;
	        doc.add(contents);
        }
        
        Field allContents = new Field("allContents", allContentsData, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO) ;
	    doc.add(allContents);           

     
    
        writer.addDocument(doc);        
        writer.optimize();
        writer.close();    	
    }
    
    public void deleteDoc(int itemKey) throws IOException
    {
        if (!indexDir.exists() || !indexDir.isDirectory()) 
        {
            throw new IOException(indexDir + " does not exist or is not a directory");
        }
        
        try 
        {
    	    IndexReader reader = IndexReader.open(FSDirectory.open(indexDir), false) ;
    	    reader.deleteDocuments(new Term("uniqNo", Integer.toString(itemKey))) ;
    	    reader.close() ;
        }
        catch(FileNotFoundException e)
        {
        	// throw new IOException(indexDir + " can not open index file");
        }        
    }
}



