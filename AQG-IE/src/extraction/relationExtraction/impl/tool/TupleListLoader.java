package extraction.relationExtraction.impl.tool;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.persistence.PersistenceImplementation;
import utils.persistence.databaseWriter;

import com.google.gdata.util.common.util.Base64;
import com.google.gdata.util.common.util.Base64DecoderException;

import execution.workload.tuple.Tuple;
import exploration.model.Document;

public class TupleListLoader {

	/**
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException {
		
		String relation = "PersonCareer";
		
		databaseWriter dw = (databaseWriter)PersistenceImplementation.getWriter();
		
		ResultSet rs = dw.runQuery("select * from tmpSSK"+relation+"Full;");
		
		Map<Integer,List<Tuple>> map = new HashMap<Integer, List<Tuple>>();
		
		int i = 0;
		
		while (rs.next()){

			if (i++ % 1000 == 0)
				System.out.print(".");
			
			int id = rs.getInt(1);

			List<Tuple> t = getValue(rs.getString(2));
			
			map.put(id, t);
			
		}
		
		try{
			//use buffering


			OutputStream file = new FileOutputStream( "/proj/dbNoBackup/pjbarrio/workspacedb-pc02/ResourceSelectionIE/data/extraction/MapTREC" + relation + ".ser" );
			OutputStream buffer = new BufferedOutputStream( file );
			ObjectOutput output = new ObjectOutputStream( buffer );

			try{
				output.writeObject(map);
			}
			finally{
				output.close();
			}
		}  
		catch(IOException ex){
			ex.printStackTrace();
		}

	}

	private static List<Tuple> getValue(String value) {
		
		try {
			
			InputStream buffer = new ByteArrayInputStream(Base64.decode(value));
			ObjectInput input = new ObjectInputStream ( buffer );
			List<Tuple> tuples = (List<Tuple>)input.readObject();
			buffer.close();
			input.close();
			return tuples;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Base64DecoderException e) {
			e.printStackTrace();
		}

		return null;
		
	}

}
