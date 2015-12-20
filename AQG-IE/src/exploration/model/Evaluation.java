package exploration.model;

import java.util.Hashtable;

import execution.model.scheduler.Schedulable;


public class Evaluation extends Schedulable {

	private static Hashtable<String, Evaluation> evaluationsTable = null;
	private Combination combination;
	private Database database;
	private Integer limit;
	private String id;

	private Evaluation(Combination combination, Database database,
			Integer limit, String id) {
		
		this.combination = combination;
		this.database = database;
		this.limit = limit;
		this.id = id;
		
	}

	public static Evaluation getEvaluation(Combination combination, Database database,
			Integer limit){
				
		String id = combination.getId()+ "-" + database.getId() + "-" + limit;
		
		Evaluation eval = getCachedEvaluation(id);
		
		if (eval == null){
			
			eval = new Evaluation(combination, database, limit, id);
			
			getEvaluationsTable().put(id,eval);
			
		}
		
		return eval;
	}
	
	private static Evaluation getCachedEvaluation(String id) {
		return getEvaluationsTable().get(id);
	}

	private static Hashtable<String, Evaluation> getEvaluationsTable() {
		
		if (evaluationsTable == null){
			evaluationsTable  = new Hashtable<String, Evaluation>();
		}
		return evaluationsTable;
	}

	public Database getEvaluableDatabase() {
		return database;
	}

	public Combination getCombination() {
		return combination;
	}

	public int getDatabaseLimit() {
		return limit;
	}

	public String getId(){
		return id;
	}
	
	public boolean equals(Object o){
		return id.equals(((Evaluation)o).id);
	}

	@Override
	public Object getCountableObject() {
		return this.database;
	}
	
	public int hashCode(){
		return id.hashCode();		
	}
}
