package extraction.com.clearforest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.apache.commons.io.FileUtils;
import org.mitre.jawb.io.SgmlDocument;

import extraction.collectionProcessor.local.NYTCorpusDocument;
import extraction.collectionProcessor.local.NYTCorpusDocumentParser;

import utils.CommandLineExecutor;
import utils.FileHandlerUtils;
import utils.LynxCommandLineGenerator;

public class ExtractorRunnable implements Runnable {

	private static final int LIMIT_SIZE = 100000;
	private File file;
	private String outputFile;
	private String licenseID;
	private String paramsXML;
	
	private static NYTCorpusDocumentParser nyp = new NYTCorpusDocumentParser();

	public ExtractorRunnable(File file, String outputFile, String licenseID, String paramsXML){
		
		this.file = file;
		this.outputFile = outputFile;
	
		this.licenseID = licenseID;
		this.paramsXML = paramsXML;
	}
	
	@Override
	public void run() {
		
		System.out.println("Extracting: " + file);
		
		try {
			
			//XXX this is not always the case!
//			String content = new SgmlDocument(new FileReader(file)).getSignalText();

			NYTCorpusDocument doc = null;
			
			synchronized (nyp) {
				doc = nyp.parseNYTCorpusDocumentFromFile(file, false);
			}

			StringBuilder sb = new StringBuilder();
			
			List<String> titles = doc.getTitles();
			
			if (!titles.isEmpty()){
				sb.append(titles.get(0));
				for (int i = 1; i < titles.size(); i++) {
					sb.append("\n" + titles.get(i));
				}
				
				sb.append("\n" + doc.getBody());
				
			}else{
				sb.append(doc.getBody());
			}
			
			Thread.sleep(250);
				
			String content = sb.toString();
			
			if (content.length() < LIMIT_SIZE){
				
				String result = new CalaisLocator().getcalaisSoap().enlighten(licenseID, content, paramsXML);
					
				FileUtils.writeStringToFile(new File(outputFile), result);
				
			} else {
				
				int lastIndex = 0;
				int split = 0;
				
				int endIndex = content.substring(lastIndex, Math.min(lastIndex + LIMIT_SIZE, content.length())).lastIndexOf('\n');
				
				String chunk = content.substring(lastIndex, endIndex);
				
				while (!chunk.isEmpty()){
					
					String result = new CalaisLocator().getcalaisSoap().enlighten(licenseID, chunk, paramsXML);
					
					FileUtils.writeStringToFile(new File(outputFile + "." + split), result);
					
					lastIndex = endIndex;
					
					endIndex = lastIndex + content.substring(lastIndex, Math.min(lastIndex + LIMIT_SIZE, content.length())).lastIndexOf('\n');
					
					chunk = content.substring(lastIndex, endIndex);
					
					split++;
					
				}
				
			}
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		
	}

}
