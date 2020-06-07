
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.NIOFSDirectory;

import org.apache.lucene.analysis.kr.KoreanAnalyzer;



public class IndexerTest
{
    public static void main(String[] args) 
        throws Exception 
    {
        File indexDir = new File("./data/dataIndex");
        File dataDir = new File("./data/dataText");

        long start = new Date().getTime();
        int numIndexed = index(indexDir, dataDir);
        long end = new Date().getTime();

        System.out.println("Indexing " + numIndexed + " files took "
            + (end - start) + " milliseconds");
    }

    public static int index(File indexDir, File dataDir) 
        throws IOException 
    {
        if (!dataDir.exists() || !dataDir.isDirectory()) 
        {
            throw new IOException(dataDir + " does not exist or is not a directory");
        }

        KoreanAnalyzer koreanAnalyzer = new KoreanAnalyzer();
        koreanAnalyzer.setBigrammable(false) ;  
        
        IndexWriter writer = new IndexWriter(new NIOFSDirectory(indexDir),
        		koreanAnalyzer,
        	    true, IndexWriter.MaxFieldLength.UNLIMITED) ;
        
        writer.setUseCompoundFile(false);

        indexDirectory(writer, dataDir);

        int numIndexed = writer.numDocs();
        writer.optimize();
        writer.close();
        return numIndexed;
    }

    private static void indexDirectory(IndexWriter writer, File dir)
        throws IOException 
    {
    	System.out.println("Indexing " + dir.getCanonicalPath() + " 시작..");

    	File[] files = dir.listFiles();

        for (int i = 0; i < files.length; i++) 
        {
            File f = files[i];
            if (f.isDirectory()) 
            {
                indexDirectory(writer, f);
            } 
            else if (f.getName().endsWith(".txt")) 
            {
                indexFile(writer, f);
            }
        }
    }

    private static void indexFile(IndexWriter writer, File f)
        throws IOException 
    {
        if (f.isHidden() || !f.exists() || !f.canRead()) 
        {
            return;
        }



        Document doc = new Document();
        
        try
        {
	        Field contents = new Field("contents", new FileReader(f)) ;
	        contents.setBoost(1.2f) ;
	        doc.add(contents);
	        
	        Field filename = new Field("filename", f.getCanonicalPath(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.NO) ;
	        filename.setBoost(1.0f);
	        doc.add(filename);
	        
	        writer.addDocument(doc);
        }
        catch(Exception e)
        {
        	System.out.println("Indexing " + f.getCanonicalPath() + " - 실패");
        }
    }
}



