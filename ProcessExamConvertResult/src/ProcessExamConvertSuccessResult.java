import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;



public class ProcessExamConvertSuccessResult {
	
	static String RESULT_FILE_PATH = "C:/temp/������ȯ ����.txt";
	static String RESULT_TARGET_FILE_PATH = "C:/temp/��ȯ���/���� ����/";
	
	
	
	public static void main(String[] args) throws IOException {
	    BufferedReader readBuffer = null;
		try {
			readBuffer = new BufferedReader(
			    new InputStreamReader(new FileInputStream(RESULT_FILE_PATH)));
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.out.println("���Ͽ��� ���� : " + RESULT_FILE_PATH);
			System.exit(-1);
		}

		int totalCopyFiles = 0 ;
	    String readedLine = "";
	    while((readedLine = readBuffer.readLine() ) != null){	    	
		    String[] columnDetail = readedLine.split("\\t");
		    
		    // ��λ���
		    String pathName = columnDetail[0] + "/" + columnDetail[1] + "/" 
		        + columnDetail[2] + "/" + columnDetail[3] + "/" + columnDetail[4];
		    String targetPath = RESULT_TARGET_FILE_PATH + removeInvalidCharacter(pathName);
		    createDirectory(targetPath);
		    
		    // ���Ϻ���
		    String sourceFilePath = removeInvalidCharacter(columnDetail[6]);
		    String targetFilePath = targetPath + "/" + columnDetail[5] + " " 
		        + makeTargetFileName(removeInvalidCharacter(columnDetail[6]));
		    copyFile(sourceFilePath,targetFilePath);
		    totalCopyFiles++;
	    }
	    System.out.println(totalCopyFiles + " files are copied.");
	}
	
	private static void createDirectory(String path)
	{
		String newPath = "" ;
		String[] pathNames = path.split("/");
		
		for(int i=0 ; i<pathNames.length ; i++)
		{
		    newPath = newPath + pathNames[i] + "/";

			File directory = new File(newPath);	
			if(!directory.exists()){
			    directory.mkdir();
			}
	    }
	}
	
	private static String makeTargetFileName(String path)
	{
		String newFileName = "" ;
		String[] pathNames = path.split("/");
		
		for(int i=0 ; i<pathNames.length ; i++)
		{
			if(0==i) continue;
			
			newFileName = newFileName + pathNames[i];
			if(pathNames.length-1 != i)
				newFileName = newFileName + " ";
	    }
		
		return newFileName;
	}	
	
	private static void copyFile(String sourcePath,String targetPath)
	{
        Path source = Paths.get(sourcePath);
        Path destination = Paths.get(targetPath);
        
        try {
            Files.copy(source, destination,StandardCopyOption.REPLACE_EXISTING);
            System.out.println(destination);
        } catch (IOException e) {
            e.printStackTrace();
        }		
	}
	
	private static String removeInvalidCharacter(String pathName)
	{
	    pathName = pathName.replace("\\","");
	    pathName = pathName.replace("*","");
	    pathName = pathName.replace("?","");
	    pathName = pathName.replace("\"","");
	    pathName = pathName.replace("<","");
	    pathName = pathName.replace(">","");
	    pathName = pathName.replace("\\|","");
	    
	    return pathName;
	}
}



