package kr.daulsoft.neotest5.searchEngine.unitTest;



import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.text.DecimalFormat; 

import kr.daulsoft.neotest5.searchEngine.*;



public class NT5SearchEngineSearcherTest 
{
    public static void main(String[] args) throws Exception
    {
    	NT5SearchEngineSearcher searcher = new NT5SearchEngineSearcher() ;
    	searcher.setIndexDataPath("./data/dataIndex") ;
    	searcher.setBigramable(false) ;
    	
    	
    	
    	List<NT5SearchEngineSearchResult> searchResults ;
    	List<String> fieldNames = new ArrayList<String>() ;
    	fieldNames.add("content") ;
    	fieldNames.add("example") ;
    	fieldNames.add("keyword") ;
    	
    	searchResults = searcher.search(fieldNames,"대한민국") ;
    	System.out.println("\n\n대한민국 검색 결과"); 
        printResult(searchResults) ;
    	
    	searchResults = searcher.search(fieldNames,"해설") ;
    	System.out.println("\n\n해설 검색 결과");
    	printResult(searchResults) ;
    	
    	searchResults = searcher.search(fieldNames,"효설") ;
    	System.out.println("\n\n효설 검색 결과");
    	printResult(searchResults) ;    	
    	
    	Hashtable<String,String> hashTable1 = new Hashtable<String,String>() ;
    	hashTable1.put("content", "미국") ;
    	searchResults = searcher.search(hashTable1) ;
    	System.out.println("\n\n미국 검색 결과");
    	printResult(searchResults) ;
    	
    	Hashtable<String,String> hashTable2 = new Hashtable<String,String>() ;
    	hashTable2.put("content", "효설") ;
    	hashTable2.put("keyword", "키워드") ;
    	searchResults = searcher.search(hashTable2) ;
    	System.out.println("\n\n효설, 키워드 검색 결과");
    	printResult(searchResults) ;
    	
    	
    	
    	NT5SearchEngineIndexer indexer = new NT5SearchEngineIndexer() ;
    	indexer.setIndexDataPath("./data/dataIndex") ;
    	indexer.deleteDoc(1) ;
    	System.out.println("\n\n3번 문항 삭제..");
    	
    	
    	
    	searchResults = searcher.search(fieldNames,"대한민국") ;
    	System.out.println("\n\n대한민국  검색 결과"); 
        printResult(searchResults) ;
    	
    	searchResults = searcher.search(fieldNames,"해설") ;
    	System.out.println("\n\n해설 검색 결과");
    	printResult(searchResults) ;
    	
    	searchResults = searcher.search(fieldNames,"효설") ;
    	System.out.println("\n\n효설 검색 결과");
    	printResult(searchResults) ;    	
    	
    	searchResults = searcher.search(hashTable1) ;
    	System.out.println("\n\n미국 검색 결과");
    	printResult(searchResults) ;
    	
    	searchResults = searcher.search(hashTable2) ;
    	System.out.println("\n\n효설, 키워드 검색 결과");
    	printResult(searchResults) ;
    	
    	
    	
    	searcher.setBigramable(true) ;
    	
    	List<NT5SearchEngineSearchResult> searchResults2 ;
    	List<String> fieldNames2 = new ArrayList<String>() ;
    	fieldNames2.add("header_text") ;
    	fieldNames2.add("title_text") ;
    	fieldNames2.add("ans_text") ;
    	fieldNames2.add("comment_text") ;
    	
    	searchResults2 = searcher.search(fieldNames2,"다음은 파킨슨병의") ;
    	System.out.println("\n\n다음은 파킨슨병의 검색 결과"); 
        printResult(searchResults2) ;    	
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



