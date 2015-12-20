package domain.caching.operablestructure;

import java.io.IOException;

public class CreateOperableStructureSplitLauncher {

	static String[] dbSplit = new String[]{"100"}; 
	static int i;
	static String[][] expInput;
	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

		expInput = new String[][]{{"100","7","2","100"},{"100","7","3","100"},
				{"100","9","2","100"},{"100","9","3","100"},
				{"100","11","2","100"},{"100","11","3","100"}};
		
				
		for (i = 0; i < expInput.length; i++) {

			new Thread(new Runnable(){

				
				
				@Override
				public void run() {
					try {
						CreateOperableStructureSplit.main(expInput[i]);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}
				
			}).start();

			Thread.sleep(150);
			
		}


	}

}
