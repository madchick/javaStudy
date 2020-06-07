
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.search.Explanation;

import org.apache.lucene.analysis.kr.KoreanAnalyzer;



public class SearcherTest 
{
    public static void main(String[] args) 
        throws Exception 
    {
    	File indexDir = new File("./data/dataIndex");
    	// File indexDir = new File("./data/dataIndex1");    // koreanAnalyzer.setBigrammable(false) ;
    	// File indexDir = new File("./data/dataIndex2");    // koreanAnalyzer.setBigrammable(true) ;
        // String q = "설명하긴힘들듯, 최고의학자. 들이글자를초등학생에게어떻게, 그만든원리를판단그당시설명할수있겠습니까.. 주위의, 나보다현명하다고생각되는사람들의정답일냉정한경우가대체로판단이객관적인야속하다싶을말해도많은많습니다. 예물로사람의정도로의견이거의옳습니다." ;
        String q = "세금을 내는 변호사들은 멍청한 사람들이여.. 왜냐하면 세금은 안 내도 되는 거거덩.." ;

        if (!indexDir.exists() || !indexDir.isDirectory()) 
        {
              throw new Exception(indexDir + " does not exist or is not a directory.");
        }

        search(indexDir, q);
    }

    public static void search(File indexDir, String q)
        throws Exception 
    {
        IndexReader reader = IndexReader.open(FSDirectory.open(indexDir), true);
        Searcher searcher = new IndexSearcher(reader);

        
        
        KoreanAnalyzer koreanAnalyzer = new KoreanAnalyzer();
        koreanAnalyzer.setBigrammable(false) ;
        
        QueryParser queryParser = new QueryParser(Version.LUCENE_CURRENT, "contents", koreanAnalyzer) ;

        // String searchText = q ;
        String searchText = "contents:" + q + " OR filename:" + q ;
        Query query = queryParser.parse(searchText);
        
        long start = new Date().getTime();
        TopDocs docs = searcher.search(query, 10000);
        long end = new Date().getTime();        
        
        
        
        System.out.println("Found " + docs.totalHits +
            " document(s) (in " + (end - start) +
            " milliseconds) that matched query '" +
            q + "' : \n");
    

        
        ScoreDoc[] scores = docs.scoreDocs;
        
        System.out.println("중복문항 검색결과 : " + docs.scoreDocs.length + "개");

        for (int i=0 ;i<docs.scoreDocs.length;i++)
        {            
            Document doc = searcher.doc(scores[i].doc);
            float docScore = scores[i].score ;
            // if(docScore>0.35)
            {
                System.out.println("유사도 : " + String.format("%.2f", docScore*100) + "% - " + doc.get("filename"));
                
            	Explanation explanation = searcher.explain(query,scores[i].doc) ;
                // System.out.println("유사도 : " + String.format("%.2f", docScore*100) + "% - " + doc.get("filename")
                //     + "\n" + explanation.toString());
            	
            	List<String> resultFieldName = getMatchedFieldName(explanation.toString()) ;
            	for(int iIndex=0 ; iIndex<resultFieldName.size() ; iIndex++)
            		System.out.println(resultFieldName.get(iIndex)) ;
            }
        }


        
        System.out.println("\n전문검색 결과 : " + docs.scoreDocs.length + "개");        

        for (int i=0 ;i<docs.scoreDocs.length;i++)
        {            
            Document doc = searcher.doc(scores[i].doc);
            float docScore = scores[i].score ;
            System.out.println(doc.get("filename") + " - " + String.format("%.4f", docScore));
        }

        
        
        end = new Date().getTime();
        System.out.println("\n\nFound " + docs.totalHits +
                " document(s) (in " + (end - start) +
                " milliseconds) that matched query '" +
                q + "' : \n");        
    }
    
    private static List<String> getMatchedFieldName(String s)
    {
    	List<String> resultFieldName = getMatchedFieldName(s,"(MATCH) weight(") ;
    	
    	if(resultFieldName.size()==0)
    		resultFieldName = getMatchedFieldName(s,"(MATCH) fieldWeight(") ;
    	
    	return resultFieldName ;
    }
    
    private static List<String> getMatchedFieldName(String s, String matchPatern)
    {
    	List<String> resultFieldName = new ArrayList<String>() ;
    	
    	int iIndex = 0 ;
    	while(iIndex != -1)
    	{
    	    iIndex = s.indexOf(matchPatern,iIndex) ;
    	    if(iIndex != -1)
    	    {
    	    	iIndex += matchPatern.length() ;
    	    	int iEndIndex = s.indexOf(":",iIndex) ;
    	    	if(iEndIndex != -1)
    	    	{
    	    		String fieldName = s.substring(iIndex,iEndIndex) ;
    	    		if(!isHasSameName(resultFieldName,fieldName))
    	    		    resultFieldName.add(fieldName) ;
    	    	}
    	    }
    	}
    	
    	return resultFieldName ;
    }
    
    private static boolean isHasSameName(List<String> nameList, String name)
    {
    	for(int iIndex=0 ; iIndex<nameList.size() ; iIndex++)
    	{
    		if(nameList.get(iIndex).compareTo(name)==0)
    			return true ;
    	}
    	
    	return false ;
    }
}



