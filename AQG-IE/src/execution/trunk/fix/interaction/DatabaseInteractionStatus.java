package execution.trunk.fix.interaction;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import exploration.model.Database;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class DatabaseInteractionStatus {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		
		String prefix = "/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/" + getSearchRound();
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		List<Database> databases = pW.getSamplableDatabases(null);
		
		int brokens = 1;
		
		Map<Integer,String> mapOtherQueries = new HashMap<Integer, String>();
		
		List<Thread> threads = new ArrayList<Thread>();
		
		List<Integer> brk = new ArrayList<Integer>();
		
		for (int i = 0; i < databases.size(); i++) {
			
			boolean broken = false;
			
			try {

				int length = new File(prefix,Integer.toString(databases.get(i).getId())).list().length;
				
				if (length < 3){
					System.err.println(brokens + " - Broken: " + databases.get(i).getId());
					brokens++;
					broken = true;
					brk.add(databases.get(i).getId());
				}
			} catch (Exception e) {
				System.err.println(brokens + " - Completely Broken: " + databases.get(i).getId());
				brokens++;
				brk.add(databases.get(i).getId());


			}
			
			if (broken){
				
				//See if some queries returned results (have nexts):
				
				Database d = databases.get(i);
					
				File[] queries = new File(new File(prefix,Integer.toString(d.getId())),"/QUERY/CHNH").listFiles().clone();
				
				Thread t = new Thread(new ExplorerRunnable(d,queries,mapOtherQueries));
				
				threads.add(t);
				
				t.start();
				
			}
			
		}
		
		for (int i = 0; i < threads.size(); i++) {
			threads.get(i).join();
		}
		
		for (Entry<Integer,String> entry : mapOtherQueries.entrySet()) {
			
			System.out.println("hasOthers: " + entry.getKey() + "," + entry.getValue());
			
		}
		
		System.out.print("(" + brk.get(0));
		
		for (int i = 1; i < brk.size(); i++) {
			
			System.out.print("," + brk.get(i));
			
		}
		
		System.out.println(")");
	}

	private static int getSearchRound() {
		return 1;
	}

}


/* OUTPUT

hasOthers: 956,covered
hasOthers: 1102,io
hasOthers: 823,left_man_well
hasOthers: 1760,clementine
hasOthers: 1371,vice
hasOthers: 2741,left_man_well
hasOthers: 1774,clementine
hasOthers: 404,targets
hasOthers: 553,sunday_trees_small
hasOthers: 1236,castro
hasOthers: 1109,red
hasOthers: 814,ots
hasOthers: 1346,io
hasOthers: 1347,grain
hasOthers: 1791,rica
hasOthers: 1482,centro
hasOthers: 1104,enraged
hasOthers: 2333,clementine
hasOthers: 790,io
hasOthers: 2580,sunday_trees_small
hasOthers: 927,sunday_trees_small
hasOthers: 168,sunday_trees_small
hasOthers: 923,wyeth
hasOthers: 651,io
hasOthers: 524,omelets
hasOthers: 2708,sunday_trees_small
hasOthers: 1600,ultimately
hasOthers: 2830,omelets
hasOthers: 2464,vice
hasOthers: 312,scrolling
hasOthers: 161,sunday_trees_small
hasOthers: 914,est
hasOthers: 641,sunday_trees_small
hasOthers: 2348,select
hasOthers: 1528,sunday_trees_small
hasOthers: 2585,sunday_trees_small
hasOthers: 1606,built
hasOthers: 2700,jammed
hasOthers: 670,sunday_trees_small
hasOthers: 53,evolved
hasOthers: 2575,scrolling
hasOthers: 899,sunday_trees_small
hasOthers: 2267,scrolling
hasOthers: 2258,sunday_trees_small
hasOthers: 1705,sunday_trees_small
hasOthers: 1597,shoes
hasOthers: 1713,times_family_father
hasOthers: 321,built
hasOthers: 2787,est
hasOthers: 331,sunday_trees_small
hasOthers: 2789,cal
hasOthers: 2167,sunday_trees_small
hasOthers: 101,black
hasOthers: 580,sunday_trees_small
hasOthers: 2536,soil
hasOthers: 1342,writer_fly
hasOthers: 231,products
hasOthers: 2532,people_left_economy
hasOthers: 105,scrolling
hasOthers: 1680,ultimately
hasOthers: 2552,point
hasOthers: 1922,sunday_trees_small
hasOthers: 2629,sunday_trees_small
hasOthers: 1326,evolved
hasOthers: 2144,sunday_trees_small
hasOthers: 355,sunday_trees_small
hasOthers: 1205,sunday_trees_small
hasOthers: 2633,select
hasOthers: 244,left_man_well
hasOthers: 1215,select
hasOthers: 2759,ad
(1371,2197,1591,823,1236,956,2830,2175,2335,2464,2633,2545,473,1724,1103,2348,1597,649,359,2580,2782,2016,1791,2789,136,105,2536,651,1321,1528,670,1346,2499,790,2835,1347,1958,1760,1342,1671,2839,769,135,1871,1879,2333,1600,553,244,116,321,1875,1042,101,1295,331,508,1590,2147,36,312,927,1215,2133,2662,1705,1482,2629,2575,231,2169,1104,139,580,2552,2267,2700,2585,767,2144,2675,1046,1269,1485,1959,2768,1207,1774,2554,2532,161,898,766,1387,20,2709,2787,814,2167,2722,1205,1101,2708,1821,1606,2379,1745,2759,524,1109,232,2731,1680,1579,567,696,2258,1816,460,2004,2388,1362,1102,355,496,820,1326,1713,1553,387,2522,899,2067,2254,2741,53,761,168,2848,796,404,51,1922,2401,2193,641,1268,923,1191,93,914,6)

*/