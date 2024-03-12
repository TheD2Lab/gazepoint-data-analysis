package analysis;

import java.util.Arrays;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import java_cup.runtime.lr_parser;
// import javafx.scene.chart.PieChart.Data;
import weka.core.pmml.jaxbbindings.RESULTFEATURE;

public class AreaOfInterests {
    final static String FIXATIONID_INDEX = "FPOGID";
    final static String DURATION_INDEX = "FPOGD";
    final static String AOI_INDEX = "AOI";


    private static final String[] additionalHeaders = {"AOI", "Proportion of fixations spent in AOI","Proportion of fixations durations spent in AOI"};
    private static final String[] perAoiHeaders = {"AOI Pair", "Transition Count", "Proportion including self-transitions", "Proportion excluding self-transitions"};
    
    
    public static void generateAOIs(DataEntry allGazeData, String outputDirectory, String fileName) {
        LinkedHashMap<String, DataEntry> aoiMetrics = new LinkedHashMap<>();
        for (int i = 0; i < allGazeData.rowCount(); i++) {
            String aoi = allGazeData.getValue(AOI_INDEX, i);
            String aoiKey = aoi.equals("") ? "No AOI" : aoi;
            if (!aoiMetrics.containsKey(aoiKey)) {
                DataEntry d = new DataEntry(allGazeData.getHeaders());
                aoiMetrics.put(aoiKey, d);
            }
            aoiMetrics.get(aoiKey).process(allGazeData.getRow(i));
        }

        if (aoiMetrics.size() <= 1) {
            System.out.println("File has no AOIs, no file will be output.");
            return;
        }
        
        // printing the elements of LinkedHashMap
        ArrayList<List<String>> metrics = new ArrayList<>();
        metrics.add(new ArrayList<String>());

        double totalDuration = getDuration(allGazeData);
        LinkedHashMap<String, DataEntry> validAOIs = new LinkedHashMap<>();
        boolean isFirst = true;
        Set<String> aoiKeySet = aoiMetrics.keySet();
        int row = 1;
        for (String aoiKey : aoiKeySet) {
            DataEntry aoi = aoiMetrics.get(aoiKey);
            DataEntry aoiFixations = DataFilter.filterByFixations(aoi);

            aoi.writeToCSV(outputDirectory + "/AOIs", aoiKey + "_all_gaze");

            if (aoi.rowCount() >= 2) {
                ArrayList<List<String>> results = Analysis.generateResults(aoi);
                if (isFirst) { //
                    isFirst = false;
                    List<String> headers = results.get(0);
                    metrics.get(0).addAll(headers); //Adds all headers generated by an analysis, but only for the first AOI
                    metrics.get(0).addAll(Arrays.asList(additionalHeaders));
                }
                results.get(1).add(aoiKey);
                metrics.add(results.get(1));
                metrics.get(row).addAll(getProportions(allGazeData, aoiFixations, totalDuration));
                validAOIs.put(aoiKey, aoiFixations);
                row++;
            }
        }
        ArrayList<ArrayList<String>> pairResults = generatePairResults(allGazeData, aoiMetrics);
        for (int i = 0; i < pairResults.size(); i++) { //Write values to all rows
            for (String s : perAoiHeaders) {
                metrics.get(0).add(s + "_" + i); //Adds headersfor each pair.
            }
            metrics.get(i + 1).addAll(pairResults.get(i));
        }
        FileHandler.writeToCSV(metrics, outputDirectory, fileName + "_AOI_DGMs");
    }

    public static ArrayList<String> generateAreaOfInterestResults(DataEntry all,DataEntry aoi, double totalDuration) {
        ArrayList<String> results = new ArrayList<>();
        List<String> proportions = getProportions(all, aoi, totalDuration);
        results.addAll(proportions);
        return results;
    }

    public static ArrayList<String> getProportions(DataEntry all,DataEntry aoi, double totalDuration) {
        ArrayList<String> results = new ArrayList<>();
        double fixationProportion = (double)aoi.rowCount()/all.rowCount(); //Number of fixations in AOI divided by total fixations
        results.add(String.valueOf(fixationProportion));
        double durationIn = getDuration(aoi);
        double durationProportion = durationIn/totalDuration;
        results.add(String.valueOf(durationProportion));
        return results;
    }

    public static double getDuration(DataEntry fixations) {
        double durationSum = 0.0;
        for (int i = 0; i < fixations.rowCount(); i++) {
            durationSum += Double.valueOf(fixations.getValue(DURATION_INDEX, i));
        }

        return durationSum;
    }

    public static ArrayList<ArrayList<String>> generatePairResults(DataEntry fixations, LinkedHashMap<String,DataEntry> validAOIs) {
        LinkedHashMap<String, ArrayList<Integer>> totalTransitions = new LinkedHashMap<>(); // ArrayList<Integer>(Transtions, Inclusive, Exlusive);
        LinkedHashMap<String,LinkedHashMap<String, Integer>> transitionCounts = new LinkedHashMap<>();
        for (int i = 0; i < fixations.rowCount()-1; i++) {
            String curAoi = fixations.getValue(AOI_INDEX, i);
            int curId = Integer.valueOf(fixations.getValue(FIXATIONID_INDEX, i));
            String nextAoi = fixations.getValue(AOI_INDEX, i+1);
            int nextId = Integer.valueOf(fixations.getValue(FIXATIONID_INDEX, i+1));
            boolean isValidAOI = (validAOIs.containsKey(curAoi) && validAOIs.containsKey(nextAoi));
            if (isValidAOI && nextId == curId + 1) { //Check if fixations are subsequent
                if (!totalTransitions.containsKey(nextAoi)) { //Ensure AOI is initialized in map.
                    ArrayList<Integer> counts = new ArrayList<Integer>();
                    counts.add(0);
                    counts.add(0);
                    totalTransitions.put(nextAoi, counts);

                    transitionCounts.put(nextAoi, new LinkedHashMap<String, Integer>());
                }
                if (!transitionCounts.get(nextAoi).containsKey(curAoi)) { //Ensure pair is initialized
                    transitionCounts.get(nextAoi).put(curAoi,0);
                }
                ArrayList<Integer> transition = totalTransitions.get(nextAoi);
                transition.set(0, transition.get(0)+ 1); //Inclusive counter
                if (!curAoi.equals(nextAoi)) {
                    transition.set(1, transition.get(1)+ 1); //Exclusive counter
                    LinkedHashMap<String,Integer>aoiTransitions = transitionCounts.get(nextAoi); //Transition to next Aoi, from current Aoi
                    aoiTransitions.put(curAoi,aoiTransitions.get(curAoi)+1); //Increment transition to next Aoi, from current Aoi
                }
            }
        }

        ArrayList<ArrayList<String>> results = new ArrayList<>();
        int i = 0; //Incrementer
        for (String key : totalTransitions.keySet()) {
            int transitionsInclusive = totalTransitions.get(key).get(0);
            int transitionsExclusive = totalTransitions.get(key).get(1);
            results.add(new ArrayList<>());
            for (String otherKey : totalTransitions.keySet()) {
                int transitions = 0;
                if(transitionCounts.get(key).containsKey(otherKey)) {
                    transitions = transitionCounts.get(key).get(otherKey);
                }
                 //Number of transtions from other AOI to current AOI
                results.get(i).add(otherKey);
                results.get(i).add(String.valueOf(transitions));
                results.get(i).add(String.valueOf((double)transitions/(double)transitionsInclusive));
                results.get(i).add(String.valueOf((double)transitions/(double)transitionsExclusive));
            }
            i++;
        }
        return results;
    }
}
