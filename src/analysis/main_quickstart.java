package analysis;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;

import javax.swing.JOptionPane;

import org.json.simple.parser.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

/* Some notes for this file
 * Currently it will not overwrite existing directories. Make sure the participant-name in the config file does not already exist in the directory
 * Key parameters can be set in the mainConfig.json file.
 * JSON config format is subject to change depending on required starting parameters. This is an early iteration.
 * 
 */

public class main_quickstart {
    public static void main(String args[]) throws Exception {
        System.out.println("");
        System.out.println("---------------------------------");
        System.out.println("INITIALIZING ANALYSIS FROM CONFIG");

        JSONParser parse = new JSONParser();
        JSONObject config = (JSONObject)parse.parse(new FileReader("src\\analysis\\mainConfig.json")); //Acquire mainConfig.json file

        String gazepointGZDPath = (String)config.get("gaze-filepath");
        String gazepointFXDPath = (String)config.get("fixation-filepath");
        String outputFolderPath = (String)config.get("output-directory") + "/" + (String)config.get("participant-name");
        String overwriteMode = (String)config.get("overwrite-mode");

        System.out.println("gazepointGZDPath: [ "+gazepointGZDPath+" ]");
        System.out.println("gazepointFXDPath: [ "+gazepointFXDPath+" ]");
        System.out.println("outputPath + participantName: [ "+outputFolderPath+" ]");

        SingleAnalytics.startAnalytics(gazepointGZDPath, gazepointFXDPath, outputFolderPath, overwriteMode);

    }
    /*
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
            }
        }
    }
    */
}