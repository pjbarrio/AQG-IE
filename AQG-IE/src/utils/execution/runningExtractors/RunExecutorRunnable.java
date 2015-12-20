package utils.execution.runningExtractors;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import org.apache.commons.io.FileUtils;

public class RunExecutorRunnable implements Runnable {

	private URL website;
	private String html;
	private File toSave;
	private File toSaveExtraction;
	private Set<URL> processed;

	public RunExecutorRunnable(Set<URL> processed, URL website, String html, File toSave,
			File toSaveExtraction) {
		this.processed = processed;
		this.website = website;
		this.html = html;
		this.toSave = toSave;
		this.toSaveExtraction = toSaveExtraction;
	}

	@Override
	public void run() {

		try {

			URL toProcess = new URL(website,html);
			
			synchronized (processed) {
				if (processed.contains(toProcess))
					return;
				processed.add(toProcess);
			}
			
			if (!toSave.exists())
				FileUtils.copyURLToFile(toProcess, toSave, 10000, 10000);
			
			System.out.println("URL: " + html);
			
			RunExtractor.extract(toSave,toSaveExtraction);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
