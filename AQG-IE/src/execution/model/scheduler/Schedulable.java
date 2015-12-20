package execution.model.scheduler;

public abstract class Schedulable {

	public abstract Object getCountableObject();
	
	public boolean equals(Object o){
		return this.getCountableObject().equals(((Schedulable)o).getCountableObject());
	}
	
}
