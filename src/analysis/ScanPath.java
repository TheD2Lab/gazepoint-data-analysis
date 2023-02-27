package analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

public class ScanPath {
	
	//key is the time and the value is the aoi 
	private LinkedHashMap<Double,String>aoi = new LinkedHashMap<Double,String>();
	private String fixationFile; 
	private String outputFile = "C:\\Users\\kayla\\OneDrive\\Documents\\temp\\ScanPath\\";
	public ScanPath(String fixationFile)
	{
		this.fixationFile = fixationFile;
		parseFile();
	}
	//parses the files and puts all the needed value in a hashmap
	private void parseFile()
	{
		FileReader fileReader;
		CSVReader csvReader;
		try {
			fileReader = new FileReader(fixationFile);
			csvReader = new CSVReader(fileReader);
			String[]nextLine = csvReader.readNext();
			int aoiColumnIndex = Arrays.asList(nextLine).indexOf("AOI");
			int timeIndex = -1;
			for(int i = 0; i < nextLine.length; i++)
         	{
         		if(nextLine[i].contains("TIME")) {
         			timeIndex = i;
         			break;
         		}
         	}
			
			
			while((nextLine = csvReader.readNext()) != null) 
			{
				String value = nextLine[aoiColumnIndex];
				double key = Double.valueOf(nextLine[timeIndex]);
				if(value.isBlank())
				{
					aoi.put(key, null);
				}
				else
				{
					aoi.put(key, value);
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (CsvValidationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void TScan() throws IOException
	{
		//AS, AI,HI,ALT
		Iterator<Entry<Double,String>> aoiIterator = aoi.entrySet().iterator();
		int counter = 0; 
		boolean AI, HI, ALT, AS;
		AI = HI = ALT = AS = false;
		double prevElementTime, startTime; 
		prevElementTime = startTime = -1;
		
		while(aoiIterator.hasNext())
		{
			Map.Entry<Double, String> element = (Map.Entry<Double, String>)aoiIterator.next();
			if((prevElementTime == -1 || element.getKey()- prevElementTime < 1)&&element.getValue()!=null)
			{
				switch(element.getValue())
				{
					case "Horizontal Altitude":
						AI = true;
						if(startTime == -1) startTime = element.getKey();
						prevElementTime = element.getKey();
						break;
					case "Airspeed":
						AS = true;
						if(startTime == -1) startTime = element.getKey();
						prevElementTime = element.getKey();
						break;
					case "Heading" :
						HI = true;
						if(startTime == -1) startTime = element.getKey();
						prevElementTime = element.getKey();
						break;
					case "MSL Altitude":
						ALT = true;
						if(startTime == -1) startTime = element.getKey();
						prevElementTime = element.getKey();
						break;
				}
				
			}
			else if(element.getKey()- prevElementTime < 1)
			{
				continue;
			}
			else
			{
				if(AI && HI && ALT && AS)
				{
					
					writeToFile(fixationFile, outputFile + "T_Scan" + counter + ".csv",startTime, prevElementTime);
					AI = HI = ALT = AS = false;
					prevElementTime = startTime = -1;
					counter++;
					
				}
			}
			
		}
	}

	public void primaryInstrumentAS() throws IOException
	{
		//AS, AI,HI,ALT
		Iterator<Entry<Double,String>> aoiIterator = aoi.entrySet().iterator();
		int counter = 0; 
		boolean AI, HI, AS;
		HI = AI = AS = false;
		double prevElementTime, startTime; 
		prevElementTime = startTime = -1;
		
		while(aoiIterator.hasNext())
		{
			Map.Entry<Double, String> element = (Map.Entry<Double, String>)aoiIterator.next();
			if((prevElementTime == -1 || element.getKey()- prevElementTime < 1)&&element.getValue()!=null)
			{
				switch(element.getValue())
				{
					case "Horizontal Altitude":
						AI = true;
						if(startTime == -1) startTime = element.getKey();
						prevElementTime = element.getKey();
						break;
					case "Airspeed":
						AS = true;
						if(startTime == -1) startTime = element.getKey();
						prevElementTime = element.getKey();
						break;
					case "Heading" :
						HI = true;
						if(startTime == -1) startTime = element.getKey();
						prevElementTime = element.getKey();
						break;
				}
				
			}
			else if(element.getKey()- prevElementTime < 1)
			{
				continue;
			}
			else
			{
				if(AI && HI && AS)
				{
					
					writeToFile(fixationFile, outputFile + "PitchInstrumentAS" + counter + ".csv",startTime, prevElementTime);
					AI = HI = AS = false;
					prevElementTime = startTime = -1;
					counter++;
					
				}
			}
			
		}
	}
	
	public void primaryInstrumentVSI() throws IOException
	{
		//AS, AI,HI,ALT
		Iterator<Entry<Double,String>> aoiIterator = aoi.entrySet().iterator();
		int counter = 0; 
		boolean AI, HI, VSI;
		HI = AI = VSI = false;
		double prevElementTime, startTime; 
		prevElementTime = startTime = -1;
		
		while(aoiIterator.hasNext())
		{
			Map.Entry<Double, String> element = (Map.Entry<Double, String>)aoiIterator.next();
			if((prevElementTime == -1 || element.getKey()- prevElementTime < 1)&&element.getValue()!=null)
			{
				switch(element.getValue())
				{
					case "Horizontal Altitude":
						AI = true;
						if(startTime == -1) startTime = element.getKey();
						prevElementTime = element.getKey();
						break;
					case "Vertical Speed":
						VSI = true;
						if(startTime == -1) startTime = element.getKey();
						prevElementTime = element.getKey();
						break;
					case "Heading" :
						HI = true;
						if(startTime == -1) startTime = element.getKey();
						prevElementTime = element.getKey();
						break;
				}
				
			}
			else if(element.getKey()- prevElementTime < 1)
			{
				continue;
			}
			else
			{
				if(AI && HI && VSI)
				{
					
					writeToFile(fixationFile, outputFile + "PitchInstrumentVSI" + counter + ".csv",startTime, prevElementTime);
					AI = HI = VSI = false;
					prevElementTime = startTime = -1;
					counter++;
					
				}
			}
			
		}
	}

	public void pitchTriangle() throws IOException
	{
		//AS, AI,HI,ALT
		Iterator<Entry<Double,String>> aoiIterator = aoi.entrySet().iterator();
		int counter = 0; 
		boolean AI, ALT, VSI;
		ALT = AI = VSI = false;
		double prevElementTime, startTime; 
		prevElementTime = startTime = -1;
		
		while(aoiIterator.hasNext())
		{
			Map.Entry<Double, String> element = (Map.Entry<Double, String>)aoiIterator.next();
			if((prevElementTime == -1 || element.getKey()- prevElementTime < 1)&&element.getValue()!=null)
			{
				switch(element.getValue())
				{
					case "Horizontal Altitude":
						AI = true;
						if(startTime == -1) startTime = element.getKey();
						prevElementTime = element.getKey();
						break;
					case "Vertical Speed":
						VSI = true;
						if(startTime == -1) startTime = element.getKey();
						prevElementTime = element.getKey();
						break;
					case "MSL Altitude":
						ALT = true;
						if(startTime == -1) startTime = element.getKey();
						prevElementTime = element.getKey();
						break;
				}
				
			}
			else if(element.getKey()- prevElementTime < 1)
			{
				continue;
			}
			else
			{
				if(AI && ALT && VSI)
				{
					
					writeToFile(fixationFile, outputFile + "PitchInstrumentVSI" + counter + ".csv",startTime, prevElementTime);
					AI = ALT = VSI = false;
					prevElementTime = startTime = -1;
					counter++;
					
				}
			}
			
		}
	}

	public void radialScan() throws IOException
	{
		//AS, AI,HI,ALT
				Iterator<Entry<Double,String>> aoiIterator = aoi.entrySet().iterator();
				int counter = 0; // 6 
				int csvCounter = 0;
				boolean AI,otherInstr;
				AI = otherInstr = false;
				double prevElementTime, startTime; 
				prevElementTime = startTime = -1;
				String prevOthElementValue = ""; //o
				
				while(aoiIterator.hasNext())
				{
					Map.Entry<Double, String> element = (Map.Entry<Double, String>)aoiIterator.next();
					if((prevElementTime == -1 || element.getKey()- prevElementTime < 1)&&element.getValue()!=null)
					{
						
						if(element.getValue().equals("Horizontal Altitude"))
						{
							if(AI == true)
							{
								continue;
							}
							else
							{
								AI = true;
								if(startTime==-1) startTime = element.getKey();
								prevElementTime = element.getKey();
								otherInstr = false;
								counter++;
								continue;
							}
						}
						else if(!(element.getValue() ==null)&& !(element.getValue().equals("outside")))
						{ 
							if(otherInstr == true)
							{
								if(prevOthElementValue.equals(element.getValue()))
								{
									continue;
								}
							}
							else
							{
								otherInstr= true;
								if(startTime==-1) startTime = element.getKey();
								prevElementTime = element.getKey();
								prevOthElementValue = element.getValue();
								AI = false;
								counter++;
								continue;
							}
						}
						
						
					}
					else if(element.getKey()- prevElementTime < 1)//if the element is null but within one minute
					{
						continue;
					}
					else
					{
						if(counter==6)
						{
							
							writeToFile(fixationFile, outputFile + "RadialScan" + csvCounter + ".csv",startTime, prevElementTime);
							AI = otherInstr = false;
							prevElementTime = startTime = -1;
							counter = 0;
							prevOthElementValue = "";
							csvCounter++;
							
						}
					}
					
				}
	}

	
	private static void writeToFile(String inputFile, String outputFile, double start, double end) throws IOException
	{
		FileWriter outputFileWriter = new FileWriter(new File (outputFile));
        CSVWriter outputCSVWriter = new CSVWriter(outputFileWriter);
        FileReader fileReader = new FileReader(inputFile);
        CSVReader csvReader = new CSVReader(fileReader);

        try 
        {
        	//header
             String[]nextLine = csvReader.readNext();
             outputCSVWriter.writeNext(nextLine);
             
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
            	 System.exit(0);
             }	
             
     		systemLogger.writeToSystemLog(Level.INFO, gazeAnalytics.class.getName(), "Successfully created file " + outputFile );
        }
        catch(NullPointerException ne)
        {
        	System.out.println("done writing file: " + outputFile);
        	outputCSVWriter.close();
        	gazeAnalytics.csvToARFF(outputFile);
        }
        catch(Exception e)
        {
    		systemLogger.writeToSystemLog(Level.SEVERE, gazeAnalytics.class.getName(), "Error with window method  " + outputFile + "\n" + e.toString());
    		System.exit(0);

        }
        finally
        {
            outputCSVWriter.close();
            csvReader.close();
        }
        
        gazeAnalytics.csvToARFF(outputFile);
        }
	

	






















}
