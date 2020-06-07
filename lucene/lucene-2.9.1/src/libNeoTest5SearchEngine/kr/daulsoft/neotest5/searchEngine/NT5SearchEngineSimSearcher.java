package kr.daulsoft.neotest5.searchEngine;



import java.io.File;
import java.util.List;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import org.apache.lucene.analysis.kr.KoreanAnalyzer;



public class NT5SearchEngineSimSearcher {
	File indexDir ;
	boolean bigramable = false ;
	
	
	
	public void setIndexDataPath(String strPath)
	{
		indexDir = new File(strPath);
	}
	
	public void setBigramable(boolean setBigrammable)
	{
		bigramable = setBigrammable ;
	}
	
    public List<NT5SearchEngineSearchResult> search(String searchData) throws Exception
    {
        IndexReader reader = IndexReader.open(FSDirectory.open(indexDir), true);
        Searcher searcher = new IndexSearcher(reader);

        KoreanAnalyzer koreanAnalyzer = new KoreanAnalyzer();
        koreanAnalyzer.setBigrammable(bigramable) ;
        QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, "allContents", koreanAnalyzer) ;
        
        
        
        // 루씬 쿼리문법에 사용되는 문자 치환 처리 : +, -, :, (, ), ?, *, ., ~, ", ', {, }, [, ], ^ - 공백으로 치환..
        searchData = searchData.replace('+', ' ') ;
        searchData = searchData.replace('-', ' ') ;
        searchData = searchData.replace(':', ' ') ;
        searchData = searchData.replace('(', ' ') ;
        searchData = searchData.replace(')', ' ') ;
        searchData = searchData.replace('?', ' ') ;
        searchData = searchData.replace('*', ' ') ;
        searchData = searchData.replace('.', ' ') ;
        searchData = searchData.replace('~', ' ') ;
        searchData = searchData.replace('"', ' ') ;
        searchData = searchData.replace('\'', ' ') ;
        searchData = searchData.replace('{', ' ') ;
        searchData = searchData.replace('}', ' ') ;
        searchData = searchData.replace('[', ' ') ;
        searchData = searchData.replace(']', ' ') ;
        searchData = searchData.replace('^', ' ') ;
        
        
        
        Query query = parser.parse(searchData);
        TopDocs docs = searcher.search(query, 50);
        ScoreDoc[] scores = docs.scoreDocs;
        
       

        // System.out.println("\n\n" + searchData);
        
        List<NT5SearchEngineSearchResult> searchResults = new ArrayList<NT5SearchEngineSearchResult>() ;
        for (int i=0 ; i<docs.scoreDocs.length ; i++)
        {
            Document doc = searcher.doc(scores[i].doc);
            double docScore = (double)scores[i].score ;
            // System.out.println(doc.get("uniqNo") + " - " + String.format("%.4f", docScore));
            
        	Explanation explanation = searcher.explain(query,scores[i].doc) ;
        	List<String> resultFieldName = getMatchedFieldName(explanation.toString()) ;
        	// System.out.println(explanation.toString());

        	NT5SearchEngineSearchResult searchResult = new NT5SearchEngineSearchResult() ;
        	searchResult.uniqExamNo = (String)doc.get("uniqNo") ;
            searchResult.matchScore = docScore / resultFieldName.size() ;
            if(searchResult.matchScore>1)
            	searchResult.matchScore = 0.9999 ;
        	searchResult.matchFieldNames = resultFieldName ;
        	
        	searchResults.add(searchResult) ;
        }
        
        return searchResults ;
    }
    
    // Todo : 검색된 결과가 어느 필드에 속하는지 알아내는 방법은 차후에 루씬을 더 공부해서 보다 우아한 방법으로 수정해야 함..
    private List<String> getMatchedFieldName(String s)
    {
    	List<String> resultFieldName = getMatchedFieldName(s,"(MATCH) weight(") ;
    	
    	if(resultFieldName.size()==0)
    		resultFieldName = getMatchedFieldName(s,"(MATCH) fieldWeight(") ;
    	
    	return resultFieldName ;
    }
    
    private List<String> getMatchedFieldName(String s, String matchPatern)
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
    
    private boolean isHasSameName(List<String> nameList, String name)
    {
    	for(int iIndex=0 ; iIndex<nameList.size() ; iIndex++)
    	{
    		if(nameList.get(iIndex).compareTo(name)==0)
    			return true ;
    	}
    	
    	return false ;
    }	
}
