package init.initialization;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import searcher.interaction.Interaction;
import searcher.interaction.factory.InteractionFactory;
import utils.id.DatabaseIndexHandler;
import utils.persistence.databaseWriter;

public class WebsiteInteraction {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		databaseWriter dW = new databaseWriter("");
		
		String inputNamePrefix = "/local/pjbarrio/Files/Projects/AutomaticQueryGeneration/Forms/FinalSelection/FinalInputNames/";

		String formPrefix = "/local/pjbarrio/Files/Projects/AutomaticQueryGeneration/Forms/FinalSelection/FinalForms/";

		String databaseIndex = "/local/pjbarrio/Files/Projects/AutomaticQueryGeneration/Forms/FinalSelection/FinalDataIndex.txt";

		Map<Integer, String> indexTable = DatabaseIndexHandler.loadInvertedDatabaseIndex(new File(databaseIndex));

		for (Entry<Integer,String> entry : indexTable.entrySet()) {
			
			String formFile = formPrefix + entry.getKey() + ".html";

			if (!(new File(formFile).exists()))
				continue;

//			System.out.println("Processing..." + entry.getKey());

			String inputNameFiles = inputNamePrefix + entry.getKey() + ".txt";

			String encoding = "UTF-8";
			
			Interaction interaction = InteractionFactory.generateInstance(dW.getDatabaseByName(entry.getValue()), formFile, encoding, FileUtils.readLines(new File(inputNameFiles)));
			
			String inter = interaction.getPrefixWebsite();
			
			String toPrint = inter.substring(0,inter.lastIndexOf('/')+1);
			
			if (toPrint.equals("http://")){
				toPrint = indexTable.get(entry.getKey());
			}
			
			System.out.println(entry.getKey()+","+toPrint);
			
//			if (entry.getKey() > 6)
//				dW.updateIndexOfWebsite(entry.getKey(),inter.substring(0,inter.lastIndexOf('/')+1));
			
		}
		
		
		
	}

}
