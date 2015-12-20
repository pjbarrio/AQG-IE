package online.documentHandler.contentLoader.impl;

import java.io.File;

import exploration.model.Document;
import exploration.model.enumerations.ContentLoaderEnum;

import online.documentHandler.contentLoader.ContentLoader;
import utils.CommandLineExecutor;
import utils.LynxCommandLineGenerator;
import utils.persistence.persistentWriter;

public class LynxContentLoader extends ContentLoader {

	private CommandLineExecutor cle;
	private LynxCommandLineGenerator lclg;
	
	public LynxContentLoader(){
		
		cle = new CommandLineExecutor();
		
		lclg = new LynxCommandLineGenerator();
		
	}
	
	@Override
	public String loadContent(Document document,persistentWriter pW) {
		return cle.getOutput(lclg.getLynxCommandLine(document.getFilePath(pW),"" /*XXX watch out*/));
	}

	@Override
	public ContentLoaderEnum getLoaderEnum() {
		return ContentLoaderEnum.LYNX;
	}
	
}
