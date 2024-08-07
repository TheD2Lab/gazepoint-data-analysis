package com.github.thed2lab.analysis;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class AreaOfInterests {
    final static String FIXATIONID_INDEX = "FPOGID"; //CNT
    final static String DURATION_INDEX = "FPOGD";
    final static String AOI_INDEX = "AOI";


    private static final String[] additionalHeaders = {"aoi", "proportion_of_fixations_spent_in_aoi","proportion_of_fixations_durations_spent_in_aoi"};
    private static final String[] perAoiHeaders = {"aoi_pair", "transition_count", "proportion_including_self_transitions", "proportion_excluding_self_transitions"};
    
    
    public static void generateAOIs(DataEntry allGazeData, String outputDirectory, String fileName) {
        LinkedHashMap<String, DataEntry> aoiMetrics = new LinkedHashMap<>();
        for (int i = 0; i < allGazeData.rowCount(); i++) {
            String aoi = allGazeData.getValue(AOI_INDEX, i);
            String aoiKey = aoi.equals("") ? "Undefined Area" : aoi;
            if (!aoiMetrics.containsKey(aoiKey)) {
                DataEntry d = new DataEntry(allGazeData.getHeaders());
                aoiMetrics.put(aoiKey, d);
            }
            aoiMetrics.get(aoiKey).process(allGazeData.getRow(i));
        }

        
        LinkedHashMap<String, DataEntry> aoiFixationMetrics = new LinkedHashMap<>();
        DataEntry allFixations = DataFilter.filterByValidity(DataFilter.filterByFixations(allGazeData));
        //System.out.println(allFixations.rowCount());
        for (int i = 0; i < allFixations.rowCount(); i++) {
            String aoi = allFixations.getValue(AOI_INDEX, i);
            String aoiKey = aoi.equals("") ? "Undefined Area" : aoi;
            if (!aoiFixationMetrics.containsKey(aoiKey)) {
                DataEntry d = new DataEntry(allFixations.getHeaders());
                aoiFixationMetrics.put(aoiKey, d);
            }
            aoiFixationMetrics.get(aoiKey).process(allFixations.getRow(i));
        }

        // For any AOIs not in aoiFixationMetrics, add an empty DataEntry
        for (String key : aoiMetrics.keySet()) {
            if (!aoiFixationMetrics.containsKey(key)) {
                DataEntry d = new DataEntry(allGazeData.getHeaders());
                aoiFixationMetrics.put(key, d);
            }
        }

        if (aoiMetrics.size() <= 1) {
            System.out.println("File has no AOIs, no file will be output.");
            return;
        }
        
        // printing the elements of LinkedHashMap
        ArrayList<List<String>> metrics = new ArrayList<>();
        metrics.add(new ArrayList<String>());
        
        double totalDuration = getDuration(allFixations);
        LinkedHashMap<String, DataEntry> validAOIs = new LinkedHashMap<>();
        boolean isFirst = true;
        Set<String> aoiKeySet = aoiMetrics.keySet();
        
        int row = 1;
        for (String aoiKey : aoiKeySet) {
            DataEntry singleAoiGaze = aoiMetrics.get(aoiKey);
            DataEntry singleAoiFixations = aoiFixationMetrics.get(aoiKey);

            singleAoiGaze.writeToCSV(outputDirectory + "/AOIs", aoiKey + "_all_gaze");

            List<List<String>> results = Analysis.generateResults(allGazeData, singleAoiGaze, singleAoiFixations);
            if (isFirst) { //
                isFirst = false;
                List<String> headers = results.get(0);
                metrics.get(0).addAll(headers); //Adds all headers generated by an analysis, but only for the first AOI
                metrics.get(0).addAll(Arrays.asList(additionalHeaders));
            }
            results.get(1).add(aoiKey);
            metrics.add(results.get(1));
            metrics.get(row).addAll(getProportions(allFixations, singleAoiFixations, totalDuration));
            validAOIs.put(aoiKey, singleAoiFixations);
            row++;
        }
        ArrayList<List<String>> pairResults = generatePairResults(allFixations, aoiMetrics);
        FileHandler.writeToCSV(metrics, outputDirectory, fileName + "_AOI_DGMs");
        FileHandler.writeToCSV(pairResults, outputDirectory, fileName+"_AOI_Transitions");
    }

    public static ArrayList<String> getProportions(DataEntry fixations, DataEntry aoiFixations, double totalDuration) {
        ArrayList<String> results = new ArrayList<>();
        double fixationProportion = (double)aoiFixations.rowCount()/fixations.rowCount(); //Number of fixations in AOI divided by total fixations
        results.add(String.valueOf(fixationProportion));
        double durationIn = getDuration(aoiFixations);
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

    public static ArrayList<List<String>> generatePairResults(DataEntry fixations, LinkedHashMap<String,DataEntry> validAOIs) {
        LinkedHashMap<String, ArrayList<Integer>> totalTransitions = new LinkedHashMap<>(); // ArrayList<Integer>(Transtions, Inclusive, Exlusive);
        LinkedHashMap<String,LinkedHashMap<String, Integer>> transitionCounts = new LinkedHashMap<>();
        for (int i = 0; i < fixations.rowCount()-1; i++) {
            String curAoi = fixations.getValue(AOI_INDEX, i);
            curAoi = curAoi.equals("") ? "Undefined Area" : curAoi;
            int curId = Integer.valueOf(fixations.getValue(FIXATIONID_INDEX, i));
            String nextAoi = fixations.getValue(AOI_INDEX, i+1);
            nextAoi = nextAoi.equals("") ? "Undefined Area" : nextAoi;
            int nextId = Integer.valueOf(fixations.getValue(FIXATIONID_INDEX, i+1));
            boolean isValidAOI = (validAOIs.containsKey(curAoi) && validAOIs.containsKey(nextAoi));
            if (isValidAOI && nextId == curId + 1) { //Check if fixations are subsequent
                if (!totalTransitions.containsKey(curAoi)) { //Ensure AOI is initialized in map.
                    ArrayList<Integer> counts = new ArrayList<Integer>();
                    counts.add(0);
                    counts.add(0);
                    totalTransitions.put(curAoi, counts);

                    transitionCounts.put(curAoi, new LinkedHashMap<String, Integer>());
                }
                if (!transitionCounts.get(curAoi).containsKey(nextAoi)) { //Ensure pair is initialized
                    transitionCounts.get(curAoi).put(nextAoi,0);
                }
                ArrayList<Integer> transition = totalTransitions.get(curAoi);
                transition.set(0, transition.get(0)+ 1); //Inclusive counter
                LinkedHashMap<String,Integer>aoiTransitions = transitionCounts.get(curAoi); //Data for current AOI's transitions
                aoiTransitions.put(nextAoi,aoiTransitions.get(nextAoi)+1); //Increment count of curAoi->nextAoi
                if (!curAoi.equals(nextAoi)) {
                    transition.set(1, transition.get(1)+ 1); //Exclusive counter
                }
            }
        }

        ArrayList<List<String>> results = new ArrayList<>();
        results.add(new ArrayList<String>());
        results.get(0).addAll(Arrays.asList(perAoiHeaders));

        int i = 1; //Incrementer starts at since element 0 is the list of headers.
        for (String key : totalTransitions.keySet()) {
            int transitionsInclusive = totalTransitions.get(key).get(0);
            int transitionsExclusive = totalTransitions.get(key).get(1);
            for (String otherKey : totalTransitions.keySet()) {
                int transitions = 0;
                if(transitionCounts.get(key).containsKey(otherKey)) {
                    transitions = transitionCounts.get(key).get(otherKey);
                }
                 //Number of transtions from other AOI to current AOI
                results.add(new ArrayList<>());
                results.get(i).add(key + " -> " + otherKey);
                results.get(i).add(String.valueOf(transitions));
                results.get(i).add(String.valueOf((double)transitions/(double)transitionsInclusive));
                if (key != otherKey) results.get(i).add(String.valueOf((double)transitions/(double)transitionsExclusive)); //Only count exclusive transitions for transitions that arent from the AOI to itself (transitions exclusive from A->A is set to be 0)
                else results.get(i).add(String.valueOf(0.0));
                i++;
            }
        }
        return results;
    }
}
