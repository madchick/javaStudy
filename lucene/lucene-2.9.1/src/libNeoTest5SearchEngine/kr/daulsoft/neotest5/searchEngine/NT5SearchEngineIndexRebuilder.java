package kr.daulsoft.neotest5.searchEngine;



import java.io.*;
import java.util.Date;
import java.util.Hashtable;



public class NT5SearchEngineIndexRebuilder 
{
    public static void main(String[] args) 
	    throws Exception 
	{
        long start = new Date().getTime();
        
    	String dataPath = "./data/dataIndex/textData" ;
    	File indexDir = new File(dataPath);
    	int rebuildCount = indexDirectory(indexDir) ;
    	
        long end = new Date().getTime();    	
    	
    	System.out.println("\n\nIndex Rebuild 완료 (" + rebuildCount + "개 문항, " + (end - start) + " milliseconds)\n\n");
	}
    
    private static int indexDirectory(File dir)
        throws IOException 
	{
		System.out.println("\nIndexing " + dir.getCanonicalPath() + " 시작..");
	
		File[] files = dir.listFiles();
	
		int rebuildCount = 0 ;
	    for (int i = 0; i < files.length; i++) 
	    {
	        File f = files[i];
            indexFile(f);
            
            rebuildCount++ ;
	    }
	    
	    return rebuildCount ;
	}
    
    private static void indexFile(File f)
	    throws IOException 
	{
    	String examNo = f.getName();
    	File[] files = f.listFiles();
    	
	    try
	    {
	    	Hashtable<String,String> hashTable ;
	    	hashTable = new Hashtable<String,String>() ;
	    	
	    	System.out.println("\n문항번호 : " + examNo) ;
		    for (int i = 0; i < files.length; i++) 
		    {
		        File dataFile = files[i];
		        
		        if(dataFile.getName().equals("header_text"))
		        {
		            System.out.println(dataFile.getName()) ;
		            String dataString = readFromFile(f.getCanonicalPath() + "\\" + dataFile.getName()) ;
		            hashTable.put("header_text", dataString) ;
		        }
		        
		        if(dataFile.getName().equals("title_text"))
		        {
		            System.out.println(dataFile.getName()) ;
		            String dataString = readFromFile(f.getCanonicalPath() + "\\" + dataFile.getName()) ;
		            hashTable.put("title_text", dataString) ;
		        }
		        
		        if(dataFile.getName().equals("ans_text"))
		        {
		            System.out.println(dataFile.getName()) ;
		            String dataString = readFromFile(f.getCanonicalPath() + "\\" + dataFile.getName()) ;
		            hashTable.put("ans_text", dataString) ;
		        }
		        
		        if(dataFile.getName().equals("comment_text"))
		        {
		            System.out.println(dataFile.getName()) ;
		            String dataString = readFromFile(f.getCanonicalPath() + "\\" + dataFile.getName()) ;
		            hashTable.put("comment_text", dataString) ;
		        }
		    }
		    
	    	NT5SearchEngineIndexer indexer = new NT5SearchEngineIndexer() ;
	    	indexer.setIndexDataPath("./data/dataIndex") ;

	    	indexer.indexDoc(Integer.parseInt(examNo), hashTable) ;		    
	    }
	    catch(Exception e)
	    {
	    	System.out.println("Indexing " + f.getCanonicalPath() + " - 실패");
	    }
	}
    
    private static String readFromFile(String fileName)
        throws IOException
    {
	    BufferedReader in = new BufferedReader(new FileReader(fileName));
	    String s = "" ;
	
	    String temp ;
	    while ((temp = in.readLine()) != null) 
	    {
	    	s += temp ;
	    }
	    in.close();
	    
	    return s ;
    }
}
