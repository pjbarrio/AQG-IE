package execution.trunk.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import execution.workload.tuple.Tuple;
import execution.workload.tuple.TupleReader;

import sample.generation.relation.TuplesGenerator;
import utils.id.TuplesLoader;
import utils.persistence.PersistenceImplementation;
import utils.persistence.databaseWriter;
import utils.persistence.persistentWriter;
import utils.word.extraction.WordExtractor;
import utils.word.extraction.WordTokenizer;

public class AbstractVSConcreteTuples {

	/**
	 * @param args
	 * @throws SQLException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws SQLException, FileNotFoundException {
		
		System.setOut(new PrintStream(new File("/local/pjbarrio/Files/ff.txt")));
		
		System.out.println("Showing ~TASW and TASW");
		
		databaseWriter dW = (databaseWriter)PersistenceImplementation.getWriter();
		
		ResultSet result = dW.runQuery("select distinct idDatabase, idDocument, tupleString from tmpT11");

		Map<Integer,Map<Integer,List<Tuple>>> map1 = createMap(result);
		
		result = dW.runQuery("select distinct idDatabase, idDocument, tupleString from tmpT12");

		Map<Integer,Map<Integer,List<Tuple>>> map2 = createMap(result);
		
		System.out.println("# DBs");
		
		System.out.println(map1.size());
		System.out.println(map2.size());
		
		System.out.println("# Docs");
		
		System.out.println(getNumDocs(map1));
		System.out.println(getNumDocs(map2));
		
		System.out.println("# Words in PERSON");
		
		Set<String> p1 = getWords(new String[]{"PERSON"}, map1);
		Set<String> p2 = getWords(new String[]{"PERSON"}, map2);
		
		System.out.println(p1.size());
		System.out.println(p2.size());

		System.out.println("# Words in CAREER");
		
		Set<String> c1 = getWords(new String[]{"CAREER"}, map1);
		Set<String> c2 = getWords(new String[]{"CAREER"}, map2);
		
		System.out.println(c1.size());
		System.out.println(c2.size());

		System.out.println("# Words in ALL");
		
		Set<String> pc1 = getWords(new String[]{"CAREER","PERSON"}, map1);
		Set<String> pc2 = getWords(new String[]{"CAREER","PERSON"}, map2);
		
		System.out.println(pc1.size());
		System.out.println(pc2.size());
		
//		System.out.println("Unique words PERSON");
//		
//		Set<String> bkpp1 = new HashSet<String>(p1);
//		bkpp1.removeAll(p2);
//		
//		Set<String> bkpp2 = new HashSet<String>(p2);
//		bkpp2.removeAll(p1);
//		
//		System.out.println(bkpp1.size() + bkpp1.toString());
//		System.out.println(bkpp2.size() + bkpp2.toString());
//		
//		System.out.println("Unique words CAREER");
//		
//		Set<String> bkpc1 = new HashSet<String>(c1);
//		bkpc1.removeAll(c2);
//		
//		Set<String> bkpc2 = new HashSet<String>(c2);
//		bkpc2.removeAll(c1);
//		
//		System.out.println(bkpc1.size() + bkpc1.toString());
//		System.out.println(bkpc2.size() + bkpc2.toString());
//
//		System.out.println("Unique words PERSON,CAREER");
//		
//		Set<String> bkppc1 = new HashSet<String>(pc1);
//		bkppc1.removeAll(pc2);
//		
//		Set<String> bkppc2 = new HashSet<String>(pc2);
//		bkppc2.removeAll(pc1);
//		
//		System.out.println(bkppc1.size() + bkppc1.toString());
//		System.out.println(bkppc2.size() + bkppc2.toString());

	
		System.out.println("#Comparison of tuples");
		
		Map<Integer,Set<Tuple>> dbs = createMap(map2);		
		
		for (Entry<Integer,Set<Tuple>> db1 : dbs.entrySet()) {
			
			System.out.print(db1.getKey() + ","+ db1.getValue().size());
			
			for (Entry<Integer,Set<Tuple>> db2 : dbs.entrySet()) {
				
				System.out.print("," + getOverlap(new HashSet<Tuple>(db1.getValue()),new HashSet<Tuple>(db2.getValue())));
				
			}
			
			System.out.println("");
			
		}
		
		
	}

	private static double getOverlap(Set<Tuple> l1, Set<Tuple> l2) {
		
		double size = l1.size();
		
		l1.removeAll(l2);
		
		return 1.0 - ((double)l1.size() / size);
		
	}

	private static Map<Integer, Set<Tuple>> createMap(
			Map<Integer, Map<Integer, List<Tuple>>> map1) {
		
		Map<Integer,Set<Tuple>> ret = new HashMap<Integer, Set<Tuple>>();
		
		for (Entry<Integer, Map<Integer, List<Tuple>>> m : map1.entrySet()) {
			
			ret.put(m.getKey(), getList(m.getValue().values()));
			
		}
		
		return ret;
	}

	private static Set<Tuple> getList(Collection<List<Tuple>> values) {
		
		Set<Tuple> ret = new HashSet<Tuple>();
		
		for (List<Tuple> tuples : values) {
			
			ret.addAll(tuples);
			
		}
		
		return ret;
		
	}


	private static Set<String> getWords(String[] att,
			Map<Integer, Map<Integer, List<Tuple>>> map) {
		
		WordTokenizer wt = new WordTokenizer();
		
		Set<String> words = new HashSet<String>();
		
		for (Map<Integer, List<Tuple>> values : map.values()) {
			
			for (List<Tuple> tuples : values.values()) {
				
				for (int i = 0; i < tuples.size(); i++) {
					
					for (int j = 0; j < att.length; j++) {
						
						String text = tuples.get(i).getFieldValue(att[j]);
						
						if (text!= null)
							words.addAll(Arrays.asList(wt.getWords(text)));
						
					}
					
				}
				
			}
			
		}
		
		return words;
		
	}

	private static int getNumDocs(
			Map<Integer, Map<Integer, List<Tuple>>> map) {
		
		int ret = 0;
		
		for (Map<Integer, List<Tuple>> values : map.values()) {
			
			ret += values.size();
			
		}
		
		return ret;
	}

	private static Map<Integer, Map<Integer, List<Tuple>>> createMap(ResultSet result) throws SQLException {
		
		Map<Integer, Map<Integer, List<Tuple>>> ret = new HashMap<Integer, Map<Integer,List<Tuple>>>();
		
		while (result.next()){
			
			int db = result.getInt(1);
			int doc = result.getInt(2);
			Tuple tup = TupleReader.generateTuple(result.getString(3).trim());
			Map<Integer, List<Tuple>> map = ret.get(db);
			
			if (map == null){
				
				map = new HashMap<Integer, List<Tuple>>();
				
				ret.put(db, map);
				
			}
			
			List<Tuple> list = map.get(doc);
			
			if (list == null){
				list = new ArrayList<Tuple>();
				map.put(doc, list);
			}
			
			list.add(tup);
			
		}
		
		return ret;
		
	}

}
