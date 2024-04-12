package analysis;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opencsv.CSVReader;

public class Windows {

    final static String TIME_INDEX = "TIME";
    final static int BASELINE_LENGTH = 120;

    public static void generateWindows(DataEntry allGaze, String outputDirectory, WindowSettings settings) {
        List<String> headers = allGaze.getHeaders();

        // Generate baseline file
        generateBaselineFile(allGaze, outputDirectory + "/baseline");

        // Tumbling Window
        if (settings.tumblingEnabled) {
            ArrayList<DataEntry> windows = new ArrayList<DataEntry>();
            DataEntry window = new DataEntry(headers);
            double windowSize = settings.tumblingWindowSize;
            double start = Double.valueOf(allGaze.getValue(TIME_INDEX, 0));
            double end = start + windowSize;

            for (int i = 0; i < allGaze.rowCount(); i++) {
                List<String> currRow = allGaze.getRow(i);
                Double t = Double.valueOf(allGaze.getValue(TIME_INDEX, i));
                
                if (t > end) { 
                    end += windowSize;
                    windows.add(window);
                    window = new DataEntry(headers);
                    window.process(currRow);
                } else if (i == allGaze.rowCount() - 1) { // Check to see if this is the last row of data in the list, if so append it to the last window
                    window.process(currRow);
                    windows.add(window);
                } else {
                    window.process(currRow);
                }
            }

            outputWindowFiles(windows, outputDirectory + "/tumbling");
        }

        // Expanding Window
        if (settings.expandingEnabled) {
            ArrayList<DataEntry> windows = new ArrayList<DataEntry>();
            DataEntry window = new DataEntry(headers);
            double windowSize = settings.expandingWindowSize;
            double start = Double.valueOf(allGaze.getValue(TIME_INDEX, 0));
            double end = start + windowSize;

            for (int i = 0; i < allGaze.rowCount(); i++) {
                List<String> currRow = allGaze.getRow(i);
                Double t = Double.valueOf(allGaze.getValue(TIME_INDEX, i));

                if (t > end) { 
                    System.out.println(end);
                    end += windowSize;
                    windows.add(window);
                    window = window.clone();
                    window.process(currRow);
                } else if (i == allGaze.rowCount() - 1) { // Check to see if this is the last row of data in the list, if so append it to the last window
                    window.process(currRow);
                    windows.add(window);
                } else {
                    window.process(currRow);
                }
            }

            outputWindowFiles(windows, outputDirectory + "/expanding");
        }

        // Hopping Window
        if (settings.hoppingEnabled) {
            ArrayList<DataEntry> windows = new ArrayList<DataEntry>();
            DataEntry window = new DataEntry(headers);
            int windowSize = settings.hoppingWindowSize;
            int hopSize = settings.hoppingHopSize;
            double start = Double.valueOf(allGaze.getValue(TIME_INDEX, 0));
            double end = start + windowSize;

            for (int i = 0; i < allGaze.rowCount(); i++) {
                double t1 = Double.parseDouble(allGaze.getValue(TIME_INDEX, i));
                
                if (t1 >= start) {
                    for (int j = i; j < allGaze.rowCount(); j++) {
                        List<String> row2 = allGaze.getRow(j);
                        double t2 = Double.parseDouble(allGaze.getValue(TIME_INDEX, j));

                        if (t2 >= end || j == allGaze.rowCount() - 1) {
                            window.process(row2);
                            windows.add(window);

                            start += hopSize;
                            end = start + windowSize;
                            window = new DataEntry(headers);

                            break;
                        } else {
                            window.process(row2);
                        }
                    }
                }
            }

            outputWindowFiles(windows, outputDirectory + "/hopping");
        }

        // Event Window
        if (settings.eventEnabled) {
            ArrayList<DataEntry> windows = new ArrayList<DataEntry>();
            DataEntry window = new DataEntry(headers);
            boolean isEventWindow = false;

            String event = settings.event;
            double timeoutLength = settings.eventTimeout;
            double eventEnd = 0;

            double baselineValue = getEventBaseline(outputDirectory, event);

            for (int i = 0; i < allGaze.rowCount(); i++) {
                Double t = Double.valueOf(allGaze.getValue(TIME_INDEX, i));
                Double windowValue =  Double.parseDouble(allGaze.getValue(event, i));

                if (windowValue > baselineValue) {
                    isEventWindow = true;
                    eventEnd = t + timeoutLength;
                }

                if (isEventWindow) {
                    if (t > eventEnd || i == allGaze.rowCount() - 1) {
                        windows.add(window);
                        window = new DataEntry(headers);
                        isEventWindow = false;
                    } else {
                        window.process(allGaze.getRow(i));
                    }
                }
            }

            outputWindowFiles(windows, outputDirectory + "/event");
        }
    }

