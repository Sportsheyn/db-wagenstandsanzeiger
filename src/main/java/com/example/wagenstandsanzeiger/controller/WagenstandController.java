package com.example.wagenstandsanzeiger.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

@RestController
public class WagenstandController {


    @GetMapping("/station/{ril100}/train/{trainNumber}/waggon/{number}")
    public ResponseEntity<Map<String, Object>> getWagenstand(@PathVariable String ril100, @PathVariable int trainNumber, @PathVariable int number) {

        try {
            String fileName = readXmlFileByPrefix(ril100);

            File folder = new File("src/main/resources/xml/" + fileName);
            Node node = getNodeByXPath(folder, "./station/tracks/track/trains/train[trainNumbers/trainNumber = '" + trainNumber + "']");
            Node node2 = getNodeByXPath(node, "//train/waggons/waggon[position=" + number + "]");
            List<String> resultList = getXmlString(getXmlString(node2));
            Map<String, Object> response = new HashMap<>();
            response.put("sections", resultList);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch(Exception e) {
            return  new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

    }

    public static String readXmlFileByPrefix(String prefix) throws IOException {
        File folder = new File("src/main/resources/xml");
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.getName().matches(prefix + "_.*\\.xml")) {
                String fileContent = new String(Files.readAllBytes(file.toPath()));
                return file.getName();
            }
        }
        throw new FileNotFoundException("Keine Datei mit dem Präfix " + prefix + " gefunden.");
    }

    public static Node getNodeByXPath(Node node, String xpathExpression) throws Exception {
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile(xpathExpression);
        Node resultNode = (Node) expr.evaluate(node, XPathConstants.NODE);
        return resultNode;
    }

    public static Node getNodeByXPath(File xmlFile, String xpathExpression) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlFile);
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile(xpathExpression);
        Node resultNode = (Node) expr.evaluate(document, XPathConstants.NODE);
        return resultNode;
    }

    public static String getXmlString(Node node) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");

        DOMSource source = new DOMSource(node);
        StreamResult result = new StreamResult(new StringWriter());
        transformer.transform(source, result);
        String xmlString = result.getWriter().toString();

        return xmlString;
    }

    public static List<String> getXmlString(String xmlString ) throws TransformerException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(xmlString));
        Document document = builder.parse(inputSource);

        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile("//identifier");

        NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET); //

        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            arrayList.add(nodeList.item(i).getTextContent());
        }
        return arrayList;
    }

    public static void test(String ril100, int trainNumber, int number) {
        try {
            String fileName = readXmlFileByPrefix(ril100);
            File folder = new File("src/main/resources/xml/" + fileName);
            Node node = getNodeByXPath(folder, "./station/tracks/track/trains/train[trainNumbers/trainNumber = '" + trainNumber + "']");
            Node node2 = getNodeByXPath(node, "//train/waggons/waggon[position=" + number + "]");
            List<String> resultList = getXmlString(getXmlString(node2));

            for (int i = 0; i < resultList.size(); i++) {

            }
            System.out.println(resultList.get(0));

        } catch (Exception e) {
            System.out.println("Fehler");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        //File folder = new File("src/main/resources/xml/FF_2017-12-01_10-47-17.xml");
        //Node node = getNodeByXPath(folder, "./station/tracks/track/trains/train[1][trainNumbers/trainNumber = '2310']");
        //Node node2 = getNodeByXPath(node, "//train/waggons/waggon[position=11]");

        //Node node3 = getNodeByXPath(node2, "//waggon/sections/identifier");
        test("FF", 2310, 10);
    }

}
