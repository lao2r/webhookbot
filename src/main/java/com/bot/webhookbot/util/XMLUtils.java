package com.bot.webhookbot.util;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class XMLUtils {

    public String processXml(String base64XmlString) {
        byte[] xmlData = Base64.getDecoder().decode(base64XmlString);
        SAXBuilder saxBuilder = new SAXBuilder();
        String xmlString = new String(xmlData, StandardCharsets.UTF_8);

        try {
            Document document = saxBuilder.build(new StringReader(xmlString));
            Element root = document.getRootElement();
            String version = root.getChildText("version", root.getNamespace());
            if (version != null) {
                return version;
            } else {
                Element parent = document.getRootElement().getChild("parent", root.getNamespace());
                return parent.getChildText("version", parent.getNamespace());
            }

        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
