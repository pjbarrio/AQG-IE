package execution.trunk.test;

import java.util.concurrent.Semaphore;

public class SemaphoreTester {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		
		Semaphore s = new Semaphore(4);
		
		for (int i = 0; i < 10; i++) {
			
			s.acquire();
			
			System.out.println(i);
			
		}
		

	}

}
