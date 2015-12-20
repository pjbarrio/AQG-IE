package domain.caching.tuple;

import java.io.IOException;

import domain.caching.operablestructure.CreateOperableStructureSplit;

public class CreateTuplesSplitLauncher {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		String[] dbSplit = new String[]{"100"}; 		
		for (int i = 0; i < dbSplit.length; i++) {

			CreateTuplesSplit.main(new String[]{dbSplit[i],"7","2","100","false"});
			CreateTuplesSplit.main(new String[]{dbSplit[i],"7","3","100","false"});
//			CreateTuplesSplit.main(new String[]{dbSplit[i],"8","2","100","false"});
//			CreateTuplesSplit.main(new String[]{dbSplit[i],"8","3","100","false"});
			CreateTuplesSplit.main(new String[]{dbSplit[i],"9","2","100","false"});
			CreateTuplesSplit.main(new String[]{dbSplit[i],"9","3","100","false"});
//			CreateTuplesSplit.main(new String[]{dbSplit[i],"10","2","100","false"});
//			CreateTuplesSplit.main(new String[]{dbSplit[i],"10","3","100","false"});
			CreateTuplesSplit.main(new String[]{dbSplit[i],"11","2","100","false"});
			CreateTuplesSplit.main(new String[]{dbSplit[i],"11","3","100","false"});
//			CreateTuplesSplit.main(new String[]{dbSplit[i],"12","2","100","false"});
//			CreateTuplesSplit.main(new String[]{dbSplit[i],"12","3","100","false"});

		}		

	}

}
