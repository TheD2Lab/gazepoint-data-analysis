package analysis;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

// Note: Order used for data filtering matters and can invalidate DataEntrys if used incorrectly
// Must first filter by fixations, then validate, then apply screen size; otherwise, fixation algorithm or validating algorithm will fail
public class DataFilter {



    static public DataEntry filterByFixations(DataEntry data) { //Cleanses the data by filtering out repeated fixations
        DataEntry filtered = new DataEntry(data.getHeaders());

        List<String> lastValidFixation = null;
        int currFixation = 1;

        for (int row = 0; row < data.rowCount(); row++) {
            int fixationID = Integer.parseInt(data.getValue("FPOGID", row));
            int fixationValidity = Integer.parseInt(data.getValue("FPOGV", row));
            if (fixationID != currFixation) {
                if (lastValidFixation != null) filtered.process(lastValidFixation);
                if (fixationValidity == 1) lastValidFixation = data.getRow(row); // Edge case; check to see if the first line associated with a given fixation is valid
                currFixation = fixationID;
            } else if (fixationID == currFixation && fixationValidity == 1) {
                lastValidFixation = data.getRow(row);
            }
        }
        
        return filtered;
    }

    /**
     * Cleanses data by filtering out invalid data. Valid data entries must occur within
     * the bounds of the monitor and have humanly possible pupil dilation.
     * @param data Data to be cleansed
     */
    public static DataEntry filterByValidity(DataEntry data) {
        // humanly possible pupil diameter is between 2 and 8 mm
        final int MIN_DIAMETER = 2;
        final int MAX_DIAMETER = 8;
        final int MAX_PUPIL_DIFF = 1;   
        // GazePoint scales point of gaze location from 0 to 1 when on screen
        final int MIN_SCREEN_DIM = 0;   
        final int MAX_SCREEN_DIM = 1;   

        DataEntry filtered = new DataEntry(data.getHeaders());
        for (int rowNum = 0; rowNum < data.rowCount(); rowNum++) {
            // Note: It is extremely slow to parse a string over and over again
            // Check if Gazepoint could detect the pupils
            boolean leftValid = Integer.parseInt(data.getValue("LPMMV", rowNum)) == 1;
            boolean rightValid = Integer.parseInt(data.getValue("RPMMV", rowNum)) == 1;
            if (!(leftValid && rightValid)) {
                continue;   // skip invalid entry
            } 
            // Check if POG is on the screen
            float xCoordinate = Float.parseFloat(data.getValue("FPOGX" ,rowNum));
            float yCoordinate = Float.parseFloat(data.getValue("FPOGY" ,rowNum));
            if (xCoordinate < MIN_SCREEN_DIM || xCoordinate > MAX_SCREEN_DIM) {
                continue; // off screen in x-direction, invalid
            } else if (yCoordinate < MIN_SCREEN_DIM || yCoordinate > MAX_SCREEN_DIM) {
                continue; // off screen in y-direction, invalid entry
            }
            // Check if pupils are valid sizes individually and compared to each other.
            float leftDiameter = Float.parseFloat(data.getValue("LPMM", rowNum));
            float rightDiameter = Float.parseFloat(data.getValue("RPMM", rowNum));
            if (
                leftDiameter >= MIN_DIAMETER
                && leftDiameter <= MAX_DIAMETER
                && rightDiameter >= MIN_DIAMETER
                && rightDiameter <= MAX_DIAMETER
                && (Math.abs(leftDiameter-rightDiameter) <= MAX_PUPIL_DIFF)
            ) {
                filtered.process(data.getRow(rowNum));
            }
        }
        return filtered;
    }

    static public DataEntry applyScreenSize(DataEntry data, int screenWidth, int screenHeight) {
        DataEntry filtered = new DataEntry(data.getHeaders());

        for (int row = 0; row < data.rowCount(); row++) {
            List<String> currentRow = data.getRow(row);
            List<String> newRow = new ArrayList<String>();
            newRow.addAll(currentRow);

            int fixationXIndex = data.getHeaderIndex("FPOGX");
            int fixationYIndex = data.getHeaderIndex("FPOGY");

            newRow.set(fixationXIndex,String.valueOf(Double.valueOf(newRow.get(fixationXIndex)) * screenWidth));
            newRow.set(fixationYIndex,String.valueOf(Double.valueOf(newRow.get(fixationYIndex)) * screenHeight));

            filtered.process(newRow);
        }

        return filtered;
    }


    static public LinkedHashMap<String, DataEntry> filterByAOI(DataEntry data ){
        
        return new LinkedHashMap<String, DataEntry>();
    }
}
