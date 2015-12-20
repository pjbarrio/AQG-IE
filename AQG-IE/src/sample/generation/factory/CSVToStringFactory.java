package sample.generation.factory;

import java.util.ArrayList;
import java.util.List;

public class CSVToStringFactory {

	public static List<String> generateList(String string) {
		
		List<String> ret = new ArrayList<String>();
				
		String[] attributes = string.split(",");
		
		for (int i = 0; i < attributes.length; i++) {
			
			ret.add(attributes[i].toLowerCase());
			
		}
		
		return ret;
	
	}

}
