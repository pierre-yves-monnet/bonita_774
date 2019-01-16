/**
 * 
 */
package org.bonitasoft.connector;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FilenameUtils;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.connector.ConnectorException;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

import com.google.gson.Gson;

/**
 *The connector execution will follow the steps
 * 1 - setInputParameters() --> the connector receives input parameters values
 * 2 - validateInputParameters() --> the connector can validate input parameters values
 * 3 - connect() --> the connector can establish a connection to a remote server (if necessary)
 * 4 - executeBusinessLogic() --> execute the connector
 * 5 - getOutputParameters() --> output are retrieved from connector
 * 6 - disconnect() --> the connector can close connection to remote server (if any)
 */
public class LoadFileConnector extends
		AbstractLoadFileConnector {
	
	private Logger logger = Logger.getLogger("org.bonitasoft");
	
	private String token;

	@Override
	protected void executeBusinessLogic() throws ConnectorException{
		try {
			
            List<Document> documentList=(List<Document>) getDocumentList();
            //Customer customer=(Customer) getCustomer();
            Map<String,Object> detailsVirtualFolder=createCustomerVirtualFolder(getExecutionContext().getProcessInstanceId());
            List<Map<String,Object>> results=new ArrayList<Map<String,Object>>();            
            for (Document document:documentList){
                Map<String,Object> resultUploadDoc=uploadDocument(getAPIAccessor().getProcessAPI().getDocumentContent(document.getContentStorageId()), document.getContentMimeType(), document.getContentFileName(), new Integer(detailsVirtualFolder.get("DisplayID").toString()), document.getName());
                Map<String,Object> resultItem=new HashMap<String,Object>();
                resultItem.put("bonitaDocId", document.getId());                
                resultItem.put("mimeType", document.getContentMimeType());                
                resultItem.put("mFileDocData", resultUploadDoc);
                results.add(resultItem);
            }
            setDocIdsList(results);
        } catch (Exception ex) {            
            throw new ConnectorException(ex.toString(), ex);
        }
	
	 }

	@Override
	public void connect() throws ConnectorException{
		try {        
            token=clientAuthentication();
        } catch (Exception ex) {            
            throw new ConnectorException(ex.toString(), ex);
        }
	
	}

	@Override
	public void disconnect() throws ConnectorException{
		//[Optional] Close connection to remote server
	
	}
	
	private String clientAuthentication() throws Exception{
        ClientRequest clientRequest = new ClientRequest(getHost() + "/server/authenticationtokens");
        clientRequest.setHttpMethod(javax.ws.rs.HttpMethod.POST);
        String input = "{'Username':'" + getUsername() +"','Password':'" + getPassword() + "','VaultGuid':'" +getVaultId() +"'}";
        clientRequest.body("application/json", input);        
        ClientResponse<String> response = clientRequest.post(String.class);
        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());
        }
         
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(response.getEntity().getBytes())));

        StringBuilder sb=new StringBuilder();
        String output;
        logger.severe("Output from Server .... \n");
        while ((output = br.readLine()) != null) {
            sb.append(output);
        }
        String res=sb.toString().substring(1,sb.toString().length()-1);
        logger.severe(res);
        String[] resString=res.split(":");        
        return resString[1].replace("\"", "");
    }

	
	private Map<String,Object> createCustomerVirtualFolder(long processInstanceId) throws Exception {
        Map<String,Object> results=null;
        Gson gson=new Gson();        
        ObjectCreationInfo objectCreationInfo=new ObjectCreationInfo();        
        
        objectCreationInfo.PropertyValues=new PropertyValue[7];
        PropertyValue documentName=new PropertyValue();
        documentName.PropertyDef=0;
        TypedValue typedValue1=new TypedValue();
        typedValue1.DataType=1;
        typedValue1.Value=processInstanceId;
        documentName.TypedValue=typedValue1;
        objectCreationInfo.PropertyValues[0]=documentName;

        PropertyValue singleFile=new PropertyValue();
        singleFile.PropertyDef=22;
        TypedValue typedValue2=new TypedValue();
        typedValue2.DataType=8;
        typedValue2.Value=false;
        singleFile.TypedValue=typedValue2;
        objectCreationInfo.PropertyValues[1]=singleFile;
        
        PropertyValue classFile=new PropertyValue();
        classFile.PropertyDef=100;
        TypedValue typedValue3=new TypedValue();
        typedValue3.DataType=9;
        Lookup lookup=new Lookup();
        lookup.Item=getVirtualFolderClassId();
        typedValue3.Lookup=lookup;
        classFile.TypedValue=typedValue3;
        objectCreationInfo.PropertyValues[2]=classFile;
        
        PropertyValue customerNumber=new PropertyValue();
        customerNumber.PropertyDef=getCustomerNumberPropertyId();
        TypedValue typedValueCustomerNumber=new TypedValue();
        typedValueCustomerNumber.DataType=1;
        typedValueCustomerNumber.Value=getCustomerNumber();
        customerNumber.TypedValue=typedValueCustomerNumber;
        objectCreationInfo.PropertyValues[3]=customerNumber;
        
        PropertyValue procInstId=new PropertyValue();
        procInstId.PropertyDef=getProcessInstanceIdPropertyId();
        TypedValue typedValueProcInstId=new TypedValue();
        typedValueProcInstId.DataType=1;
        typedValueProcInstId.Value=processInstanceId;
        procInstId.TypedValue=typedValueProcInstId;
        objectCreationInfo.PropertyValues[4]=procInstId;
        
        PropertyValue companyName=new PropertyValue();
        companyName.PropertyDef=getCompanyNamePropertyId();
        TypedValue typedValueAddress=new TypedValue();
        typedValueAddress.DataType=1;
        typedValueAddress.Value=getCompanyName();
        companyName.TypedValue=typedValueAddress;
        objectCreationInfo.PropertyValues[5]=companyName;
        
        PropertyValue applicationRefNumber=new PropertyValue();
        applicationRefNumber.PropertyDef=getApplicationReferenceNumberPropertyId();
        TypedValue typedValueEmail=new TypedValue();
        typedValueEmail.DataType=1;
        typedValueEmail.Value=getApplicationReferenceNumber();
        applicationRefNumber.TypedValue=typedValueEmail;
        objectCreationInfo.PropertyValues[6]=applicationRefNumber;
        
        ClientRequest clientRequest = new ClientRequest(getHost() + "/objects/" + getCustomerObjectID().toString());                
        clientRequest.header("X-Authentication", token);
        logger.severe(gson.toJson(objectCreationInfo));
        clientRequest.body(MediaType.APPLICATION_JSON, gson.toJson(objectCreationInfo));
        
        ClientResponse<String> response = clientRequest.post(String.class);
         
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(response.getEntity().getBytes())));

        StringBuilder sb=new StringBuilder();
        String output;
        logger.severe("Output from Server .... \n");
        while ((output = br.readLine()) != null) {
            sb.append(output);
        }        
        logger.severe(sb.toString());
        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());
        }
        results=gson.fromJson(sb.toString(), Map.class);        
        return results;
    }
    
    private Map<String,Object> uploadDocument(byte[] content, String mimeType, String fileName, Integer folderId, String bonitaDocumentName) throws Exception {
        Map<String,Object> results=null;
        Gson gson=new Gson();         
        ClientRequest clientRequest = new ClientRequest(getHost() + "/files");
        clientRequest.setHttpMethod(javax.ws.rs.HttpMethod.POST);        
        clientRequest.header("X-Authentication", token);        
        clientRequest.body(MediaType.APPLICATION_OCTET_STREAM, content);
        ClientResponse<String> response = clientRequest.post(String.class);
        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());
        }
         
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(response.getEntity().getBytes())));

        StringBuilder sb=new StringBuilder();
        String output;
        logger.severe("Output from Server .... \n");
        while ((output = br.readLine()) != null) {
            sb.append(output);
        }        
        logger.severe(sb.toString());
                
        UploadInfo uploadInfo=gson.fromJson(sb.toString(), UploadInfo.class);
        uploadInfo.Extension=getFileExtension(fileName);
        ObjectCreationInfo objectCreationInfo=new ObjectCreationInfo();
        
        objectCreationInfo.Files=new UploadInfo[1];
        objectCreationInfo.Files[0]=uploadInfo;
        
        objectCreationInfo.PropertyValues=new PropertyValue[5];
        PropertyValue documentName=new PropertyValue();
        documentName.PropertyDef=0;
        TypedValue typedValue1=new TypedValue();
        typedValue1.DataType=1;
        typedValue1.Value=FilenameUtils.getBaseName(fileName);
        documentName.TypedValue=typedValue1;
        objectCreationInfo.PropertyValues[0]=documentName;

        PropertyValue singleFile=new PropertyValue();
        singleFile.PropertyDef=22;
        TypedValue typedValue2=new TypedValue();
        typedValue2.DataType=8;
        typedValue2.Value=true;
        singleFile.TypedValue=typedValue2;
        objectCreationInfo.PropertyValues[1]=singleFile;
        
        PropertyValue classFile=new PropertyValue();
        classFile.PropertyDef=100;
        TypedValue typedValue3=new TypedValue();
        typedValue3.DataType=9;
        Lookup lookup=new Lookup();
        lookup.Item=0;
        typedValue3.Lookup=lookup;
        classFile.TypedValue=typedValue3;
        objectCreationInfo.PropertyValues[2]=classFile;
        
        PropertyValue docName=new PropertyValue();
        docName.PropertyDef=getBonitaDocumentNamePropertyId();
        TypedValue typedValueDocname=new TypedValue();
        typedValueDocname.DataType=1;
        typedValueDocname.Value=bonitaDocumentName;
        docName.TypedValue=typedValueDocname;
        objectCreationInfo.PropertyValues[3]=docName;
        
        PropertyValue parent=new PropertyValue();
        parent.PropertyDef=getCustomerPropertyID().intValue();
        TypedValue typedValue4=new TypedValue();
        typedValue4.Lookups=new Lookup[1];
        typedValue4.DataType=10;        
        Lookup lookup2=new Lookup();
        lookup2.Item=folderId;
        typedValue4.Lookups[0]=lookup2;
        parent.TypedValue=typedValue4;
        objectCreationInfo.PropertyValues[4]=parent;
        
        clientRequest = new ClientRequest(getHost() + "/objects/0");
        clientRequest.setHttpMethod(javax.ws.rs.HttpMethod.POST);
        clientRequest.accept(MediaType.APPLICATION_JSON);
        clientRequest.header("X-Authentication", token);
        clientRequest.body(MediaType.APPLICATION_JSON, gson.toJson(objectCreationInfo));
        
        response = clientRequest.post(String.class);
        br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(response.getEntity().getBytes())));

        sb=new StringBuilder();
        
        logger.severe("Output from Server .... \n");
        while ((output = br.readLine()) != null) {
            sb.append(output);
        }        
        logger.severe(sb.toString());
        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());
        }
        results=gson.fromJson(sb.toString(), Map.class); 
        return results;
    }
    
    private String getFileExtension(String fileName){
    	return FilenameUtils.getExtension(fileName);
    }
	
	public class Authentication {
	    //! 
	    public String Username;
	    
	    //! 
	    public String Password;
	    
	    //! 
	    public String Domain;
	    
	    //! 
	    public boolean WindowsUser;
	    
	    //! 
	    public String ComputerName;
	    
	    //! 
	    public String VaultGuid;
	    
	    //! 
	    public String Expiration;
	    
	    //! 
	    public boolean ReadOnly;
	    
	    //! 
	    public String URL;
	    
	    //! 
	    public String Method;
	}
	
	public class Lookup {
	    //! Based on M-Files API.
	    public boolean Deleted;
	    
	    //! Based on M-Files API.
	    public String DisplayValue;
	    
	    //! Based on M-Files API.
	    public boolean Hidden;
	    
	    //! Based on M-Files API.
	    public int Item;
	    
	    //! Based on M-Files API.
	    public int Version;
	}
	
	public class ObjectCreationInfo {
	    public PropertyValue[] PropertyValues;
	    
	    //! References previously uploaded files.
	    public UploadInfo[] Files;
	}
	
	public class PropertyValue {
	    public int PropertyDef;
	    public TypedValue TypedValue;
	}
	
	public class TypedValue {
	    //! Specifies the type of the value.
	    public int DataType;
	    
	    //! Specifies whether the typed value contains a real value.
	    public boolean HasValue;
	    
	    //! Specifies the string, number or boolean value when the DataType is not a lookup type.
	    public Object Value;
	    
	    //! Specifies the lookup value when the DataType is Lookup.
	    public Lookup Lookup;
	    
	    //! Specifies the collection of \type{Lookup}s when the DataType is MultiSelectLookup.
	    public Lookup[] Lookups;
	    
	    //! Provides the value formatted for display.
	    public String DisplayValue;
	    
	    //! Provides a key that can be used to sort \type{TypedValue}s
	    public String SortingKey;
	    
	    //! Provides the typed value in a serialized format suitable to be used in URIs.
	    public String SerializedValue;
	}
	
	public class UploadInfo {
	    //! Temporary upload ID given by the server.
	    public int UploadID;
	    
	    //! File name without extension.
	    public String Title;
	    
	    //! File extension.
	    public String Extension;
	    
	    //! File size.
	    public long Size;
	    
	    public long FileInformationType;
	}

}


