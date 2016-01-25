package com.framework.migrator.helper;

import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import com.framework.migrator.parser.ActionBeanParser;
import com.framework.migrator.parser.BeanParser;
import com.framework.migrator.parser.FormBeanParser;

public class MigratorActionHelper {

	public ArrayList<String> parseAndProcess(File file,
			List<BeanHelper> beansInfo, String packageName,
			List<String> errorMssgs) {

		System.out.println("File name:" + file.getName() + "is in process.");

		List<String> actionBeans = getAllActionBeanNames(beansInfo);
		List<String> formBeans = getAllFormBeanNames(beansInfo);

		for (String str : actionBeans) {
			System.out.println("Action Class Name:" + str);
		}

		for (String str : formBeans) {
			System.out.println("Form Class Name:" + str);
		}

		if (actionBeans.contains(file.getName().split("\\.")[0])) {
			return ActionBeanParser.parse(file, beansInfo, packageName,
					errorMssgs);
		} else if (formBeans.contains(file.getName().split("\\.")[0])) {
			return FormBeanParser.parse(file, beansInfo, packageName,
					errorMssgs);
		} else {
			return BeanParser.parse(file, beansInfo, packageName, errorMssgs);
		}

	}

	public static List<String> findAllDependendForms(File file,String inputAppPath){
		
		FileInputStream in = null;
		CompilationUnit cu = null;
		List<String> forms=new ArrayList<String>();
		
			try {
					in=new FileInputStream(file);
					cu = japa.parser.JavaParser.parse(in);
					Iterator<ImportDeclaration> itr=cu.getImports().iterator();		        						        		        
					while(itr.hasNext()){
						ImportDeclaration importDeclaration=(ImportDeclaration) itr.next();
						System.out.println("import name:"+importDeclaration.getName().toString()+",Class name:"+importDeclaration.getName().getName());
						if(!importDeclaration.getName().toString().contains("org.apache.struts")){
							String srcPath=inputAppPath+"/src/";
							String importFilePath=srcPath+importDeclaration.getName().toString().replace('.', '/')+".java";
							File importFile=new File(importFilePath);
							String importFileString = null;
							try {
								importFileString = FileUtils.readFileToString(importFile);
								if(importFileString.contains("extends ActionForm")){
									forms.add(importDeclaration.getName().toString());
								}
							} catch (IOException e) {
								System.out.println("file name:"+importFile.getName()+" not found");
							}		        									
						}
					}
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}catch (ParseException e1) {
					e1.printStackTrace();
				}finally {
		                 try {
						       in.close();
					         } catch (IOException e) {
					        	 System.out.println(e.getMessage());
					  }
			 } 
           
		System.out.println("list of dependent forms:"+Arrays.toString(forms.toArray()));	
			
		return forms;
		
	}

	public static String getCommandNameForJsp(String fileString,
			Map<String, List<String>> formBeanMapping) {

		if (!formBeanMapping.isEmpty()) {
			int i = 0;
			for (Map.Entry<String, List<String>> entry : formBeanMapping
					.entrySet()) {
				List<String> values = entry.getValue();
				System.out.println("values used to derive command name:"
						+ Arrays.toString(values.toArray()));
				for (String field : values) {
					if (fileString.contains(field)) {
						i++;
					}
				}
				if (values.size() == i) {
					System.out.println("command Name found:" + entry.getKey());
					return entry.getKey();
				}
			}
		}

		return "";
	}

	private List<String> getAllActionBeanNames(List<BeanHelper> beansInfo) {
		List<String> actionBeans = new ArrayList<String>();

		for (BeanHelper beanHelper : beansInfo) {
			if (beanHelper.isActionBean()) {
				actionBeans.add(beanHelper.getName());
			}
		}
		return actionBeans;
	}

	private List<String> getAllFormBeanNames(List<BeanHelper> beansInfo) {
		List<String> formBeans = new ArrayList<String>();

		for (BeanHelper beanHelper : beansInfo) {
			if (beanHelper.isFormBean()) {
				formBeans.add(beanHelper.getName());
			}
		}
		return formBeans;
	}
}
