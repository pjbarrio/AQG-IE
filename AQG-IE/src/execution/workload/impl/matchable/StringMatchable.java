package execution.workload.impl.matchable;


public class StringMatchable implements Matchable {
	
	private String value;

	public StringMatchable (String value){
		setValueFromString(value);
	}
	
	public StringMatchable() {
		this.value = null;
	}

	public boolean match(String m) {
		
		return value.equals(m.toLowerCase());
		
	}

	public String toString(){
	
		return value;
	
	}

	public void setValueFromString(String value) {
		
		this.value = value.toLowerCase();
		
	}
}
