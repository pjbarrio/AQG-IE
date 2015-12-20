package sample.generation.catching;

public class Control {

	public boolean terminate;
	
	public Control() {
		
		terminate = false;
		
	}

	public void terminate() {
		terminate = true;
	}

	public boolean keepProcessing() {
		return !terminate;
	}

}
