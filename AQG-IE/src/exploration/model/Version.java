package exploration.model;

import java.util.Hashtable;

import execution.workload.impl.condition.TuplesCondition;
import execution.workload.impl.condition.UsefulCondition;
import execution.workload.impl.condition.WorkLoadCondition;
import exploration.model.enumerations.VersionEnum;

public class Version {

	
	private static Hashtable<Integer,Version> independentVersionTable = new Hashtable<Integer, Version>();
	private static Hashtable<Integer,Version> dependentVersionTable = new Hashtable<Integer, Version>();
	private int id;
	private VersionEnum name;
	private UsefulCondition condition;

	protected Version(int id, VersionEnum name, UsefulCondition usefulCondition) {
		
		this.id = id;
		this.name = name;
		this.condition = usefulCondition;
	}

	public int getId() {
		
		return id;
		
	}

	public String getName() {
		
		return name.name();
		
	}

	public synchronized static Version generateInstance(String name, WorkloadModel model) {
	
		switch (VersionEnum.valueOf(name)) {
		
		case INDEPENDENT:
			
			Version independentVersion = independentVersionTable.get(model.getId());
			
			if (independentVersion == null){
				independentVersion  = new Version(1,VersionEnum.INDEPENDENT,new TuplesCondition());
			
				independentVersionTable.put(model.getId(), independentVersion);
			}
			
			return independentVersion;

		case DEPENDENT:
			
			Version dependentVersion = dependentVersionTable.get(model.getId());
			
			if (dependentVersion == null){
				dependentVersion  = new Version(2,VersionEnum.DEPENDENT,new WorkLoadCondition(model.getTuples(), model.getDescription()));
			
				dependentVersionTable.put(model.getId(), dependentVersion);
			}
			
			return dependentVersion;
	
			
		default:
			
			return null;
		
		}
		
	}

	public UsefulCondition getCondition(){
		
		return condition;
	
	}
	
	public boolean equals(Object o){
		return id == ((Version)o).id;
	}
	
}
