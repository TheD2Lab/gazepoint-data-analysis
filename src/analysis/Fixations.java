package analysis;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Fixations {
    final static String DURATION_INDEX = "FPOGD";

    static public LinkedHashMap<String,String> analyze(DataEntry data) {
        LinkedHashMap<String,String> results = new LinkedHashMap<String,String>();

        ArrayList<Double> allFixationDurations = new ArrayList<>();
        int fixationCount = data.rowCount();
        
        for (int row = 0; row < data.rowCount(); row++) {    
            Double fixationDurationSeconds = Double.valueOf(data.getValue(DURATION_INDEX, row));
            allFixationDurations.add(fixationDurationSeconds);
        }
        
        results.put(
            "Total Number of Fixations", //Output Header
            String.valueOf(fixationCount) //Output Value
            );

        results.put(
            "Sum of all fixation duration (s)",
            String.valueOf(DescriptiveStats.getSumOfDoubles(allFixationDurations))
            );

        results.put(
            "Mean fixation duration (s)",
            String.valueOf(DescriptiveStats.getMeanOfDoubles(allFixationDurations))
            );

        results.put(
            "Median fixation duration (s)",
            String.valueOf(DescriptiveStats.getMedianOfDoubles(allFixationDurations))
            );

        results.put(
            "St.Dev. of fixation durations (s)",
            String.valueOf(DescriptiveStats.getStDevOfDoubles(allFixationDurations))
            );

        results.put(
            "Min. fixation duration (s)",
            String.valueOf(DescriptiveStats.getMinOfDoubles(allFixationDurations))
            );

        results.put(
            "Max. fixation duration (s)",
            String.valueOf(DescriptiveStats.getMaxOfDoubles(allFixationDurations))
            );

        return results;
    }
}