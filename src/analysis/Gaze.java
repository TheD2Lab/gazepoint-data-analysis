package analysis;

import java.util.LinkedHashMap;

public class Gaze {
    final static String PUPIL_LEFT_DIAMETER_INDEX = "LPMM";
    final static String PUPIL_RIGHT_DIAMETER_INDEX = "RPMM";
    final static String BLINK_INDEX = "BKPMIN";
    final static String TIME_INDEX = "TIME";

    static public LinkedHashMap<String,String> analyze(DataEntry data) {
        LinkedHashMap<String,String> results = new LinkedHashMap<String,String>();
        double leftSum = 0.0;
        double rightSum = 0.0;
        double bothSum = 0.0;
        int totalBlinks = 0;
        int minutes = 1;
        int count = data.rowCount();

        for (int row = 0; row < data.rowCount(); row++) {
            double leftSize = Double.valueOf(data.getValue(PUPIL_LEFT_DIAMETER_INDEX, row));
            double rightSize = Double.valueOf(data.getValue(PUPIL_RIGHT_DIAMETER_INDEX, row));

            leftSum += leftSize;
            rightSum += rightSize;
            bothSum += (leftSize + rightSize) / 2.0;

            if (Double.valueOf(data.getValue(TIME_INDEX, row)) > minutes * 60) {
                totalBlinks += Integer.valueOf(data.getValue(BLINK_INDEX, row));
                minutes++;
            }
        }
        double leftAverage = leftSum / count;
        double rightAverage = rightSum / count;
        double bothAverage = bothSum / count;

        results.put(
            "Average Blink Rate per Minute", //Output Header
            String.valueOf(totalBlinks / minutes)
            );   

        //Absolute Degrees
        results.put(
            "total number of valid recordings", //Output Header
            String.valueOf(data.rowCount())
            );

        results.put(
            "average pupil size of left eye", //Output Header
            String.valueOf(leftAverage)
            );    

        results.put(
            "average pupil size of right eye", //Output Header
            String.valueOf(rightAverage)
            );    

        results.put(
            "average pupil size of both eyes", //Output Header
            String.valueOf(bothAverage)
            );    
        return results;
    }
}
