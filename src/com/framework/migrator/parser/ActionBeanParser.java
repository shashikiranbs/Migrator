package com.framework.migrator.parser;

import japa.parser.ASTHelper;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.expr.MarkerAnnotationExpr;
import japa.parser.ast.expr.MemberValuePair;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.NormalAnnotationExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.framework.migrator.helper.BeanHelper;
import com.framework.migrator.main.SourceMigrateAction;

public class ActionBeanParser {

	@SuppressWarnings({ "unchecked", "resource" })
	public static ArrayList<String> parse(File file,List<BeanHelper> beansInfo,String packageName,List<String> errorMssgs){
		try {
			
			String fileName=file.getName();
			
			if(StringUtils.containsIgnoreCase(fileName, "action")){
				fileName=fileName.replace("action", "Controller");
				fileName=fileName.replace("Action", "Controller");
			}
			System.out.println("new file name:"+fileName);			
			System.out.println("old file path:"+file.getCanonicalPath());			
			String newFilePath=file.getCanonicalPath().replace(file.getName(), fileName);
			System.out.println("new file path:"+newFilePath);
			File newFile=new File(newFilePath);
			file.renameTo(newFile);			
			System.out.println("new canonical file path:"+newFile.getCanonicalPath());
			BeanHelper beanInfo=null;
			
			for(BeanHelper beanHelper:beansInfo){
				if(StringUtils.equals(beanHelper.getName(), file.getName().split("\\.")[0])){
					beanHelper.setNewPackageInfo(packageName);
					beanInfo=beanHelper;					
					break;
				}
			}
			
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
	        	if(SourceMigrateAction.contextScanPath.isEmpty())SourceMigrateAction.contextScanPath=packageName;
	        }
	        
	        Iterator<ImportDeclaration> itr=cu.getImports().iterator();
	        
	        List<ImportDeclaration> importDeclarationList=new ArrayList<ImportDeclaration>();
	        
	        while(itr.hasNext()){
	        	ImportDeclaration importDeclaration=(ImportDeclaration) itr.next();
	        	if(!importDeclaration.getName().toString().contains("org.apache.struts")){
	        		if(beanInfo.getDependentForms().contains(importDeclaration.getName().toString())){
	        			String importFormName=importDeclaration.getName().getName();
	        			importFormName=importFormName.replace("form", "Command");
	        			importFormName=importFormName.replace("Form", "Command");
	        			String importStatement=importDeclaration.getName().toString();
	        			importStatement=importStatement.replace(importDeclaration.getName().getName(), "command."+importFormName);
	        			importStatement=importStatement.replace("form.", "");
	        			importDeclaration.setName(new NameExpr(importStatement));
	        		}
	        		importDeclarationList.add(importDeclaration);
	        	}
	        	
	        }	
	        importDeclarationList.add(new ImportDeclaration(new NameExpr("org.springframework.stereotype.Controller"),false,false));
	        importDeclarationList.add(new ImportDeclaration(new NameExpr("org.springframework.web.bind.annotation.ModelAttribute"),false,false));
	        importDeclarationList.add(new ImportDeclaration(new NameExpr("org.springframework.web.bind.annotation.RequestMapping"),false,false));
	        importDeclarationList.add(new ImportDeclaration(new NameExpr("org.springframework.web.bind.annotation.RequestMethod"),false,false));
	        importDeclarationList.add(new ImportDeclaration(new NameExpr("org.springframework.web.bind.annotation.SessionAttributes"),false,false));
	        importDeclarationList.add(new ImportDeclaration(new NameExpr("org.springframework.web.servlet.ModelAndView"),false,false));
	        importDeclarationList.add(new ImportDeclaration(new NameExpr("org.springframework.ui.ModelMap"),false,false));
	        importDeclarationList.add(new ImportDeclaration(new NameExpr("org.springframework.validation.BindingResult"),false,false));
	      
	        cu.setImports(importDeclarationList);
	        new MethodChangerVisitor().visit(cu, beanInfo);         
	        new ClassDeclarationVisitor().visit(cu, fileName.split("\\.")[0]);
	       
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
			MarkerAnnotationExpr markerAnnotationExpr = new MarkerAnnotationExpr(new NameExpr("Controller"));
	        List<AnnotationExpr> annotationExprList = new ArrayList<AnnotationExpr>();
	        annotationExprList.add(markerAnnotationExpr);
	        n.setAnnotations(annotationExprList);
		}
    }

	/**
     * Simple visitor implementation for visiting MethodDeclaration nodes.
     */
    @SuppressWarnings("rawtypes")
	private static class MethodChangerVisitor extends VoidVisitorAdapter {

        @Override
        public void visit(MethodDeclaration n, Object arg) {
        	
        	System.out.println("getBody"+n.getBody());
            BlockStmt blockStmnt=n.getBody();
            System.out.println("blockStmt:"+blockStmnt.getStmts().size());
            BeanHelper beanInfo=(BeanHelper)arg;
			List<String> dependentForms=beanInfo.getDependentForms();
            Map<String,String> formNameMap=new HashMap<String,String>();
            String foundFormName="";
            boolean skipToProcess=true;
            for(String form:dependentForms){
            	String formName=StringUtils.substringAfterLast(form, ".");           	
            	formName=formName.replace("form", "Command");
            	formName=formName.replace("Form", "Command"); 
            	formNameMap.put(StringUtils.substringAfterLast(form, "."), formName);
            	foundFormName=foundFormName!=null?formName:"";
            }
            
            List<Statement> statements=new ArrayList<Statement>();
            Statement exprStatement1=new ExpressionStmt(new NameExpr("ModelMap modelMap=new ModelMap()")); 
            String statementString="modelMap.put(\"BEAN_NAME\",BEAN_NAME)";
            statementString=statementString.replace("BEAN_NAME", foundFormName!=null?foundFormName.substring(0, 1).toLowerCase() + foundFormName.substring(1):"BEAN_NAME");
            Statement exprStatement2=new ExpressionStmt(new NameExpr(statementString)); 		   
            statements.add(exprStatement1);
            statements.add(exprStatement2);
            
            for(Statement stmnt:blockStmnt.getStmts()){
                if(stmnt instanceof ExpressionStmt){
                	ExpressionStmt expStmnt=(ExpressionStmt)stmnt;
                	String statementInfo=stmnt.toString();
                	for(Entry<String, String> entries:formNameMap.entrySet()){
                		String actualFormName=entries.getKey();
                		String formNameInstance=actualFormName.substring(0, 1).toLowerCase() + actualFormName.substring(1);
                		String actualNewFormName=entries.getValue();
                		String newFormNameInstance=actualNewFormName.substring(0, 1).toLowerCase() + actualNewFormName.substring(1);
                		if(statementInfo.contains("("+actualFormName+")")){
                			skipToProcess=true;
                			break;
                		}else if(statementInfo.contains(actualFormName)){
                			statementInfo=statementInfo.replace(actualFormName, actualNewFormName);
                		}else if(statementInfo.contains(formNameInstance)){
                			statementInfo=statementInfo.replace(formNameInstance, newFormNameInstance);
                		}else{
                			statementInfo=statementInfo.replace("return mapping.findForward(\"failure\");", "return (new ModelAndView(\"failure\", modelMap));");
                			statementInfo=statementInfo.replace("return mapping.findForward(\"success\");", "return (new ModelAndView(\"success\", modelMap));");
                		}
                	}
                	
                	if(!skipToProcess){
                	expStmnt.setExpression(new NameExpr(statementInfo));
                	statements.add(expStmnt);
                	}
                }else if(stmnt instanceof IfStmt){
                	IfStmt ifStmnt=(IfStmt)stmnt;
                	String conditionExpr="";
                	String thenStmntInfo="";
                	String elseStmntInfo="";
                	for(Entry<String, String> entries:formNameMap.entrySet()){
                		String actualFormName=entries.getKey();
                		String formNameInstance=actualFormName.substring(0, 1).toLowerCase() + actualFormName.substring(1);
                		String actualNewFormName=entries.getValue();
                		String newFormNameInstance=actualNewFormName.substring(0, 1).toLowerCase() + actualNewFormName.substring(1);
                		
                		conditionExpr=ifStmnt.getCondition().toString();
                		
                		thenStmntInfo=ifStmnt.getThenStmt().toString();
                		thenStmntInfo=thenStmntInfo.replace("{", "");
            			thenStmntInfo=thenStmntInfo.replace("}", "");
            			thenStmntInfo=thenStmntInfo.replace(";", "");
                		
                		elseStmntInfo=ifStmnt.getElseStmt().toString();
                		elseStmntInfo=elseStmntInfo.replace("{", "");
            			elseStmntInfo=elseStmntInfo.replace("}", "");
            			elseStmntInfo=elseStmntInfo.replace(";", "");
                		
                		if(conditionExpr.contains(actualFormName)){
                			conditionExpr=conditionExpr.replace(actualFormName, actualNewFormName);
                		}else if(conditionExpr.contains(formNameInstance)){
                			conditionExpr=conditionExpr.replace(formNameInstance, newFormNameInstance);
                		}else{
                			conditionExpr=conditionExpr.replace("return mapping.findForward(\"failure\")", "return (new ModelAndView(\"failure\", modelMap))");
                			conditionExpr=conditionExpr.replace("return mapping.findForward(\"success\")", "return (new ModelAndView(\"success\", modelMap))");
                		}
                		
                		if(thenStmntInfo.contains(actualFormName)){
                			thenStmntInfo=thenStmntInfo.replace(actualFormName, actualNewFormName);
                		}else if(thenStmntInfo.contains(formNameInstance)){
                			thenStmntInfo=thenStmntInfo.replace(formNameInstance, newFormNameInstance);
                		}else{
                			thenStmntInfo=thenStmntInfo.replace("return mapping.findForward(\"failure\")", "return (new ModelAndView(\"failure\", modelMap))");
                			thenStmntInfo=thenStmntInfo.replace("return mapping.findForward(\"success\")", "return (new ModelAndView(\"success\", modelMap))");
                		}
                		
                		if(elseStmntInfo.contains(actualFormName)){
                			elseStmntInfo=elseStmntInfo.replace(actualFormName, actualNewFormName);
                		}else if(elseStmntInfo.contains(formNameInstance)){
                			elseStmntInfo=elseStmntInfo.replace(formNameInstance, newFormNameInstance);
                		}else{
                			elseStmntInfo=elseStmntInfo.replace("return mapping.findForward(\"failure\")", "return (new ModelAndView(\"failure\", modelMap))");
                			elseStmntInfo=elseStmntInfo.replace("return mapping.findForward(\"success\")", "return (new ModelAndView(\"success\", modelMap))");
                		}                				
                	}
            
                	List<Statement> stmnts=new ArrayList<Statement>();
					stmnts.add(new ExpressionStmt(new NameExpr(thenStmntInfo.trim())));
                	BlockStmt thenBlkStmt=new BlockStmt(stmnts);
                	
                	stmnts=new ArrayList<Statement>();
					stmnts.add(new ExpressionStmt(new NameExpr(elseStmntInfo.trim())));
                	BlockStmt elseBlkStmt=new BlockStmt(stmnts);
					
                	ifStmnt.setCondition(new NameExpr(conditionExpr));
                	ifStmnt.setThenStmt(thenBlkStmt);
                	ifStmnt.setElseStmt(elseBlkStmt);

                	statements.add(ifStmnt);
                }
            }
           
            n.setBody(new BlockStmt(statements));
            
            System.out.println("new getBody"+n.getBody());
            
        	List<MemberValuePair> valuePairs=new ArrayList<MemberValuePair>();
        	MemberValuePair valuePair1=new MemberValuePair();
        	valuePair1.setName("value");
        	String actionName=foundFormName!=null?foundFormName.replace("Command", ""):"Action";
        	actionName=actionName.substring(0, 1).toLowerCase() + actionName.substring(1);
        	valuePair1.setValue(new StringLiteralExpr("/"+actionName));
        	
        	MemberValuePair valuePair2=new MemberValuePair();
        	valuePair2.setName("method");
        	valuePair2.setValue(new NameExpr("RequestMethod.POST"));
        	
			valuePairs.add(valuePair1);
			valuePairs.add(valuePair2);
						
        	NormalAnnotationExpr normalAnnotationExpr = new NormalAnnotationExpr(new NameExpr("RequestMapping"),valuePairs);
	        List<AnnotationExpr> annotationExprList = new ArrayList<AnnotationExpr>();
	        annotationExprList.add(normalAnnotationExpr);
	        n.setAnnotations(annotationExprList);
            n.setModifiers(ModifierSet.PUBLIC);
            n.setType(new ClassOrInterfaceType("ModelAndView"));
            n.setParameters(null);
            // create the new parameter
            Parameter httpRequest = ASTHelper.createParameter(new ClassOrInterfaceType("HttpServletRequest"), "request");
            Parameter httpResponse = ASTHelper.createParameter(new ClassOrInterfaceType("HttpServletResponse"), "resonse");
            String formParameter=(foundFormName!=null)?foundFormName.substring(0, 1).toLowerCase() + foundFormName.substring(1):"";
            Parameter modelAttribute = ASTHelper.createParameter(new ClassOrInterfaceType("@ModelAttribute(\""+formParameter+"\")"+foundFormName), formParameter);
            Parameter bindingResult = ASTHelper.createParameter(new ClassOrInterfaceType("BindingResult"), "bindingResult");
            
            List<Parameter> parameters=new ArrayList<Parameter>();
            parameters.add(httpRequest);
            parameters.add(httpResponse);
            parameters.add(modelAttribute);
            parameters.add(bindingResult);
            
            n.setParameters(parameters); 
        }

    }
}
