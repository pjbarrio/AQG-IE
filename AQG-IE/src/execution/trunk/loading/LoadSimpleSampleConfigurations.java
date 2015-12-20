package execution.trunk.loading;

import java.util.ArrayList;
import java.util.List;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class LoadSimpleSampleConfigurations {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
//		int[] parameters = {353756,353757,353758,353759,353760,353761,353762,353764,353765,353791,353792,353794,353795,353796,353797,353798,353799,353800,353801,353802,353803,353804,353805};
		
//		int[] parameters = {353810};
		
//		int[] parameters = {353811,353812};
		
		int[] parameters = {353791,353792};
		
		int[] version = {1};
		
//		int[][] workload = {{17,1},{18,2},{19,3},{20,4},{21,5},{22,6}};
		
		int[][] workload = {{18,2}};
		
//		int[] system = {17,18,19,20};

		int[] system = {17,19};
		
//		int[] qpe = {1,2,3,4,5};
	
		int[] qpe = new int[1200];
		
		for (int i = 6; i < 1206; i++) {
			qpe[i-6] = i+1;
		}
		
//		int[] sg = {1,2,3,4,5,6,7,8,9,10,11,12,13}; 
		
		int[] sg = {1};
		
//		int[] ua = {0,1};
		
		int[] ua = {1};
		
		int[] act = {0};
		
		int[] rpq = {10,20,30,40,50};
		
//		int[][] ufulca = {{400,0,1000,10000,0},{400,0,1000,10000,1},{400,400,1000,10000,0},{400,400,1000,10000,1}};
		
		int[][] ufulca = {{0,400,1000,10000,1}};
		
		List<int[]> sc = new ArrayList<int[]>();
		
		for (int par = 0; par < parameters.length; par++) {
			for (int ver = 0; ver < version.length; ver++) {
				for (int wor = 0; wor < workload.length; wor++) {
					for (int ext = 0; ext < system.length; ext++) {
						for (int qpei = 0; qpei < qpe.length; qpei++) {
							for (int sgi = 0; sgi < sg.length; sgi++) {
								for (int uai = 0; uai < ua.length; uai++) {
									for (int acti = 0; acti < act.length; acti++) {
										for (int rpqi = 0; rpqi < rpq.length; rpqi++) {
											for (int li = 0; li < ufulca.length; li++) {
												sc.add(new int[]{parameters[par],version[ver],workload[wor][0],workload[wor][1],system[ext],qpe[qpei],sg[sgi],ua[uai],act[acti],rpq[rpqi],
														ufulca[li][0],ufulca[li][1],ufulca[li][2],ufulca[li][3],ufulca[li][4]});
											}
										}
									}
								}
							}	
						}	
					}	
				}	
			}	
		}
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		pW.writeSampleConfigurations(sc);
		
	}

}
