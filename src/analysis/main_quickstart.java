package analysis;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;

import javax.swing.JOptionPane;

import org.json.simple.parser.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class main_quickstart {
    public static void main(String args[]) throws Exception {
        System.out.println("Initializing Analysis from Config")

        JSONParser parse = new JSONParser();
        JSONObject config = (JSONObject)parse.parse(new FileReader("src\\analysis\\mainConfig.json"));

        //JSONObject output = (JSONObject)config.get("output");

        String gazepointGZDPath = (String)config.get("gaze-filepath");
        String gazepointFXDPath = (String)config.get("fixation-filepath");
        String outputFolderPath = (String)config.get("output-directory") + "/" + (String)config.get("participant-name");

        System.out.println("gazepointGZDPath: [ "+gazepointGZDPath+" ]");
        System.out.println("gazepointFXDPath: [ "+gazepointFXDPath+" ]");
        System.out.println("outputPath + participantName: [ "+outputFolderPath+" ]");

        startAnalytics(gazepointGZDPath, gazepointFXDPath, outputFolderPath);

    }

    private static void startAnalytics(String gazepointGZDPath, String gazepointFXDPath, String outputFolderPath) {
        File participantFolder = new File(outputFolderPath);

        //creates the folder only if it doesn't exists already
        if(!participantFolder.exists())
        {
            boolean folderCreated = participantFolder.mkdir();
            if(!folderCreated)
            {
                JOptionPane.showMessageDialog(null, "Unable to create participant's folder", "Error Message", JOptionPane.ERROR_MESSAGE);
                systemLogger.writeToSystemLog(Level.SEVERE, SingleAnalytics.class.getName(), "Unable to create participant's folder");
                System.exit(0);
            }
            else
            {
                SingleAnalytics.analyzeData(gazepointGZDPath, gazepointFXDPath, outputFolderPath);
                try
                {	
                    SingleAnalytics.gazeAnalyticsOptions();
                } 
                catch (IOException e1) 
                {
                    systemLogger.writeToSystemLog(Level.WARNING, SingleAnalytics.class.getName(), "Error in Windowing Operation \n" + e1);
                }
            }

        }
    }
} 