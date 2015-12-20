package domain.caching.tuple.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.axis.encoding.Base64;
import org.apache.tools.ant.filters.StringInputStream;

import execution.workload.tuple.Tuple;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class TestInternalTuples {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		Map<Integer, List<String>> tupleMap = pW.getInternalTupleMap();
		
		for (Entry<Integer, List<String>> entry : tupleMap.entrySet()) {
			
			System.out.println("Database: " + entry.getKey());
			
			for (int i = 0; i < entry.getValue().size(); i++) {
				
				try {
					
					InputStream buffer = new ByteArrayInputStream(Base64.decode(entry.getValue().get(i)));
					ObjectInput input = new ObjectInputStream ( buffer );
					List<Tuple> tuples = (List<Tuple>)input.readObject();
					buffer.close();
					input.close();
					
					if (!tuples.isEmpty())
						System.out.println(tuples.toString());
					
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		}

	}

}
