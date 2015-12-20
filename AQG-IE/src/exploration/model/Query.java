package exploration.model;

import searcher.interaction.formHandler.TextQuery;
import execution.model.scheduler.Schedulable;

public class Query extends Schedulable{

	private TextQuery text;
	private long time;
	private int position;
	private String id;

	public Query(Combination combination,TextQuery text, long time, int position) {

		this.text = text;
		this.time = time;
		this.position = position;
		
		this.id = combination.getId() + "-" + position;
	}

	public TextQuery getText() {

		return text;
	
	}

	public int getPosition() {
		
		return position;
		
	}

	public String getId() {
		
		return id;
		
	}

	public long getGenerationTime() {
		
		return time;
	
	}

	public boolean equals(Object o){
		return id.equals(((Query)o).id);
	}

	@Override
	public Object getCountableObject() {
		return this;
	}
	
	public int hashCode(){
		return id.hashCode();
	}
}
