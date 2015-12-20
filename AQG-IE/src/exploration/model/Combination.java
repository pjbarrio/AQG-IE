package exploration.model;

import java.util.List;



public class Combination {

	private Integer id;
	private Algorithm al;
	private Configuration con;
	private long time;
	private Sample sampleG;
	private Sample sampleC;
	private List<Query> queries;
	private int parameter_g;
	private int parameter_c;

	public Combination(Integer id, Algorithm al, Sample sample, long time, int parameter_g) {
		
		this(id, al, sample, null,null,time,parameter_g,0);
		
	}

	public Combination(Integer id, Algorithm al, Sample generationSample, Sample crossableSample, Configuration con, long time, int parameter_g,int parameter_c) {
		
		this.id = id;
		this.al = al;
		this.sampleG = generationSample;
		this.sampleC = crossableSample;
		this.con = con;
		this.time = time;
		this.parameter_g = parameter_g;
		this.parameter_c = parameter_c;
	}

	public Sample getCrossSample() {
		
		return sampleC;
		
	}

	public Algorithm getAlgorithm() {
		
		return al;
		
	}

	public Sample getGeneratorSample() {
		
		return sampleG;
		
	}

	public int getId() {
		
		return id;
		
	}

	public Configuration getConfiguration() {
		
		return con;
		
	}

	public long getTime() {
		return time;
	}

	public Version getVersion() {
		return sampleG.getVersion();
	}

	public WorkloadModel getWorkload() {
		
		return sampleG.getWorkload();
		
	}

	public List<Query> getQueries() {
		return queries;
	}

	public boolean equals(Object o){
		  
		return id == ((Combination)o).id;
	  
	}
	
	public void setQueries(List<Query> queries){
		
		this.queries = queries;
	
	}
	
	public int hashCode(){
		return Integer.valueOf(id).hashCode();
	}

	public int getParameterG() {
		return parameter_g;
	}
}
