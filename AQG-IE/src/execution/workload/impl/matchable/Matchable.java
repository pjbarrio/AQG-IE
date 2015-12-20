package execution.workload.impl.matchable;

public interface Matchable {
	public boolean match(String m);

	public void setValueFromString(String fieldValue);
}
