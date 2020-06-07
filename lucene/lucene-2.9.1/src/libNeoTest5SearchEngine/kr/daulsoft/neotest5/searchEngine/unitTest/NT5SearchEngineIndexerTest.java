package kr.daulsoft.neotest5.searchEngine.unitTest;



import java.util.Hashtable;

import kr.daulsoft.neotest5.searchEngine.*;



public class NT5SearchEngineIndexerTest
{
    public static void main(String[] args) throws Exception
    {
    	NT5SearchEngineIndexer indexer = new NT5SearchEngineIndexer() ;
    	indexer.setIndexDataPath("./data/dataIndex") ;
    	
    	
    	
    	Hashtable<String,String> hashTable ;
    	
    	hashTable = new Hashtable<String,String>() ;
    	hashTable.put("content", "미국은 민주주의 국가입니다.") ;
    	hashTable.put("example", "효설") ;
    	hashTable.put("keyword", "코워드") ;
    	
    	indexer.indexDoc(1, hashTable) ;
    	
    	
    	 
    	hashTable = new Hashtable<String,String>() ;
    	hashTable.put("content", "무궁화 꽃이 피었습니다.") ;
    	hashTable.put("example", "해설 두번째") ;
    	hashTable.put("keyword", "키워드 두번째") ;
    	
    	indexer.indexDoc(2, hashTable) ;
    	
    	
    	
    	hashTable = new Hashtable<String,String>() ;
    	hashTable.put("content", "테스트용 콘텐츠 입니다.") ;
    	hashTable.put("example", "효설") ;
    	hashTable.put("keyword", "효설") ;
    	
    	indexer.indexDoc(3, hashTable) ;
    	
    	
    	
    	System.out.println("\n\n인덱스 완료~ !!\n\n");
    } 
}



