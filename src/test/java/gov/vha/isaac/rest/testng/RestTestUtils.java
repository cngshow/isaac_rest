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
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;

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
//			for (int i = 0; i < nodeList.getLength(); ++i) {
//				System.out.println("Node #" + i + ": " + toString(nodeList.item(i)));
//			}
			
			return nodeList;
		} catch (XPathExpressionException | ParserConfigurationException | SAXException | IOException e) {
			System.err.println("Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
			e.printStackTrace();
		}
		
		return null;
	}

	public static String toString(TaxonomyCoordinate coordinate) {
		return "";
	}
	public static String toString(StampCoordinate coordinate) {
		return "";
	}
	public static String toString(LanguageCoordinate coordinate) {
		return "";
	}
	public static String toString(LogicCoordinate coordinate) {
		return "";
	}
}
