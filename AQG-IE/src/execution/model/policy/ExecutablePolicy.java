package execution.model.policy;

import execution.model.scheduler.Schedulable;

public interface ExecutablePolicy {



	public boolean accepts(Schedulable o);
	

	public void addNew(Schedulable o);

	public boolean willNeverBeAccepted(Schedulable o);

	
}
