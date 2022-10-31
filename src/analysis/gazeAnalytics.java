package analysis;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

public class gazeAnalytics {
//String inputFile, String outputFile, int timelength
	public static void continuousWindow(String inputFile, String outputFolder, int windowSize) throws IOException
	{
		int endTime = windowSize;
		int initialTime = 0;
		String outputFile = outputFolder + "\\continuous_" + endTime + ".csv";
		while(window(inputFile,outputFile,initialTime,endTime))
		{
			initialTime += windowSize;
			endTime += windowSize;
			outputFile = outputFolder + "\\continuous_" + endTime + ".csv";
		}
	}
	
	public static void cumulativeWindow(String inputFile, String outputFolder, int windowSize) throws IOException
	{
		int endTime = windowSize;
		int initialTime = 0;
		String outputFile = outputFolder + "\\cumulative_" + endTime + ".csv";
		while(window(inputFile,outputFile,initialTime,endTime))
		{
			endTime += windowSize;
			outputFile = outputFolder + "\\cumulative_" + endTime + ".csv";
		}
	}
	
	public static void overlappingWindow(String inputFile, String outputFolder, int windowSize, int overlap) throws IOException
	{
		int endTime = windowSize;
		int initialTime = 0;
		String outputFile = outputFolder + "\\overlap_" + endTime + ".csv";
		while(window(inputFile,outputFile,initialTime,endTime))
		{
			initialTime = endTime - overlap;
			endTime += windowSize;
			outputFile = outputFolder + "\\overlap_" + endTime + ".csv";
			
		}
	}
	
	private static boolean window(String inputFile, String outputFile, int start, int end) throws IOException
	{
		FileWriter outputFileWriter = new FileWriter(new File (outputFile));
        CSVWriter outputCSVWriter = new CSVWriter(outputFileWriter);
        FileReader fileReader = new FileReader(inputFile);
        CSVReader csvReader = new CSVReader(fileReader);

        try {
             String[]nextLine = csvReader.readNext();
             int timestampIndex = -1;
             for(int i = 0; i < nextLine.length; i++)
             {
            	 if(nextLine[i].contains("TIME("))
            	 {
            		 timestampIndex = i;
            		 break;
            	 }
             }
             
             while((nextLine = csvReader.readNext()) != null) 
             {
            	 if(Double.valueOf(nextLine[timestampIndex]) < start)
            	 {
            		 continue;
            	 }
            	 else if(Double.valueOf(nextLine[timestampIndex]) > end)
            	 {
            		 break;
            	 }
            	 else
            	 {
            		 outputCSVWriter.writeNext(nextLine);
            	 }
             }

             if((nextLine = csvReader.readNext()).equals(null))
             {
            	 return false;
             }
             
            System.out.println("done writing file: " + outputFile);
            outputCSVWriter.close();
        }
        catch(NullPointerException ne)
        {
        	System.out.println("done writing file: " + outputFile);
        	outputCSVWriter.close();
        	csvToARFF(outputFile);
        	return false;
        }
        catch(Exception e)
        {
            System.out.println("Error unable to write file: " + outputFile);
            System.out.println(e);
            return false;
        }
        finally
        {
            csvReader.close();
        }
        
        csvToARFF(outputFile);
        return true;
	}
	
	
	public static void csvToARFF(String outputCSVPath) throws IOException
	{
		CSVLoader loader = new CSVLoader();
	    loader.setSource(new File(outputCSVPath));
	    Instances data = loader.getDataSet();
	    
	    String outputARFFPath = outputCSVPath.replace(".csv", ".arff");
	    
	    File arffFile = new File(outputARFFPath);
	    if(!arffFile.exists())
	    {
		    ArffSaver saver = new ArffSaver();
		    saver.setInstances(data);
		    saver.setFile(arffFile);
		    saver.writeBatch();
		    System.out.println("Successful " + outputARFFPath);
	    }
	    else
	    {
	    	System.out.println("File Exists");
	    }
	}
	
}
