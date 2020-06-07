package kr.daulsoft.neotest5.searchEngine.unitTest;



import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import kr.daulsoft.neotest5.searchEngine.*;



public class NT5SearchEngineDocDuplicatorTest
{
    public static void main(String[] args) throws Exception
    {
    	NT5SearchEngineDocDuplicator docDuplicator = new NT5SearchEngineDocDuplicator() ;
    	docDuplicator.setIndexDataPath("./data/dataIndex") ;
    	
    	List<String> fieldNames = new ArrayList<String>() ;
    	fieldNames.add("content") ;
    	fieldNames.add("example") ;
    	fieldNames.add("keyword") ;
    	
    	docDuplicator.docDuplicate(1,111,fieldNames) ;
    	
    	
    	
    	NT5SearchEngineSearcher searcher = new NT5SearchEngineSearcher() ;
    	searcher.setIndexDataPath("./data/dataIndex") ;
    	searcher.setBigramable(false) ;
    	
    	List<NT5SearchEngineSearchResult> searchResults ;
    	
    	searchResults = searcher.search(fieldNames,"민주주의") ;
    	System.out.println("\n\n민주주의 검색 결과");
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



