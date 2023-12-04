package analysis;

import java.io.FileReader;
import org.json.simple.parser.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class mainSimple {
    public static void main(String args[]) throws Exception {
        JSONParser parse = new JSONParser();
        JSONObject config = (JSONObject)parse.parse(new FileReader("src\\analysis\\mainConfig.json"));

        //JSONObject output = (JSONObject)config.get("output");
        String directory = (String)config.get("location");
        String folderName = (String)config.get("name");

        System.out.println("directory: "+directory);
        System.out.println("folderName: "+folderName);

    }
} 