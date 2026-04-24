package com.fd.vplus.dmc.customCode.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.fd.vplus.dmc.userexit.common.DMUserExitInterface;

public class DMCustomCodeAtToEndpointExit implements DMUserExitInterface {

	@Override
	public byte[] execute(byte[] incomingBytes, String encoding) {

		final Logger logger = LogManager.getLogger(DMCustomCodeAtToEndpointExit.class);

		if (logger.isInfoEnabled()) {
			logger.info("DMCustomCodeAtToEndpointExit - inside the custom-code at to-endpoint exit");
		}

		if (incomingBytes == null || incomingBytes.length == 0) {
			if (logger.isInfoEnabled()) {
				logger.info("DMCustomCodeAtToEndpointExit - No incoming bytes received");
			}
		} else {

			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//				dbf.setNamespaceAware(true); // Crucial step
				XPath xPath = XPathFactory.newInstance().newXPath();

				String eJCICCertPath = System.getProperty("EJCIC_CERT_PATH");
				String dmcToJCICXSLTFilePath = System.getProperty("DMC_TO_JCIC_XSLT_FILE_PATH");

				if (eJCICCertPath == null || eJCICCertPath.length() <= 0) {
					logger.error("DMCustomCodeAtToEndpointExit - Ejcic certificate file Path is invalid");
					return null;
				}

				if (dmcToJCICXSLTFilePath == null || dmcToJCICXSLTFilePath.length() <= 0) {
					logger.error(
							"DMCustomCodeAtToEndpointExit - DMC to JCIC transformation stylesheet file path is invalid");
					return null;
				}

				// Access the XSLT file and build a StreamSource to use in XML transformation
				// later
				File stylesheet = new File(dmcToJCICXSLTFilePath);
				if (!stylesheet.exists()) {
					logger.error(
							"DMCustomCodeAtToEndpointExit - DMC to JCIC transformation stylesheet file does not exist");
					return null;
				}
//				StreamSource stylesource = new StreamSource(stylesheet);
				Source  stylesource = new StreamSource(stylesheet);

				// Create encrypter to encypt SystemName, timeStamp and macValue fields using
				// the public Key of EJCIC
				Encrypter encrypter = new Encrypter(eJCICCertPath);

				if (logger.isInfoEnabled()) {
					logger.info("DMCustomCodeAtToEndpointExit - stylesheet and encrypter are ready");
				}

				// Create a Document object from the incoming bytes[] to easily parse and
				// encrypt required fields
				String incomingMsg = new String(incomingBytes, encoding);

				logger.info("DMCustomCodeAtToEndpointExit - Incoming Request Raw Message >>> " + incomingMsg);

				// BufferedWriter outRawMsg = new BufferedWriter(new
				// FileWriter("C:/data/Projects/DMC_user_exit_for_jcic/JCIC_outgoing_message_Raw.xml"));
				// BufferedWriter outRawMsg = new BufferedWriter(new
				// FileWriter("/opt/visionplus/EJCICStandaloneClient/JCIC_outgoing_message_Raw.xml"));
				// outRawMsg.write(incomingMsg);
				// outRawMsg.close();

				if (logger.isTraceEnabled()) {
					logger.trace("DMCustomCodeAtToEndpointExit - message data recieved >>> " + incomingMsg);
				}
//				dbf.setNamespaceAware(false);
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document document = db.parse(new ByteArrayInputStream(incomingMsg.getBytes()));

				// Find System Name node and encrypt the value (assuming the node for System
				// Name field is first position)
				// FieldCode is not hard coded. In case the field position changes change below
				// expression accordingly
				Node systemNameNode = (Node) xPath.evaluate("/*/*/DMC_ROOT/DMC_MSGOUT/FIELD_CODE_AREA/fc_5001/text()",
						document, XPathConstants.NODE);
				if (systemNameNode != null) {
					String systemName = systemNameNode.getNodeValue();

					logger.info("DMCustomCodeAtToEndpointExit - systemName - " + systemName);

					String encryptedSystemName;
					encryptedSystemName = encrypter.encrypt(systemName);

					logger.info("DMCustomCodeAtToEndpointExit - encryptedSystemName - " + encryptedSystemName);

					systemNameNode.setTextContent(encryptedSystemName);
				}

				// Find Time Stamp node and encrypt the value (assuming the node for Time Stamp
				// field is second position)
				// FieldCode is not hard coded. In case the field position changes change below
				// expression accordingly
				Node txtimeNode = (Node) xPath.evaluate("/*/*/DMC_ROOT/DMC_MSGOUT/FIELD_CODE_AREA/fc_5002/text()",
						document, XPathConstants.NODE);
				if (txtimeNode != null) {
					String txtime = txtimeNode.getNodeValue();

					logger.info("DMCustomCodeAtToEndpointExit - txtime - " + txtime);

					String encryptedTimeStamp;
					encryptedTimeStamp = encrypter.encrypt(txtime);

					logger.info("DMCustomCodeAtToEndpointExit - encryptedTimeStamp - " + encryptedTimeStamp);

					txtimeNode.setTextContent(encryptedTimeStamp);
				}

				// Find MacValue node and encrypt the value (assuming the node for MacValue
				// field is third position)
				// FieldCode is not hard coded. In case the field position changes change below
				// expression accordingly
				Node querykey1Node = (Node) xPath.evaluate("/*/*/DMC_ROOT/DMC_MSGOUT/FIELD_CODE_AREA/fc_5004/text()",
						document, XPathConstants.NODE);
				String querykey1 = "";
				if (querykey1 != null) {
					querykey1 = querykey1Node.getNodeValue();

					logger.info("DMCustomCodeAtToEndpointExit - querykey1 - " + querykey1);
				}

				Node macValueNode = (Node) xPath.evaluate("/*/*/DMC_ROOT/DMC_MSGOUT/FIELD_CODE_AREA/fc_5003/text()",
						document, XPathConstants.NODE);
				if (macValueNode != null) {
					String macValue = macValueNode.getNodeValue();

					logger.info("DMCustomCodeAtToEndpointExit - macValue - " + macValue);

					String encryptedMacValue;
					// encryptedMacValue = encrypter.encrypt(macValue);
					encryptedMacValue = encrypter.encrypt(querykey1);

					logger.info("DMCustomCodeAtToEndpointExit - encryptedMacValue - " + encryptedMacValue);

					macValueNode.setTextContent(encryptedMacValue);
				}

				if (logger.isInfoEnabled()) {
					logger.info("DMCustomCodeAtToEndpointExit - encryption completed");
				}

				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer1 = transformerFactory.newTransformer();
				StringWriter stringWriter = new StringWriter();
				StreamResult streamResult = new StreamResult(stringWriter);
				DOMSource domSource = new DOMSource(document);

				transformer1.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer1.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				transformer1.transform(domSource, streamResult);

				String xmlString = stringWriter.getBuffer().toString();
				incomingMsg = xmlString;

				logger.info("DMCustomCodeAtToEndpointExit - Incoming Encrypted Request Message >>> " + incomingMsg);

				Transformer transformer = transformerFactory.newTransformer(stylesource);

				transformer.setOutputProperty("indent", "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

				StreamSource xmlSource = new StreamSource(new StringReader(incomingMsg));
				StringWriter stringWriter1 = new StringWriter();
				StreamResult result = new StreamResult(stringWriter1);
				transformer.transform(xmlSource, result);

				String xmlOutputForJCIC = stringWriter1.toString();

				if (logger.isTraceEnabled()) {
					logger.trace("DMCustomCodeAtToEndpointExit - message data encrypted >>> " + xmlOutputForJCIC);
				}

				if (logger.isInfoEnabled()) {
					logger.info("DMCustomCodeAtToEndpointExit - Message is transformed to JCIC format. >>> "
							+ xmlOutputForJCIC);
				}

				logger.info("DMCustomCodeAtToEndpointExit - XML Output for JCIC >>> " + xmlOutputForJCIC);

				// extract bytes of the transformed XML
				incomingBytes = xmlOutputForJCIC.getBytes();

			} catch (UnsupportedEncodingException e) {
				logger.error("DMCustomCodeAtToEndpointExit - unsopported encoding" + encoding);
			} catch (TransformerConfigurationException e) {
				logger.error("DMCustomCodeAtToEndpointExit - TransformerConfigurationException" + e.getMessage());
			} catch (TransformerException e) {
				logger.error("DMCustomCodeAtToEndpointExit - TransformerException" + e.getMessage());
			} catch (ParserConfigurationException e) {
				logger.error("DMCustomCodeAtToEndpointExit - ParserConfigurationException" + e.getMessage());
			} catch (SAXException e) {
				logger.error("DMCustomCodeAtToEndpointExit - SAXException" + e.getMessage());
			} catch (XPathExpressionException e) {
				logger.error("DMCustomCodeAtToEndpointExit - XPathExpressionException" + e.getMessage());
			} catch (IOException e) {
				logger.error("DMCustomCodeAtToEndpointExit - IOException" + e.getMessage());
			}
		}

		return incomingBytes;

	}

}
