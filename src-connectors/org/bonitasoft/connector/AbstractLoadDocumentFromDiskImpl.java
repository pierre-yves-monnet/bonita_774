package org.bonitasoft.connector;

import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorValidationException;

public abstract class AbstractLoadDocumentFromDiskImpl extends
		AbstractConnector {

	protected final static String PATHFILE_INPUT_PARAMETER = "pathFile";
	protected final static String DOCUMENTNAME_INPUT_PARAMETER = "documentName";
	protected final static String CONTENTTYPE_INPUT_PARAMETER = "contentType";
	protected final String DOCUMENT_OUTPUT_PARAMETER = "document";

	protected final java.lang.String getPathFile() {
		return (java.lang.String) getInputParameter(PATHFILE_INPUT_PARAMETER);
	}

	protected final java.lang.String getDocumentName() {
		return (java.lang.String) getInputParameter(DOCUMENTNAME_INPUT_PARAMETER);
	}

	protected final java.lang.String getContentType() {
		return (java.lang.String) getInputParameter(CONTENTTYPE_INPUT_PARAMETER);
	}

	protected final void setDocument(
			org.bonitasoft.engine.bpm.document.DocumentValue document) {
		setOutputParameter(DOCUMENT_OUTPUT_PARAMETER, document);
	}

	@Override
	public void validateInputParameters() throws ConnectorValidationException {
		try {
			getPathFile();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException("pathFile type is invalid");
		}
		try {
			getDocumentName();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException(
					"documentName type is invalid");
		}
		try {
			getContentType();
		} catch (ClassCastException cce) {
			throw new ConnectorValidationException(
					"contentType type is invalid");
		}

	}

}
