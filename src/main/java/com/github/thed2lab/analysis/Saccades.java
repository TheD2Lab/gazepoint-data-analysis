package com.github.thed2lab.analysis;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Saccades {
    final static String DURATION_INDEX = "FPOGD";
    final static String TIMESTAMP_INDEX = "FPOGS";
    final static String FIXATIONID_INDEX = "FPOGID";
    final static String FIXATIONX_INDEX = "FPOGX";
    final static String FIXATIONY_INDEX = "FPOGY";
    
    static public LinkedHashMap<String,String> analyze(DataEntry data) {
        LinkedHashMap<String,String> results = new LinkedHashMap<String,String>();

        ArrayList<Double> allFixationDurations = new ArrayList<>();
        ArrayList<Double[]> saccadeDetails = new ArrayList<>();
        ArrayList<Coordinate> allCoordinates = new ArrayList<>();
        
        for (int row = 0; row < data.rowCount(); row++) {
            Double fixationDurationSeconds = Double.valueOf(data.getValue(DURATION_INDEX, row));;

            Double[] eachSaccadeDetail = new Double[3];
            eachSaccadeDetail[0] = Double.valueOf(data.getValue(TIMESTAMP_INDEX, row));
            eachSaccadeDetail[1] = Double.valueOf(data.getValue(DURATION_INDEX, row));
            eachSaccadeDetail[2] = Double.valueOf(data.getValue(FIXATIONID_INDEX, row));
            saccadeDetails.add(eachSaccadeDetail);

            Coordinate eachCoordinate = new Coordinate(
                Double.valueOf(data.getValue(FIXATIONX_INDEX, row)),
                Double.valueOf(data.getValue(FIXATIONY_INDEX, row)),
                Integer.valueOf(data.getValue(FIXATIONID_INDEX, row))
            );
            allCoordinates.add(eachCoordinate);
            allFixationDurations.add(fixationDurationSeconds);
        }
        
        Double[] allSaccadeLengths = getAllSaccadeLengths(allCoordinates);
        ArrayList<Double> allSaccadeDurations = getAllSaccadeDurations(saccadeDetails);

        
        results.put(
            "total_number_of_saccades", //Output Header
            String.valueOf(allSaccadeLengths.length) //Output Value
            );

        results.put(
            "sum_of_all_saccade_lengths", 
            String.valueOf(DescriptiveStats.getSum(allSaccadeLengths))
            );
        
        results.put(
            "mean_saccade_length", 
            String.valueOf(DescriptiveStats.getMean(allSaccadeLengths))
            );
        
        results.put(
            "median_saccade_length", 
            String.valueOf(DescriptiveStats.getMedian(allSaccadeLengths))
            );
        
        results.put(
            "stdev_of_saccade_lengths", 
            String.valueOf(DescriptiveStats.getStDev(allSaccadeLengths))
            );
        
        results.put(
            "min_saccade_length", 
            String.valueOf(DescriptiveStats.getMin(allSaccadeLengths))
            );
        
        results.put(
            "max_saccade_length", 
            String.valueOf(DescriptiveStats.getMax(allSaccadeLengths))
            );

        results.put(
            "sum_of_all_saccade_durations", 
            String.valueOf(DescriptiveStats.getSumOfDoubles(allSaccadeDurations))
            );
        
        results.put(
            "mean_saccade_duration", 
            String.valueOf(DescriptiveStats.getMeanOfDoubles(allSaccadeDurations))
            );
        
        results.put(
            "median_saccade_duration", 
            String.valueOf(DescriptiveStats.getMedianOfDoubles(allSaccadeDurations))
            );

        results.put(
            "stdev_of_saccade_durations", 
            String.valueOf(DescriptiveStats.getStDevOfDoubles(allSaccadeDurations))
            );
                
        results.put(
            "min_saccade_duration", 
            String.valueOf(DescriptiveStats.getMinOfDoubles(allSaccadeDurations))
            );

        results.put(
            "max_saccade_duration", 
            String.valueOf(DescriptiveStats.getMaxOfDoubles(allSaccadeDurations))
            );
            
        results.put(
            "scanpath_duration", 
            String.valueOf(getScanpathDuration(allFixationDurations, allSaccadeDurations))
            );

        results.put(
            "fixation_to_saccade_ratio", 
            String.valueOf(getFixationToSaccadeRatio(allFixationDurations, allSaccadeDurations))
            );

        return results;
    } 
    

    public static Double[] getAllSaccadeLengths(ArrayList<Coordinate> allCoordinates) {
        if (allCoordinates.size() == 0) return new Double[0];

		ArrayList<Double> allSaccadeLengths = new ArrayList<Double>();
		int objectSize = allCoordinates.size();
		Double[] allLengths = new Double[(objectSize-1)];
		for(int i=0; i<objectSize; i++){
			Coordinate earlyCoordinate = allCoordinates.get(i);

			if(i+1<objectSize){
				Coordinate laterCoordinate = allCoordinates.get(i+1);
				if (earlyCoordinate.fid == laterCoordinate.fid - 1)
					allSaccadeLengths.add(Math.sqrt(Math.pow((laterCoordinate.x - earlyCoordinate.x), 2) + Math.pow((laterCoordinate.y - earlyCoordinate.y), 2)));
			}
		}
		
		allLengths = new Double[allSaccadeLengths.size()];
		return allSaccadeLengths.toArray(allLengths);
	}

    public static ArrayList<Double> getAllSaccadeDurations(ArrayList<Double[]> saccadeDetails){
        if (saccadeDetails.size() == 0) return new ArrayList<Double>();

		ArrayList<Double> allSaccadeDurations = new ArrayList<>();
		for (int i=0; (i+1)<saccadeDetails.size(); i++){
			Double[] currentDetail = (Double[]) saccadeDetails.get(i);
			Double[] subsequentDetail = (Double[]) saccadeDetails.get(i+1);
			
			if (currentDetail[2] == subsequentDetail[2] - 1) {
				double currentTimestamp = currentDetail[0];
				double currentFixationDuration = currentDetail[1];
				double subsequentTimestamp = subsequentDetail[0];

				double eachSaccadeDuration = subsequentTimestamp - (currentTimestamp + currentFixationDuration);

				allSaccadeDurations.add(eachSaccadeDuration);
			}
		}
		return allSaccadeDurations;
	}

	public static double getScanpathDuration(ArrayList<Double> allFixationDurations, ArrayList<Double> allSaccadeDurations) {
		double fixationDuration = DescriptiveStats.getSumOfDoubles(allFixationDurations);
		double saccadeDuration = DescriptiveStats.getSumOfDoubles(allSaccadeDurations);
		return fixationDuration + saccadeDuration;
	}

	public static double getFixationToSaccadeRatio(ArrayList<Double> allFixationDurations, ArrayList<Double> allSaccadeDurations){
        if (allSaccadeDurations.size() == 0) return Double.NaN;

		double fixationDuration = DescriptiveStats.getSumOfDoubles(allFixationDurations);
		double saccadeDuration = DescriptiveStats.getSumOfDoubles(allSaccadeDurations);
		return fixationDuration/saccadeDuration;
	}
}