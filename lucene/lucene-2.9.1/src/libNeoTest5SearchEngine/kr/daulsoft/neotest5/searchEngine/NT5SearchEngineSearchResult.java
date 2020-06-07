package kr.daulsoft.neotest5.searchEngine;



import java.util.List;
import java.util.ArrayList;



public class NT5SearchEngineSearchResult
{
	public String uniqExamNo ;
	public double matchScore ;
	public List<String> matchFieldNames ;
	
	NT5SearchEngineSearchResult()
	{
		matchFieldNames = new ArrayList<String>() ;
	}
}
