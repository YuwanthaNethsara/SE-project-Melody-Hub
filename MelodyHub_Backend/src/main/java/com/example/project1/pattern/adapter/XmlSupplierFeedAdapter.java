package com.example.project1.pattern.adapter;

import com.example.project1.dto.SupplierRequest;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for a supplier's partner-system XML:
 * <pre>
 * &lt;suppliers&gt;
 *   &lt;supplier&gt;&lt;company_name/&gt;&lt;contact_name/&gt;&lt;mail/&gt;&lt;tel/&gt;&lt;location/&gt;&lt;active&gt;true&lt;/active&gt;&lt;/supplier&gt;
 * &lt;/suppliers&gt;
 * </pre>
 * The partner's field names and the true/false "active" flag are translated to MelodyHub's own
 * supplierName / contactPerson / email / phone / address / status (ACTIVE or INACTIVE).
 */
@Component
public class XmlSupplierFeedAdapter implements SupplierFeedAdapter {

    @Override
    public String getFormat() {
        return "XML";
    }

    @Override
    public String describe() {
        return "<suppliers><supplier><company_name/><contact_name/><mail/><tel/><location/><active>true|false</active></supplier>...</suppliers>";
    }

    @Override
    public List<SupplierRequest> toSupplierRequests(String rawData) {
        Document document = parse(rawData);
        NodeList nodes = document.getElementsByTagName("supplier");
        if (nodes.getLength() == 0) {
            throw new IllegalArgumentException("XML must contain at least one <supplier> element");
        }

        List<SupplierRequest> result = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            SupplierRequest request = new SupplierRequest();
            request.setSupplierName(text(element, "company_name"));
            request.setContactPerson(text(element, "contact_name"));
            request.setEmail(text(element, "mail"));
            request.setPhone(text(element, "tel"));
            request.setAddress(text(element, "location"));
            String active = text(element, "active");
            request.setStatus(active == null ? null
                    : ("true".equalsIgnoreCase(active) || "yes".equalsIgnoreCase(active) || "1".equals(active)
                    ? "ACTIVE" : "INACTIVE"));
            result.add(request);
        }
        return result;
    }

    private static String text(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        if (list.getLength() == 0) {
            return null;
        }
        String value = list.item(0).getTextContent();
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static Document parse(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // uploaded data is untrusted: no DTDs / external entities (XXE)
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            throw new IllegalArgumentException("The XML could not be read. Please check its format.");
        }
    }
}
