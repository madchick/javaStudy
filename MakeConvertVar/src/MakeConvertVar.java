import java.io.File;

public class MakeConvertVar {
	static final int maxFileCount = 100;
	static final String initPath = "c:/temp";
	static final String varName = "examFileList";
	
	private static int getFileNames(File f,int[] listCount,int totalCount)
	{
		if(f.isDirectory())
		{
			String[] list = f.list();
			for(int i=0 ; i<list.length ; i++)
			{
				totalCount = getFileNames(new File(f,list[i]),listCount,totalCount);
			}
		}
		else
		{
			String fileName = f.getName() ;
			fileName = fileName.toLowerCase();
			if(fileName.endsWith(".hwp"))
			{
				totalCount++;
				String pathName = f.getPath();
				pathName = pathName.replace('\\','/') ;
				
				if((totalCount % maxFileCount)==0)
				{
					System.out.println("\"" + pathName + "\"\r\n];");
					
					System.out.println("");
					listCount[0]++;
					System.out.println("var " + varName + listCount[0] + " = [");					
				}
				else
					System.out.println("\"" + pathName + "\",");
			}			
		}
		
		return totalCount;
	}	
	
	public static void main(String[] ar)
	{
		int[] listCount = new int[1];
		listCount[0] = 1;
		System.out.println("var " + varName + listCount[0] + " = [");
		            
	    int totalCount = 0;
		File f = new File(initPath) ;
	    totalCount = getFileNames(f,listCount,totalCount);
	    
	    System.out.println("];\r\n\r\nTotal File Count : " + totalCount);
	}
}



