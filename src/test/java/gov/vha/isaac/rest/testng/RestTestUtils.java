/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gov.vha.isaac.rest.testng;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import gov.vha.isaac.rest.api1.data.systeminfo.RestDependencyInfo;

/**
 * 
 * {@link RestTestUtils}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class RestTestUtils {
	private RestTestUtils() {}
	
	public static String toString(Node node) {
		return "Node {name=" + node.getNodeName() + ", value=" + node.getNodeValue() + ", type=" + node.getNodeType() + ", text=" + node.getTextContent() + "}";
	}

	@SuppressWarnings("unchecked")
	public static <T> T unmarshalObject(Class<T> classType, String xmlString)
	{
		try
		{
			T object = null;
			StringReader stringReader = new StringReader(xmlString);
			StreamSource streamSource = new StreamSource(stringReader);

			JAXBContext jaxbContext = JAXBContext.newInstance(classType);

			Unmarshaller unMarshaller = jaxbContext.createUnmarshaller();
			object = (T)unMarshaller.unmarshal(streamSource);
			return (T)object;
		}
		catch (JAXBException e)
		{
			e.printStackTrace();
			throw new RuntimeException("Error unmarshalling class : " + classType.getName() + ". " + "Caught " + e.getClass().getName() + " " + e.getMessage());
		}
	} 

	public static Double getNumberFromXml(String xmlStr, String xPathStr) {
		return (Double)getFromXml(xmlStr, xPathStr, XPathConstants.NUMBER);
	}
	public static Boolean getBooleanFromXml(String xmlStr, String xPathStr) {
		return (Boolean)getFromXml(xmlStr, xPathStr, XPathConstants.BOOLEAN);
	}
	public static String getStringFromXml(String xmlStr, String xPathStr) {
		return (String)getFromXml(xmlStr, xPathStr, XPathConstants.STRING);
	}
	public static Node getNodeFromXml(String xmlStr, String xPathStr) {
		return (Node)getFromXml(xmlStr, xPathStr, XPathConstants.NODE);
	}
	public static NodeList getNodeSetFromXml(String xmlStr, String xPathStr) {
		return (NodeList)getFromXml(xmlStr, xPathStr, XPathConstants.NODESET);
	}
	
	private static Object getFromXml(String xmlStr, String xPathStr, QName type) {
		//System.out.println(xmlStr);
		
		InputStream responseXmlStream = new ByteArrayInputStream(xmlStr.getBytes(StandardCharsets.UTF_8));

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document xmlDocument = db.parse(responseXmlStream);
			
			XPath xPath =  XPathFactory.newInstance().newXPath();
			
			//String xPathStr = "/Employees/Employee[@emplid='3333']/email"
			//String xPathStr = "/restSememeVersions/results/sememeChronology/identifiers/uuids"
			
			
			//read a nodelist using xpath
			Object object = xPath.compile(xPathStr).evaluate(xmlDocument, type);
			
			//	System.out.println("Node: " + toString(node));
			//if (object instanceof NodeList) {
			//		System.out.println("FOUND " + nodeList.getLength() + " NODES IN LIST:");
			//		for (int i = 0; i < nodeList.getLength(); ++i) {
			//			System.out.println("Node #" + i + ": " + toString(nodeList.item(i)));
			//		}
			//} else if (object instanceof Node) {
			//		System.out.println("Node: " + toString(object));
			//}
			
			return object;
		} catch (XPathExpressionException | ParserConfigurationException | SAXException | IOException e) {
			System.err.println("Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
			e.printStackTrace();
		}
		
		return null;
	}

	public static NodeList getNodeList(String xmlStr, String xPathStr) {
		//System.out.println(xmlStr);
		
		InputStream responseXmlStream = new ByteArrayInputStream(xmlStr.getBytes(StandardCharsets.UTF_8));

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document xmlDocument = db.parse(responseXmlStream);
			
			XPath xPath =  XPathFactory.newInstance().newXPath();
			
			//String expression = "/Employees/Employee[@emplid='3333']/email"

			//String xPathStr = "/restSememeVersions/results/sememeChronology/identifiers/uuids";
			//expression = "/restSememeVersions/results";
			
			//read a single xml node using xpath
			//Node node = (Node) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODE);
			 
			//read a nodelist using xpath
			NodeList nodeList = (NodeList) xPath.compile(xPathStr).evaluate(xmlDocument, XPathConstants.NODESET);
			
			//System.out.println("FOUND " + nodeList.getLength() + " NODES IN LIST:");
			//	for (int i = 0; i < nodeList.getLength(); ++i) {
			//		System.out.println("Node #" + i + ": " + toString(nodeList.item(i)));
			//	}
			
			return nodeList;
		} catch (XPathExpressionException | ParserConfigurationException | SAXException | IOException e) {
			System.err.println("Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void main(String...argv) {
		// unmarshalObject example
		String xml ="<restDependencyInfo> <groupId>gov.vha.isaac.db</groupId> <artifactId>solor</artifactId> <version>1.1-SNAPSHOT</version> <classifier>all</classifier> <type>cradle.zip</type> </restDependencyInfo>";
		RestDependencyInfo object = unmarshalObject(RestDependencyInfo.class, xml);

		System.out.println(object);
	}
}
