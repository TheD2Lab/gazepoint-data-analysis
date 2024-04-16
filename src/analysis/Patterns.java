package analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Patterns {
    public static ArrayList<List<String>> discoverPatterns(List<String> sequences, int minPatternLength, int maxPatternLength, int minFrequency, int minSequenceSize) {
        ArrayList<List<String>> results = new ArrayList<List<String>>();
        results.add(Arrays.asList(new String[] {"Pattern String", "Frequency", "Sequence Support", "Average Pattern Frequency", "Proportional Pattern Frequency"})); 

        for (int patternLength = minPatternLength; patternLength <= maxPatternLength; patternLength++) {
            int totalPatternCount = 0;
            HashMap<String, Integer> frequencyMap = new HashMap<String, Integer>();
            HashMap<String, ArrayList<String>> sequenceMap = new HashMap<String, ArrayList<String>>();
            
            for (String s: sequences) {
                for (int i = 0; i < s.length() - patternLength; i++) {
                    String patternString = s.substring(i, i + patternLength);
                    totalPatternCount++;
    
                    int count = frequencyMap.containsKey(patternString) ? frequencyMap.get(patternString) + 1 : 1;
                    frequencyMap.put(patternString, count);
    
                    if (!sequenceMap.containsKey(patternString)) sequenceMap.put(patternString, new ArrayList<String>());
                    if (!sequenceMap.get(patternString).contains(s)) sequenceMap.get(patternString).add(s);
                }
            }
            
            for (String pattern: frequencyMap.keySet()) {
                int frequency = frequencyMap.get(pattern);
                double sequenceSupport = (double) sequenceMap.get(pattern).size()/sequences.size();
                double averagePatternFrequency = (double) frequencyMap.get(pattern)/sequences.size();
                double proportionalPatternFrequency = (double) frequencyMap.get(pattern)/totalPatternCount;
                
                // System.out.print(pattern + " ");
                // System.out.print(frequency + " ");
                // System.out.print(sequenceSupport + " ");
                // System.out.print(averagePatternFrequency + " ");
                // System.out.println(proportionalPatternFrequency + " ");
                
                if (frequency >= minFrequency && sequenceMap.get(pattern).size() >= minSequenceSize) {
                    List<String> patternData = new ArrayList<String>();
                    patternData.add(pattern);
                    patternData.add(String.valueOf(frequency));
                    patternData.add(String.valueOf(sequenceSupport));
                    patternData.add(String.valueOf(averagePatternFrequency));
                    patternData.add(String.valueOf(proportionalPatternFrequency));
                    results.add(patternData);
                }
            }
        }
        
        return results;
    }
}
