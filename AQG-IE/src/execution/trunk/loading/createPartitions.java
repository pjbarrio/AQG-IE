package execution.trunk.loading;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class createPartitions {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String f = "/home/pjbarrio/Desktop/workingdbs.csv";

		List<String> list = FileUtils.readLines(new File(f));
		
		int size = 5;
		
		int numPart = 0;
		
		System.out.print("PARTITION part" + numPart + " VALUES IN (");
		
		for (int i = 0; i < list.size(); i++) {
			size--;
			if (size > 0){
				System.out.print(list.get(i) + ",");
			} else {
				size = 5;
				System.out.println(list.get(i) + "),");
				numPart++;
				System.out.print("PARTITION part" + numPart + " VALUES IN (");
			}
		}
		
	}

}
