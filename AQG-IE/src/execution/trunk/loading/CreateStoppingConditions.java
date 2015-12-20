package execution.trunk.loading;

public class CreateStoppingConditions {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String[] T ={"SMARTCYCLIC","OPPORTUNITY","QUOTA"};
		String[] M = {"1","3","4","5"};
		String[] N = {"10","50","75","100"};
		String[] P = {"0.02","0.05","0.1","0.15","0.25"};
		String[] MP = {"0.05","0.1","0.15","0.20","025"}; //I had it as 025 :-(
		
		int parameter = 353813;
		int qpool = 7;
		for (int i1 = 0; i1 < T.length; i1++) {
			for (int i = 0; i < M.length; i++) {
				for (int j = 0; j < N.length; j++) {
					for (int j2 = 0; j2 < P.length; j2++) {
						for (int k = 0; k < MP.length; k++) {
							if (parameter >= 354525)
								System.out.println("INSERT INTO `AutomaticQueryGeneration`.`QueryPoolExecutor`" +
									"(`idQueryPoolExecutor`,`idParameter`,`Description`) " +
									"VALUES ("+qpool+","+parameter+",'"+T[i1]+".M:"+M[i]+".N:"+N[j]+".P:"+P[j2]+".MP:"+MP[k]+"');");
							
							
							parameter++;
							qpool++;
						}
						
					}
					
				}
				
			}

		}

	}

}
