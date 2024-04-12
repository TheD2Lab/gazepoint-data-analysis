package analysis;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SaccadeVelocity {
    final static String TIME_INDEX = "TIME";
    final static String FIXATIONID_INDEX = "FPOGID";
    final static String FIXATIONX_INDEX = "FPOGX";
    final static String FIXATIONY_INDEX = "FPOGY";
    final static String FIXATION_VALIDITY_INDEX = "FPOGV";

    static public LinkedHashMap<String,String> analyze(DataEntry data) {
        LinkedHashMap<String,String> results = new LinkedHashMap<String,String>();

        List<List<Double[]>> positionProfiles = new ArrayList<List<Double[]>>();
        List<Double[]> positionProfile = new ArrayList<Double[]>();
        ArrayList<Double> peakSaccadeVelocities = new ArrayList<Double>();

        String prevID = "";
        for (int i = 0; i < data.rowCount(); i++) {
            boolean saccade = Integer.parseInt(data.getValue(FIXATION_VALIDITY_INDEX, i)) == 0 ? true : false;
            if (saccade) {                
                // Check to see if these saccade points are part of the same saccade
                String currID = data.getValue(FIXATIONID_INDEX, i);
                if (!prevID.equals(currID) && positionProfile.size() != 0) {
                    positionProfiles.add(positionProfile);
                    positionProfile = new ArrayList<Double[]>();
                    prevID = currID;
                }

                Double x = Double.parseDouble(data.getValue(FIXATIONX_INDEX, i));
                Double y = Double.parseDouble(data.getValue(FIXATIONY_INDEX, i));
                Double t = Double.parseDouble(data.getValue(TIME_INDEX, i));
                positionProfile.add(new Double[] {x, y, t});
                
            } else if (positionProfile.size() != 0) {
                positionProfiles.add(positionProfile);
                positionProfile = new ArrayList<Double[]>();
            }
        }

        for (int i = 0; i < positionProfiles.size(); i++) {
            List<Double[]> saccadePoints = positionProfiles.get(i);
            Double peakSaccadeVelocity = getPeakVelocity(saccadePoints);
            if (!Double.isNaN(peakSaccadeVelocity)) peakSaccadeVelocities.add(peakSaccadeVelocity);
        }

        results.put(
            "Average Peak Saccade Velocity",
            String.valueOf(DescriptiveStats.getMeanOfDoubles(peakSaccadeVelocities))
        );

        return results;
    }


    /*
	 * Returns the peak velocity of a given saccade calculated using a two point central difference algorithm.
	 * 
	 * @param	saccadePoints	A list of saccade data points, where each data point is a double array. 
	 * 							[0] = X position
	 * 							[1] = Y position
	 * 							[2] = Time of data point
	 * 
	 * @return	The peak velocity of a saccade
	 */
	public static double getPeakVelocity(List<Double[]> saccadePoints) {
		if (saccadePoints.size() == 0 || saccadePoints.size() == 1) {
			return Double.NaN;
		}
		
		double peakVelocity = 0;
		double conversionRate = 0.0264583333; // Convert from pixels to cms
		double velocityThreshold = 700; // Maximum possible saccadic velocity
        int participantDistance = 65; // assume an average distance of 65cm from the participant to the screen
		
		for (int i = 1; i < saccadePoints.size(); i++) {
			Double[] currPoint = saccadePoints.get(i);
			Double[] prevPoint = saccadePoints.get(i - 1);

			double x1 = currPoint[0];
			double y1 = currPoint[1];
			double x2 = prevPoint[0];
			double y2 = prevPoint[1];
			
			double dx = Math.sqrt(Math.pow(Math.abs(x1 - x2), 2) + Math.pow(Math.abs(y1 - y2), 2)) * conversionRate;
			double dt = Math.abs(currPoint[2] - prevPoint[2]);
			double amplitude = 180/Math.PI * Math.atan(dx/participantDistance);
			
			double velocity = amplitude/dt;
			
			if (velocity > peakVelocity && velocity <= velocityThreshold) {
				peakVelocity = velocity;
			}
		}
		
		return peakVelocity;
	}
}
