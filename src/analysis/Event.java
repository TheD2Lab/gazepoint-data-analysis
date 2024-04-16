package analysis;

import java.util.LinkedHashMap;

public class Event {
    final static String INPUT_INDEX = "CS";
    
    static public LinkedHashMap<String,String> analyze(DataEntry data) {
        LinkedHashMap<String,String> results = new LinkedHashMap<String,String>();
        int leftMouseClicks = 0;

        for (int row = 0; row < data.rowCount(); row++) {
            if (data.getValue(INPUT_INDEX, row).equals("1")) {
                leftMouseClicks += 1;
            }
        }
        //Absolute Degrees
        results.put(
            "total number of L mouse clicks", //Output Header
            String.valueOf(leftMouseClicks)
            );    
        return results;
    }
}
