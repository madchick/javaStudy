package kr.daulsoft.neotest5.searchEngine.unitTest;



import java.util.List;
import java.text.DecimalFormat; 

import kr.daulsoft.neotest5.searchEngine.*;



public class NT5SearchEngineSimSearcherTest 
{
    public static void main(String[] args) throws Exception
    {
    	NT5SearchEngineSimSearcher searcher = new NT5SearchEngineSimSearcher() ;
    	searcher.setIndexDataPath("./data/dataIndex") ;
    	searcher.setBigramable(false) ;
    	
    	
    	
    	List<NT5SearchEngineSearchResult> searchResults ;
    	searchResults = searcher.search("다음은 파킨슨병의 병인pathogenesis)과 관련된 그림이다.") ;
    	System.out.println("\n\n유사문항 검색 결과"); 
        printResult(searchResults) ;  	
    }
    
    private static void printResult(List<NT5SearchEngineSearchResult> searchResults)
    {
    	for(int i=0 ; i<searchResults.size() ; i++)
    	{
    		System.out.println("") ;
    		
    		NT5SearchEngineSearchResult searchResult = searchResults.get(i) ;
    		System.out.println("문항고유번호 : " + searchResult.uniqExamNo) ;
    		
    		String pattern = "###,###,###.##" ; 
    		DecimalFormat dformat = new DecimalFormat (pattern) ; 
    		System.out.println("유사율 : " + dformat.format(searchResult.matchScore*100) + "%") ;
    		// System.out.println("유사율 : " + searchResult.matchScore) ;
    		
    		for(int j=0 ; j<searchResult.matchFieldNames.size() ; j++)
    		    System.out.println("매치필드명 : " + searchResult.matchFieldNames.get(j)) ;
    	}    	
    }
}
