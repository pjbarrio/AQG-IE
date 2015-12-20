package exploration.model;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class WorkloadModel {

	private int id;
	private String description;
	private String tuples;
	private String queries;
	private String relations;
	private String[] relationsArray;

	public WorkloadModel(int id, String description, String tuples, String queries, String... relations) {
		super();
		this.id = id;
		this.description = description;
		this.tuples = tuples;
		this.queries = queries;
		this.relationsArray = relations;
	}
	
	public int getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public String getTuples() {
		return tuples;
	}

	public String getQueriesFile() {
		return queries;
	}

	public boolean equals(Object o){
		return id == ((WorkloadModel)o).id;
	}

	public String getRelationsFile() {
		return relations;
	}

	public String[] getRelations() {
		
		return relationsArray;
	}
	
}
