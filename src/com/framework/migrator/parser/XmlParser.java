package com.framework.migrator.parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class XmlParser {

	@SuppressWarnings("resource")
	public static void parseConfigXML(String contextScanPath,String inputFilePath,String outputFilePath) {
	    try {
	    	File inputFile=new File(inputFilePath);
		    String inputFileString=FileUtils.readFileToString(inputFile);
			FileUtils.copyFileToDirectory(inputFile, new File(outputFilePath));
			inputFileString=inputFileString.replace("BASE_PKG", contextScanPath);
			System.out.println("finalConfigString:"+inputFileString);    
			FileOutputStream  out = new FileOutputStream(new File(outputFilePath+"/"+inputFile.getName()));
			out.write(inputFileString.getBytes());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public static void parseWebXML(File xmlFile,String outputDirPath) {
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		try {
			FileUtils.copyFileToDirectory(xmlFile, new File(outputDirPath));
			doc = (Document) builder.build(xmlFile);
			Element webAppNode = doc.getRootElement();
			String servletName = null;

			for (Element element : webAppNode.getChildren()) {
				if (element.getName().equals("servlet")) {
					Element servletClassElement = element.getChild("servlet-class", element.getNamespace());
					servletName = element.getChild("servlet-name",element.getNamespace()).getText();
					if (servletClassElement.getText().equalsIgnoreCase("org.apache.struts.action.ActionServlet")) {
						servletClassElement.setText("org.springframework.web.servlet.DispatcherServlet");
					}

					Element initParamElement = element.getChild("init-param",element.getNamespace());
					if (initParamElement.getChild("param-name", element.getNamespace()).getText().equalsIgnoreCase("config")) {
						initParamElement.getChild("param-name",element.getNamespace()).setText("contextConfigLocation");
						initParamElement.getChild("param-value",element.getNamespace()).setText("/WEB-INF/dispatcher-servlet.xml");
					}
				}

				if (element.getName().equalsIgnoreCase("servlet-mapping")) {
					Element servletNameElement = element.getChild("servlet-name", element.getNamespace());
					if (servletNameElement.getText().equalsIgnoreCase(servletName)) {
						element.getChild("url-pattern", element.getNamespace()).setText("/");
					}
				}
				
				if(element.getName().equalsIgnoreCase("welcome-file-list")){
					for(Element child:element.getChildren()){
						String childValue=child.getText();
						if(childValue.endsWith(".jsp")){
							child.setText("/WEB-INF/jsp/"+childValue);
						}
					}
				}
			}

			webAppNode.removeChildren("jsp-config", webAppNode.getNamespace());

			XMLOutputter xmlOutput = new XMLOutputter();

			// display nice nice
			xmlOutput.setFormat(Format.getPrettyFormat());
			xmlOutput.output(doc, new FileWriter(outputDirPath+"/"+xmlFile.getName()));

			System.out.println("File updated!");

		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
