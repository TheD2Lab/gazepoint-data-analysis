package analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class GazeEntropy {
    final static String AOI_INDEX = "AOI";

    static public LinkedHashMap<String,String> analyze(DataEntry data) {
        LinkedHashMap<String,String> results = new LinkedHashMap<String,String>();

        HashMap<String, Double> aoiProbability = new HashMap<String, Double>();
        HashMap<String, HashMap<String, Double>> transitionProbability = new HashMap<String, HashMap<String, Double>>();
        ArrayList<String> aoiSequence = new ArrayList<String>();
        String lastAoi = "";
        
        ArrayList<Double> allFixationDurations = new ArrayList<>();
        int fixationCount = data.rowCount();
        
        for (int row = 0; row < data.rowCount(); row++) {
            String aoi = data.getValue(AOI_INDEX, row);
            aoiSequence.add(aoi);
            
            if (aoi.equals(""))
                continue;
            else if (aoiProbability.containsKey(aoi)) {
                aoiProbability.put(aoi, aoiProbability.get(aoi) + 1);
                if (!lastAoi.equals("")) {
                    HashMap<String, Double> relationMatrix = transitionProbability.get(lastAoi);
                    if (relationMatrix.containsKey(aoi)) {
                        double count = relationMatrix.get(aoi);
                        relationMatrix.put(aoi, count + 1);
                    } else {
                        relationMatrix.put(aoi, 1.0);
                    }
                }
                
            } else {
                aoiProbability.put(aoi, 1.0);
                transitionProbability.put(aoi, new HashMap<String,Double>());
            }
            lastAoi = aoi;
        }

        for (Map.Entry<String, Double> entry : aoiProbability.entrySet()) {
            Double AOIFixationCount = entry.getValue();
            Double probability = AOIFixationCount/fixationCount;
            entry.setValue(probability);
        }
        
        
        for (Map.Entry<String, HashMap<String, Double>> entry : transitionProbability.entrySet()) {
            int aoiTransitions = 0;
            for (Map.Entry<String, Double> edge : entry.getValue().entrySet()) {
                aoiTransitions += edge.getValue();
            }
            for (Map.Entry<String, Double> edge : entry.getValue().entrySet()) {
                edge.setValue(edge.getValue()/aoiTransitions);
            }
        }

        results.put(
            "stationary entropy", //Output Header
            String.valueOf(getStationaryEntropy(aoiProbability)) //Output Value
            );

        results.put(
            "transition entropy", //Output Header
            String.valueOf(getTransitionEntropy(aoiProbability, transitionProbability)) //Output Value
            );
    
        return results;
    }

    public static double getStationaryEntropy(HashMap<String, Double> aoiProbability) {
		double stationaryEntropy = 0;
		for (Map.Entry<String, Double> entry : aoiProbability.entrySet()) {
			double probability = entry.getValue();
			stationaryEntropy += -probability * Math.log10(probability);
		};
		
		return stationaryEntropy;
	}
	
	public static double getTransitionEntropy(HashMap<String, Double> aoiProbability, HashMap<String,HashMap<String,Double>> transitionMatrix){	
    	double transitionEntropy = 0;
		for (Map.Entry<String,HashMap<String,Double>> entry : transitionMatrix.entrySet()) {
    		double pijSum = 0;
    		for (Map.Entry<String, Double> edge : entry.getValue().entrySet()) {
    			pijSum += edge.getValue() * Math.log10(edge.getValue());
    		}
    		transitionEntropy += pijSum * -aoiProbability.get(entry.getKey());
    	}
		
		return transitionEntropy;
	}
}
