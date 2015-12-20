package sample.generation.relation;

import java.io.IOException;

public class GenerateSplitForRelSysLauncher {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		String[] ies = new String[]{args[0]};//new String[]{"17","19"};
		String[] relExp = new String[]{args[1]};//new String[]{"7","8","9","10","11","12"};
		String split = args[2];
		String tasw = args[3];
		String hasAll = args[4];
		String belowSplit = args[5];
		String cached = args[6];
		String takeCareOfSaving = args[7];
		
		String[][] coll = new String[][]{{"TREC","3000","5000"}};
		
		
		for (int i = 0; i < ies.length; i++) {
			for (int j = 0; j < relExp.length; j++) {
				for (int j2 = 0; j2 < coll.length; j2++) {
					
					generateSplits(new String[]{ies[i],relExp[j],coll[j2][0],coll[j2][1],hasAll,belowSplit, cached, takeCareOfSaving});

//					generateDocList(new String[]{ies[i],relExp[j],coll[j2][0],coll[j2][1],coll[j2][2],hasAll});
//					
//					generateArff(new String[]{ies[i],relExp[j],coll[j2][0],coll[j2][1],coll[j2][2],split,tasw});
//					
//					reduceArff(new String[]{ies[i],relExp[j],coll[j2][0],coll[j2][1],coll[j2][2],split,tasw});
					
				}
			}
		}
		
		
		
	}

	private static void reduceArff(String[] args)  throws Exception{
		
		ReduceArffSample.main(args);
		
	}

	private static void generateArff(String[] args)  throws IOException{
		
		ArffGenerator.main(args);
		
	}

	private static void generateDocList(String[] args) throws IOException {

		GenerateDocumentsList.main(args);
		
	}

	private static void generateSplits(String[] args) throws IOException {
		
		GenerateSplitForRelSys.main(args);
		
	}

}
