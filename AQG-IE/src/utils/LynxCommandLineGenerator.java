package utils;

import java.io.File;

public class LynxCommandLineGenerator {
	public String getLynxCommandLine(File inputFile,String newName){
		new File(newName).deleteOnExit();
		return new String("lynx -dump -nolist -hiddenlinks=ignore -force_html -verbose " + FileHandlerUtils.format(inputFile,newName));
	}

}
