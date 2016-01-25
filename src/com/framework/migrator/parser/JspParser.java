package com.framework.migrator.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.framework.migrator.helper.MigratorActionHelper;

public class JspParser {

	public static void parse(File file,Map<String,List<String>> formBeanMapping) {
		Document htmlFile = null;

		if (null != file ) {
			try {
				List<String> lines = FileUtils.readLines(file, "UTF-8");
				Collection<String> finalCollections = new ArrayList<String>();
				Collection<String> newCollections = new ArrayList<String>();
				String prefixString = "prefix=";
				String htmlPrefixString = null;
				String beanPrefixString = null;
				for (String line : lines) {
					// System.out.println("line:"+line);
					if (line.startsWith("<%@taglib")) {
						if (line.indexOf("html.tld") != -1) {
							try {
								htmlPrefixString = line.substring(line.indexOf(prefixString)+ prefixString.length(),line.indexOf("uri"));
							} catch (StringIndexOutOfBoundsException ex) {
								htmlPrefixString = line.substring(line.indexOf(prefixString)+ prefixString.length(),line.indexOf("%>"));
							}
						} else if (line.indexOf("bean.tld") != -1) {
							try {
								beanPrefixString = line.substring(line.indexOf(prefixString)+ prefixString.length(),line.indexOf("uri"));
							} catch (StringIndexOutOfBoundsException ex) {
								beanPrefixString = line.substring(line.indexOf(prefixString)+ prefixString.length(),line.indexOf("%>"));
							}
						}

					} else {
						if(line.startsWith("<html>") || finalCollections.size()>0){
							finalCollections.add(line);
					}
											
					}
				}

				
				
				FileUtils.writeLines(file, finalCollections);
				
				
				
				newCollections.add("<%@page contentType=\"text/html\" pageEncoding=\"UTF-8\"%>");
				newCollections.add("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
				newCollections.add("<%@ taglib prefix=\"form\" uri=\"http://www.springframework.org/tags/form\"%>");
				newCollections.add("<%@ taglib prefix=\"spring\" uri=\"http://www.springframework.org/tags\"%>");
				newCollections.add("<%@ taglib uri=\"http://java.sun.com/jsp/jstl/core\" prefix=\"c\" %>");

				String str = FileUtils.readFileToString(file);
				String commandName=MigratorActionHelper.getCommandNameForJsp(str, formBeanMapping);
				System.out.println("beanparsestring:" + beanPrefixString);
				System.out.println("htmlparsestring:" + htmlPrefixString);
				System.out.println("output string:" + str);

				htmlFile = Jsoup.parse(str);
				//htmlFile.outputSettings().syntax(Document.OutputSettings.Syntax.html);
				System.out.println("output html" + htmlFile.html());

				if (null != htmlPrefixString || null != beanPrefixString) {

					for (Element element : htmlFile.getAllElements()) {

						if (element.tagName().equalsIgnoreCase("html:errors")) {
							String tagName = element.tagName().replaceAll(htmlPrefixString.replace('"', ' ').trim()+ ":errors", "form:errors");
							element.tagName(tagName);
						}
						if (element.tagName().equalsIgnoreCase("html:form")) {
							Attributes attributes = element.attributes();
							System.out.println("attributes:"+ attributes.toString());
							if (!attributes.hasKey("method")) {
								attributes.put("method", "POST");
							}
							if (attributes.hasKey("action")) {
								attributes.put("action",attributes.get("action").replace("/",""));
							}
							attributes.put("id", "form");
							attributes.put(new org.jsoup.nodes.Attribute("commandName", commandName));
							String tagName = element.tagName().replaceAll(htmlPrefixString.replace('"', ' ').trim()+ ":form", "form:form");
							element.tagName(tagName);
						}

						if (element.tagName().equalsIgnoreCase("html:text")) {
							Attributes attributes = element.attributes();
							if (!attributes.hasKey("type")) {
								attributes.put("type", "text");
							}
							attributes.put("name", attributes.get("property"));
							attributes.put("id", attributes.get("property"));
							attributes.put("path", attributes.get("property"));
							attributes.remove("property");
							attributes.put("value", "");
							String tagName = element.tagName().replaceAll(htmlPrefixString.replace('"', ' ').trim()+ ":text", "form:input");
							element.tagName(tagName);

						}

						if (element.tagName().equalsIgnoreCase("html:password")) {
							Attributes attributes = element.attributes();
							if (!attributes.hasKey("type")) {
								attributes.put("type", "password");
							}
							attributes.put("name", attributes.get("property"));
							attributes.put("id", attributes.get("property"));
							attributes.put("path", attributes.get("property"));
							attributes.remove("property");
							attributes.put("value", "");
							String tagName = element.tagName().replaceAll(htmlPrefixString.replace('"', ' ').trim()+ ":password", "form:passowrd");
							element.tagName(tagName);
						}

						if (element.tagName().equalsIgnoreCase("html:submit")) {
							Attributes attributes = element.attributes();
							if (!attributes.hasKey("type")) {
								attributes.put("type", "submit");
							}
							attributes.put("name", "submit");
							attributes.put("value", attributes.get("value"));
							String tagName = element.tagName().replaceAll(htmlPrefixString.replace('"', ' ').trim()+ ":submit", "form:submit");
							element.tagName(tagName);
						}

						if (element.tagName().equalsIgnoreCase("bean:message")) {

							String tagName = element.tagName().replaceAll(beanPrefixString.replace('"', ' ').trim()+ ":message", "spring:message");
							element.tagName(tagName);
						}

						System.out.println("tag name:" + element.tagName());

						System.out.println("tag attributes:"+ element.attributes().toString());
						FileUtils.writeStringToFile(file, "");
						FileUtils.writeLines(file, newCollections);
						System.out.println("jsp output:"+htmlFile.html());
						FileUtils.writeStringToFile(file, StringEscapeUtils.unescapeHtml4(StringUtils.replace(htmlFile.html(), "commandname","commandName")), "UTF-8", true);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			throw new IllegalArgumentException(
					"valid filepath needs to passed.");
		}
	}
}
