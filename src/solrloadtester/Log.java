/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package solrloadtester;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

/**
 *
 * @author kevin
 */
public class Log {
      private final static File logFile = new File("request.log");
     private static Writer LOG_WRITER(File log){
        Writer fileWriter = null;
                try{
                fileWriter = new FileWriter(log, true);
                }catch(Exception e){
                    
                }
        return fileWriter;
    }
     
    private final static Writer fileWriter = LOG_WRITER(logFile);
    
    public static void log(String message){
        logMessage(message);
    }
    
      public static void log(Class cls, String message){
        logMessage("["+ cls.getSimpleName()+"]  " + message);
    }
   
    private static void logMessage(String message){
 
		try {
                    if(!logFile.exists()){
                        logFile.createNewFile();
                    }
			fileWriter.write(message);
		} catch (Exception e) {
		 
			e.printStackTrace();
		} 
    }
}
