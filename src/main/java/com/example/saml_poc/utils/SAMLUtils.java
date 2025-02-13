package com.example.saml_poc.utils;

import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.w3c.dom.Element;

import java.io.StringWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class SAMLUtils {

    public static String marshallSAMLObject(LogoutRequest logoutRequest) throws Exception {
        try {
            // Get marshaller
            MarshallerFactory marshallerFactory = XMLObjectProviderRegistrySupport.getMarshallerFactory();
            Marshaller marshaller = marshallerFactory.getMarshaller(logoutRequest);
            if (marshaller == null) {
                throw new RuntimeException("No marshaller found for LogoutRequest.");
            }

            // Convert to XML DOM Element
            Element element = marshaller.marshall(logoutRequest);

            // Convert to String
            return elementToString(element);
        } catch (MarshallingException e) {
            throw new RuntimeException("Error marshalling SAML object: " + e.getMessage(), e);
        }
    }

    private static String elementToString(Element element) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(element), new StreamResult(writer));
        return writer.toString();
    }
}
