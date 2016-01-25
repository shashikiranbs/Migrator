package com.framework.migrator.parser;

import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.framework.migrator.helper.BeanHelper;
import com.framework.migrator.main.SourceMigrateAction;


public class FormBeanParser {

	@SuppressWarnings({ "unchecked", "resource" })
	public static ArrayList<String> parse(File file,List<BeanHelper> beansInfo,String packageName,List<String> errorMssgs){
		try {
			
			String fileName=file.getName();
			
			if(StringUtils.containsIgnoreCase(fileName, "form")){
				fileName=fileName.replace("form", "Command");
				fileName=fileName.replace("Form", "Command");
			}
			
			System.out.println("new file name:"+fileName);			
			System.out.println("old file path:"+file.getCanonicalPath());			
			String newFilePath=file.getCanonicalPath().replace(file.getName(), fileName);
			System.out.println("new file path:"+newFilePath);
			File newFile=new File(newFilePath);
			file.renameTo(newFile);			
			System.out.println("new canonical file path:"+newFile.getCanonicalPath());
			
			FileInputStream in = new FileInputStream(newFile);
			CompilationUnit cu = null;
	        try {
	            // parse the file
	            try {
					cu = japa.parser.JavaParser.parse(in);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				};
	        } finally {
	            try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	        
	        if(!cu.getPackage().getName().getName().equalsIgnoreCase(packageName)){
	        	cu.setPackage(new PackageDeclaration(new NameExpr(packageName)));
	        }	        
	        List<ImportDeclaration> importDeclarationList=new ArrayList<ImportDeclaration>(); 
	        Iterator<ImportDeclaration> itr=cu.getImports().iterator();
	        while(itr.hasNext()){
	        	ImportDeclaration importDeclaration=(ImportDeclaration) itr.next();
	        	if(!importDeclaration.getName().toString().contains("org.apache.struts"))
	        	importDeclarationList.add(importDeclaration);	        	
	        }
	        cu.setImports(importDeclarationList);       
	        new ClassDeclarationVisitor().visit(cu, fileName.split("\\.")[0]);
	        List<String> fields=new ArrayList<String>();
	        new FieldDeclarationVisitor().visit(cu, fields);
	        fileName=fileName.split("\\.")[0];
	        fileName=fileName.substring(0, 1).toLowerCase() + fileName.substring(1);
	        SourceMigrateAction.formBeanMapping.put(fileName, fields);
	        System.out.println(cu.toString());
	        FileOutputStream out = new FileOutputStream(newFile);
	        out.write(cu.toString().getBytes());
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	
	
	@SuppressWarnings("rawtypes")
	private static class ClassDeclarationVisitor extends VoidVisitorAdapter {
		@Override
		public void visit(ClassOrInterfaceDeclaration n, Object arg) {
			n.setExtends(null);
			n.setName((String)arg);
		}
    }
	
	@SuppressWarnings("rawtypes")
	private static class FieldDeclarationVisitor extends VoidVisitorAdapter{

		@SuppressWarnings("unchecked")
		@Override
		public void visit(FieldDeclaration n, Object arg) {
			List<String> fields=(List<String>)arg;
			for(VariableDeclarator varDecl:n.getVariables()){
				String fieldName=varDecl.getId().getName();
				fieldName=fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
				fields.add(fieldName);
			}
		}
		
	}

}
