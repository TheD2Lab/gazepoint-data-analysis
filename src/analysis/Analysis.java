package analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Analysis {
    final static int SCREEN_WIDTH = 1920;
	final static int SCREEN_HEIGHT = 1080;

    final static int MIN_PATTERN_LENGTH = 3;
    final static int MAX_PATTERN_LENGTH = 7;
    final static int MIN_PATTERN_FREQUENCY = 2;
    final static int MIN_SEQUENCE_SIZE = 3;

    final static String TIME_INDEX = "TIME";

    private Parameters params;
    
    public Analysis(Parameters params) {
        this.params = params;
    }

    public boolean run() {
        try {
            File[] inputFiles = params.getInputFiles();
            List<String> sequences = new ArrayList<String>();
            ArrayList<List<String>> allParticipantDGMs = new ArrayList<List<String>>();

            for (int i = 0; i < inputFiles.length; i++) {
                File f = inputFiles[i];

                String pName = f.getName().replace("_all_gaze", "").replace(".csv", "");
                String pDirectory = params.getOutputDirectory() + "/" + pName;

                System.out.println("Analyzing " + pName);

                DataEntry allGaze = FileHandler.buildDataEntry(f);
                DataEntry validGaze = DataFilter.filterByValidity(allGaze);
                
                DataEntry fixations = DataFilter.filterByFixations(allGaze);
                DataEntry validFixations = DataFilter.filterByValidity(fixations);

                
                // Write validated DataEntrys to file
                validGaze.writeToCSV(pDirectory, pName + "_valid_all_gaze");
                validFixations.writeToCSV(pDirectory, pName + "_valid_fixations");
                
                fixations.writeToCSV(pDirectory, pName + "_fixations");

                ArrayList<List<String>> descriptiveGazeMeasures = generateResults(allGaze, fixations);
                FileHandler.writeToCSV(descriptiveGazeMeasures, pDirectory, pName + "_DGMs");

                // If empty, add header row
                if (allParticipantDGMs.size() == 0) {
                    List<String> headers = descriptiveGazeMeasures.get(0);
                    headers.add(0, "Participant ID"); // Add a participant ID colum to headers
                    allParticipantDGMs.add(headers);
                }

                // Populate allParticipantDGMs with the DGMs generated for a participant
                List<String> dgms = descriptiveGazeMeasures.get(1);
                dgms.add(0, pName);
                allParticipantDGMs.add(dgms);

                // File generators
                Windows.generateWindows(allGaze, pDirectory, params.getWindowSettings());
                AreaOfInterests.generateAOIs(allGaze, pDirectory, pName);
                Sequences.generateSequenceFiles(validFixations, pDirectory, sequences);
            }

            // Batch analysis
            if (inputFiles.length > 1) {
                List<String> expandedSequences = sequences;
                List<String> collapsedSequences = new ArrayList<String>();

                for (String s : expandedSequences) {
                    collapsedSequences.add(Sequences.getCollapsedSequence(s));
                }

                ArrayList<List<String>> expandedPatterns = Patterns.discoverPatterns(expandedSequences, MIN_PATTERN_LENGTH, MAX_PATTERN_LENGTH, MIN_PATTERN_FREQUENCY, MIN_SEQUENCE_SIZE);
                ArrayList<List<String>> collapsedPatterns = Patterns.discoverPatterns(collapsedSequences, MIN_PATTERN_LENGTH, MAX_PATTERN_LENGTH, MIN_PATTERN_FREQUENCY, MIN_SEQUENCE_SIZE);
                
                String directory = params.getOutputDirectory();
                FileHandler.writeToCSV(expandedPatterns, directory, "expandedPatterns");
                FileHandler.writeToCSV(collapsedPatterns, directory, "collapsedPatterns");
                FileHandler.writeToCSV(allParticipantDGMs, directory, "combinedDGMs");
            }

            System.out.println("Analysis Complete.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // This function should only take in raw gaze data as a parameter, otherwise derived DataEntrys will be produced with incorrect data
    public static ArrayList<List<String>> generateResults(DataEntry allGaze, DataEntry fixations) {
        DataEntry validGaze = DataFilter.applyScreenSize(DataFilter.filterByValidity(allGaze), SCREEN_WIDTH, SCREEN_HEIGHT);
        DataEntry validFixations = DataFilter.applyScreenSize(DataFilter.filterByValidity(fixations), SCREEN_WIDTH, SCREEN_HEIGHT);

        // DataEntry validGaze = DataFilter.applyScreenSize(allGaze, SCREEN_WIDTH, SCREEN_HEIGHT);
        // DataEntry validFixations = DataFilter.applyScreenSize(fixations, SCREEN_WIDTH, SCREEN_HEIGHT);

        ArrayList<List<String>> results = new ArrayList<List<String>>();
        results.add(new ArrayList<String>()); //Headers
        results.add(new ArrayList<String>()); //Values

        LinkedHashMap<String,String> fixation = Fixations.analyze(validFixations);
        results.get(0).addAll(fixation.keySet());
        results.get(1).addAll(fixation.values());

        LinkedHashMap<String,String> saccades = Saccades.analyze(validFixations);
        results.get(0).addAll(saccades.keySet());
        results.get(1).addAll(saccades.values());

        LinkedHashMap<String, String> saccadeVelocity = SaccadeVelocity.analyze(validGaze);
        results.get(0).addAll(saccadeVelocity.keySet());
        results.get(1).addAll(saccadeVelocity.values());
    
        LinkedHashMap<String,String> angles = Angles.analyze(validFixations);
        results.get(0).addAll(angles.keySet());
        results.get(1).addAll(angles.values());

        LinkedHashMap<String,String> convexHull = ConvexHull.analyze(validFixations);
        results.get(0).addAll(convexHull.keySet());
        results.get(1).addAll(convexHull.values());

        LinkedHashMap<String,String> entropy = GazeEntropy.analyze(validFixations);
        results.get(0).addAll(entropy.keySet());
        results.get(1).addAll(entropy.values());

        LinkedHashMap<String,String> gaze = Gaze.analyze(validGaze);
        results.get(0).addAll(gaze.keySet());
        results.get(1).addAll(gaze.values());

        LinkedHashMap<String,String> event = Event.analyze(validGaze);
        results.get(0).addAll(event.keySet());
        results.get(1).addAll(event.values());

        return results;
    }
}
