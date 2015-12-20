package exploration.model;

import exploration.model.enumerations.AlgorithmEnum;

public class Algorithm {

	private int id;
	private AlgorithmEnum name;

	public Algorithm(int id, AlgorithmEnum algorithmEnum) {
		
		this.id = id;
		this.name = algorithmEnum;
		
	}

	public int getId() {
		
		return id;
		
	}

	public String getName() {

		return name.name();
		
	}

	public boolean equals(Object o){
		  
		return id == ((Algorithm)o).id;
	  
	}
	
	public AlgorithmEnum getEnum(){
		return name;
	}
}
