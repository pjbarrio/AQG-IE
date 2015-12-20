package execution.trunk.test;

import java.io.File;
import java.io.IOException;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.GoogleContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;

import org.apache.commons.io.FileUtils;

public class EntityTester {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String f = FileUtils.readFileToString(new File("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/1/777/RESULTS/CHNH/TEDW/ALLLINKS/damage/0/0.html"));
		
		ContentExtractor ce = new TikaContentExtractor();
		
		String content = ce.extractContent(f);
		
		System.out.println(content);
		
		System.out.println(content.substring(7508,7517));
		System.out.println(content.substring(7597,7606));
		System.out.println(content.substring(652,656));
		System.out.println(content.substring(898,906));
		System.out.println(content.substring(4229,4242));
		System.out.println(content.substring(7500,7506));
		System.out.println(content.substring(7544,7548));
		
	}

}
