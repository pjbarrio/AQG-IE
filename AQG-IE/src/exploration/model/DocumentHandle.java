package exploration.model;

public class DocumentHandle {
  public final static int FILE_TYPE = 0;
  private String docHandle;
  
  public DocumentHandle(String handle) {
    setDocHandle(handle);
  }
  
  public String getDocHandle() {
    return docHandle;
  }
  
  public void setDocHandle(String handle) {
    if ((handle.toLowerCase().indexOf("file://")) >= 0) {
      docHandle = handle.replaceAll("file://", "");
    } else {
      docHandle = handle;
    }
  }
  
  public boolean equals(Object docHandle){
	  
	  DocumentHandle dc = (DocumentHandle)docHandle;
	  
	  return this.docHandle.equals(dc.getDocHandle());
	  
  }
  
  public String toString(){
	  return docHandle;
  }

}
