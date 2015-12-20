package dataset.generation;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.List;

import com.google.gdata.util.common.base.Pair;

import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import exploration.model.Database;

public class StatisticsFromCollection {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		int group = Integer.valueOf(args[0]); //-10 has all the databases
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		List<Database> databases = pW.getSamplableDatabases(group);

		for (Database database : databases) {
			
			List<Pair<Integer, String>> list = (List<Pair<Integer,String>>)deserialize("cachingExperiments/" + database.getId() + ".ser");
			if (list!= null)
				System.out.println("Database: " + database.getId() + " -  Size: " + list.size());
			
		}
		
	}

	public static Object deserialize(String fileName){

		Object obj = null;

		try{
			//use buffering
			InputStream file = new FileInputStream( fileName );
			InputStream buffer = new BufferedInputStream( file );
			ObjectInput input = new ObjectInputStream ( buffer );

			try{

				obj = input.readObject();


			}
			finally{
				input.close();
			}


		}
		catch(ClassNotFoundException ex){
			ex.printStackTrace();
		}
		catch(IOException ex){
//			ex.printStackTrace();
		}		

		return obj;

	}
	
}