    public static void outputWindowFiles(ArrayList<DataEntry> windows, String outputDirectory) {
        int windowCount = 1;
        for (DataEntry w : windows) {
            String fileName = "window" + windowCount;
            w.writeToCSV(outputDirectory, fileName);
            ArrayList<List<String>> results = Analysis.generateResults(w, DataFilter.filterByFixations(w)); // windows are continuous and raw, therefore fixation filtering will be valid
            FileHandler.writeToCSV(results, outputDirectory, fileName + "_DGMs");
            windowCount++;
        }
    }

    public static void generateBaselineFile(DataEntry allGaze, String outputDirectory) {
        DataEntry baseline = new DataEntry(allGaze.getHeaders());
        double startTime = Double.valueOf(allGaze.getValue(TIME_INDEX, 0));
        double endTime = startTime + BASELINE_LENGTH;

        for (int i = 0; i < allGaze.rowCount(); i++) {
            Double t = Double.parseDouble(allGaze.getValue(TIME_INDEX, i));

            if (t >= endTime) {
                break;
            } else {
                baseline.process(allGaze.getRow(i));
            }
        }

        // Since baseline is continuous, can filter by fixations
        baseline.writeToCSV(outputDirectory, "baseline");
        FileHandler.writeToCSV(Analysis.generateResults(baseline, DataFilter.filterByFixations(baseline)), outputDirectory, "baseline_DGMs");
    }

    public static double getEventBaseline(String fileDirectory, String event) {
        double eventValue = Double.NaN;

        try {
            File baselineDGMs = new File(fileDirectory + "/baseline/baseline.csv");
            CSVReader reader = new CSVReader(new FileReader(baselineDGMs));

            // Read the header line of the CSV file and cast into a List to find the index of the event
            List<String> headers = Arrays.asList(reader.readNext());

            int eventIndex = headers.indexOf(event);

            while (reader.peek() != null) {
                // Read the DGM values line of the CSV file and cast into a list to obtain the DGM baseline value
                eventValue += Double.valueOf(Arrays.asList(reader.readNext()).get(eventIndex));
            }

            eventValue /= ((double) reader.getLinesRead() - 1);

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return eventValue;
    }

    // public static double[] getEventBaseline(String fileDirectory, String event) {
    //     double[] eventValues = new double[] {Double.NaN, Double.NaN};

    //     try {
    //         File baselineDGMs = new File(fileDirectory + "/baseline/baseline_DGMs.csv");
    //         CSVReader reader = new CSVReader(new FileReader(baselineDGMs));

    //         // Read the header line of the CSV file and cast into a List to find the index of the event
    //         List<String> headers = Arrays.asList(reader.readNext());

    //         int eventIndex = headers.indexOf(event);
    //         eventValues[0] = eventIndex;

    //         // Read the DGM values line of the CSV file and cast into a list to obtain the DGM baseline value
    //         eventValues[1] = Double.valueOf(Arrays.asList(reader.readNext()).get(eventIndex));

    //         reader.close();
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
        
    //     return eventValues;
    // }
}
