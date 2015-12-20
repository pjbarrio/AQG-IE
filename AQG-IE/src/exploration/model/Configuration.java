package exploration.model;

import exploration.model.enumerations.ConfigurationEnum;

public class Configuration {

	private int id;
	private String description;
	private double weightCoverage;
	private double weightSpecificity;
	
	public Configuration(int id, String description, double weightCoverage, double weightSpecificity) {
		
		this.id = id;
		this.description = description;
		this.weightCoverage = weightCoverage;
		this.weightSpecificity = weightSpecificity;
		
	}

	public double getWeightCoverage() {
		return weightCoverage;
	}

	public double getWeightSpecificity() {
		return weightSpecificity;
	}

	public int getId(){
		return id;
	}

	public static Configuration generateInstance(String string) {
		
		switch (ConfigurationEnum.valueOf(string)) {
		case COVERAGE:
			
			return new Configuration(1, "COVERAGE",1,0);

		case SPECIFICITY:
			
			return new Configuration(2, "SPECIFICITY",0,1);
			
		case HYBRID:
			
			return new Configuration(3, "HYBRID",0.5,0.5);

		default:
			
			return null;
			
		}
		
	}
	
	public boolean equals(Object o){
		  
		return id == ((Configuration)o).id;
	  
	}
	
}
