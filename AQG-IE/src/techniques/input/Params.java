package techniques.input;

import java.io.IOException;

import utils.SVM.RFeaturesLoader.SVMFeaturesLoader;

public class Params {

	private static String GLOBAL_SAMPLE = "GlobalSample";
	
	public static String getMinSupport(String database) {
		
		if (GLOBAL_SAMPLE.equals(database)){
			
			return "3";
			
		}
		
		return "1";
	}

	public static String getThresholdReduction(String database) {
		
		if (GLOBAL_SAMPLE.equals(database)){
			
			return "4";
			
		}
		
		return "3";
		
	}

	public static String getMinSupportAfterUpdate(String database) {
		
		if (GLOBAL_SAMPLE.equals(database)){
			
			return "3";
			
		}
		
		return "1";
		
	}

	public static String getPrecision(String database) {
		return "0.5";
	}

	public static String getMaxQuerySize(String database) {
		
		if (GLOBAL_SAMPLE.equals(database)){
			
			return "3";
			
		}
		
		return "3";
		
	}

	public static String getPerformanceThreshold(String database){
		return "0.75";
	}

	public static String getFPIncremental(String database) {
		return "0.85";
	}

	public static String getBetaEfficiency(String database) {
		return  "0.5";
	}

	public static String getMaxNumberOfQueries(String database) {
		return "400";
	}

	public static String getPowerForBenefit(String database) {
		return "1";
	}

	public static String getThresholdLost(String database) {
		return "0.85";
	}

	public static double getSVMThreshold(String smoWekaOutput) throws IOException {
		
		return SVMFeaturesLoader.getThreshold(smoWekaOutput);
	
	}

	public static String getMaximumNumberOfNeighbors() {
		return "10";
	}

	public static String getMinimumtoBeConsideredRealInCrossing(String database) {
		
		if (GLOBAL_SAMPLE.equals(database)){
			
			return "4";
			
		}
		
		return "2";
		
	}

}
