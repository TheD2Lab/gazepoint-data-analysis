package analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.List;
import java.util.HashMap; 
import java.util.Iterator;
import java.util.ArrayList;
import org.json.simple.*;
import org.json.simple.parser.*;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

/*
 * Read and write xml, csv, txt files
 * Save into memory
 */

public class FileHandler {
   static public DataEntry buildDataEntry(File allGaze) {
        try {
            FileReader fileReader = new FileReader(allGaze);
            CSVReader csvReader = new CSVReader(fileReader);
            DataEntry data = new DataEntry(csvReader.readNext());
            String[] line;

            while ((line = csvReader.readNext()) != null) {
                data.process(line);
            }

            csvReader.close();
            return data;
            
        } catch (Exception e) {
            System.out.println(e.toString());
            return  null;
        }
    }

    static public String convertToCSV(ArrayList<String> data) {
        String converted = "";
        for (int i = 0; i < data.size(); i++) {
            converted = converted + data.get(i);
            if (i < data.size() - 1) {
                converted = converted + ",";
            }
        }

        return converted + " \n ";
    }

    static public void writeToCSV(ArrayList<List<String>> data, String outputDirectory, String fileName) {
        try {
            // Check to see if the output directory exists. If not, create it
            File dir = new File(outputDirectory);
            if (!dir.exists()) dir.mkdirs();

            // Append a .csv to the file name if it's missing
            String fString = fileName.contains(".csv") ? fileName : fileName + ".csv";
            
            File file = new File(outputDirectory + "/" + fString);
            FileWriter fileWriter = new FileWriter(file);
            CSVWriter csvWriter = new CSVWriter(fileWriter);

            for (int i = 0; i < data.size(); i++) {
                List<String> row = data.get(i);
                int rowLength = row.size();
                String[] csvData = row.toArray(new String[rowLength]);
                csvWriter.writeNext(csvData);
            }

            csvWriter.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    static public void writeToText(String s, String outputDirectory, String fileName) {
        try {
            // Check to see if the output directory exists. If not, create it
            File dir = new File(outputDirectory);
            if (!dir.exists()) dir.mkdirs();

            // Append a .csv to the file name if it's missing
            String fString = fileName.contains(".txt") ? fileName : fileName + ".txt";
            
            File file = new File(outputDirectory + "/" + fString);
            FileWriter fileWriter = new FileWriter(file);

            fileWriter.write(s);
            fileWriter.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    static public boolean saveParametersAsJSON(HashMap<String,String> data, String outputDirectory) {
        try {
            System.out.println("Writing file!");
            FileWriter pw = new FileWriter(outputDirectory);
            pw.write(buildJSON(data).toString()); //Save data as JSON to file
            pw.flush();
            pw.close();
            System.out.println("Success!");
            return true;
        } catch (Exception e) {
            System.out.println(e.toString());
            return false;
        }
    }

    static private JSONObject buildJSON(HashMap<String,String> data) { //Builds a JSON object from the Parameters
        try {
            Object[] keys = data.keySet().toArray();
            JSONObject j = new JSONObject();  
            for (int i = 0; i < data.size(); i++) {
                j.put(keys[i].toString(), (String)data.get(keys[i]));
            }
            return j;
        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        }        
    }
    
    static public HashMap<String,String> loadParametersFromJSON(File filePath) {
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(filePath));
            JSONObject jsonObject = (JSONObject)obj;
            Iterator<?> keys = jsonObject.keySet().iterator();
            HashMap<String, String> data = new HashMap<String, String>();
            while (keys.hasNext()) {
                String key = (String)keys.next();
                data.put(key, (String)jsonObject.get(key));
            }
            return data;
        } catch(Exception e) {
           e.printStackTrace();
           return null;
        }              
    }
}
