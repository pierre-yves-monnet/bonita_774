/**
 * 
 */
package org.bonitasoft.connector;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Logger;

import javax.activation.MimetypesFileTypeMap;

import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.connector.ConnectorException;

/**
 *The connector execution will follow the steps
 * 1 - setInputParameters() --> the connector receives input parameters values
 * 2 - validateInputParameters() --> the connector can validate input parameters values
 * 3 - connect() --> the connector can establish a connection to a remote server (if necessary)
 * 4 - executeBusinessLogic() --> execute the connector
 * 5 - getOutputParameters() --> output are retrieved from connector
 * 6 - disconnect() --> the connector can close connection to remote server (if any)
 */
public class LoadDocumentFromDiskImpl extends AbstractLoadDocumentFromDiskImpl {

	private Logger logger = Logger.getLogger("org.bonitasoft.connector.LoadDocumentFromDisk");
	
	@Override
	protected void executeBusinessLogic() throws ConnectorException{
		//Get access to the connector input parameters
		//getPathFile();
		//getDocumentName();
		//getContentType();
	
		try
		{
			File file = new File( getPathFile() );
			//init array with file length
			byte[] bytesArray = new byte[(int) file.length()];
	
			FileInputStream fis = new FileInputStream(file);
			fis.read(bytesArray); //read file into bytes[]
			fis.close();
					  
			String contentType=getContentType();
			if (contentType==null || contentType.trim().length()==0)
			{
				String fileName = getPathFile();
				contentType = MimetypesFileTypeMap
					    .getDefaultFileTypeMap()
					    .getContentType(fileName);
				// calculate one
				//if (fileName.endsWith(".doc") || fileName.endsWith(".docx"))
				//	contentType="application/msword";
			}
			
			String documentName = getDocumentName();
			if (documentName==null || documentName.trim().length()==0)
				documentName = file.getName();
			
			logger.info("load file ["+getPathFile()+"] contentType["+contentType+"] documentName["+documentName+"]");			
			DocumentValue docValue = new DocumentValue( bytesArray, contentType, documentName);
			 
			setDocument(docValue);
		}
		catch(Exception e)
		{
			logger.severe("Error load a file ["+getPathFile()+"] :"+e.getMessage());
			throw new ConnectorException("Error load a file ["+getPathFile()+"] :"+e.getMessage());
		}
	 }

	@Override
	public void connect() throws ConnectorException{
		//[Optional] Open a connection to remote server
	
	}

	@Override
	public void disconnect() throws ConnectorException{
		//[Optional] Close connection to remote server
	
	}

}
