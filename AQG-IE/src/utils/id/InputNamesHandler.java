package utils.id;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InputNamesHandler {

	public static List<String> loadInputNames(String inputNamesFile) {
		
		try {
		
			List<String> inputNames = new ArrayList<String>();

			BufferedReader input = new BufferedReader(new FileReader(new File(inputNamesFile)));
			
			String line;
			
			while ((line=input.readLine())!=null){
				inputNames.add(line);
			}
			
			input.close();
		
			return inputNames;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}
	
}
