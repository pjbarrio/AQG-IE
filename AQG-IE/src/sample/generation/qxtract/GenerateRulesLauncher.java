package sample.generation.qxtract;

public class GenerateRulesLauncher {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String[] ie = new String[]{args[0]};//{"17","19"};
		
		String[] relExp = new String[]{/*"7","8","9","10","11",*/"12"};
		
		String tasw = args[1];
		
		String[][] collId = {/*{"sgmACE","3001","50"},{"Reuters-21578","3002","100"},*/{"TREC","3000","5000"}};
		
		for (int i = 0; i < ie.length; i++) {
			for (int j = 0; j < relExp.length; j++) {
				for (int j2 = 0; j2 < collId.length; j2++) {
					try {
						GenerateRules.main(new String[]{ie[i],relExp[j],collId[j2][0],collId[j2][1],collId[j2][2],tasw});
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
	}

}
