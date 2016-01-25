package com.framework.migrator.main;

import japa.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.framework.migrator.helper.BeanHelper;
import com.framework.migrator.helper.MigratorActionHelper;
import com.framework.migrator.parser.JspParser;
import com.framework.migrator.parser.XmlParser;
import com.framework.migrator.util.CommonUtil;
import com.framework.migrator.util.FileUtil;

public class SourceMigrateAction {
	
	public static List<BeanHelper> beansInfo=new ArrayList<BeanHelper>();
	public static Map<String,List<String>> formBeanMapping=new HashMap<String,List<String>>(); 
	public static String contextScanPath="";
	
	
	private String getCurrentPackageName(String fileCanonicalPath){
		return fileCanonicalPath.substring(fileCanonicalPath.indexOf("src/", 0)+4,fileCanonicalPath.length()).replace('/', '.');
	}
	
	
	public static String getNewPackageName(String currentPackage,String fileCanonicalPath,String inputProjectPath){
		boolean isActionOnly=false;
		boolean isFormOnly=false;
		try {
			System.out.println("Package name"+currentPackage+":FilePath"+fileCanonicalPath+":InputprojectPath:"+inputProjectPath);
			File[] classes=CommonUtil.getAllClassesInPath(fileCanonicalPath);	
			for(File file:classes){
				System.out.println("class name:"+file.getName());
				String fileString=FileUtils.readFileToString(file);
				if(fileString.contains("extends ActionForm")){
					isFormOnly=true;
					BeanHelper beanHelper=new BeanHelper();
					beanHelper.setActionBean(false);
					beanHelper.setFormBean(true);
					beanHelper.setName(file.getName().split("\\.")[0]);
					beanHelper.setOldPackageInfo(currentPackage);
					beansInfo.add(beanHelper);
				}else if(fileString.contains("extends Action")){
					isActionOnly=true;
					BeanHelper beanHelper=new BeanHelper();
					beanHelper.setActionBean(true);
					beanHelper.setFormBean(false);
					System.out.println("file Name:"+file.getName());
					beanHelper.setName(file.getName().split("\\.")[0]);
					beanHelper.setOldPackageInfo(currentPackage);
					beanHelper.setDependentForms(MigratorActionHelper.findAllDependendForms(file,inputProjectPath));
					beansInfo.add(beanHelper);
				}else{
					BeanHelper beanHelper=new BeanHelper();
					beanHelper.setDependentForms(MigratorActionHelper.findAllDependendForms(file,inputProjectPath));
					beansInfo.add(beanHelper);
				}
				
			}
			
			if(isActionOnly && !isFormOnly){
				return currentPackage.substring(0, currentPackage.lastIndexOf('.'))+"."+"controller";
			}else if(isFormOnly && !isActionOnly){
				return currentPackage.substring(0, currentPackage.lastIndexOf('.'))+"."+"command";
			}
			
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return currentPackage;
		
	}
	
	
	
	private static void visitAndProcessAllFiles(String oldPackagePath,String newPackagePath,String inputProjectPath,String outputAppPath,List<BeanHelper> beansInfo,ArrayList<String> errors) throws IOException{
		
		String sourcePackageAppPath=inputProjectPath+"/src/"+oldPackagePath.replace('.', '/');
		String destPackageAppPath=outputAppPath+"/src/"+newPackagePath.replace('.', '/');
		File sourceDir=new File(sourcePackageAppPath);
		File destDir=new File(destPackageAppPath); 
		//1.create package directory
		FileUtil.createDirectory(destPackageAppPath);
		//2.Copy all files from source project to destination project
		FileUtils.copyDirectory(sourceDir, destDir);
		//3.Goto new path
		for(File file:destDir.listFiles()){
			if(!file.getName().startsWith(".") && file.isFile() && file.getName().endsWith(".java")){
				new MigratorActionHelper().parseAndProcess(file, beansInfo,newPackagePath,errors);
			}
		}
		
	}
	
	public  ArrayList<String> processSourceFiles(File dir,String inputProjectPath,String outputAppPath){
		ArrayList<String> errorMessgs=new ArrayList<String>();
			try {
				
				System.out.println("dir Name;"+dir.getName()+"dir path:"+dir.getCanonicalPath());
				System.out.println(dir.list().toString());
				boolean doProcess=false;				
				for(File file:dir.listFiles()){
					System.out.println("file path:"+file.getCanonicalPath());
					if(!file.getName().startsWith(".") && file.isFile()){
						doProcess=true;
						break;
					}
				}
				if(doProcess){
				String currentPackageName=getCurrentPackageName(dir.getCanonicalPath());				
				String newPackageName=getNewPackageName(currentPackageName,dir.getCanonicalPath(),inputProjectPath);				
				visitAndProcessAllFiles(currentPackageName,newPackageName,inputProjectPath,outputAppPath,beansInfo,errorMessgs);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		
		return errorMessgs;
	}
	
	private ArrayList<String> processJspFile(String fileName,String destJspDirPath,ArrayList<String> errors){
		File file=new File(destJspDirPath+"/"+fileName);
		JspParser.parse(file, SourceMigrateAction.formBeanMapping);
		return errors;
	}
	
	private ArrayList<String> processConfigFile(File file,String outputDirPath,ArrayList<String> errors){
		System.out.println("file name:"+file.getName()+",outputDirPath:"+outputDirPath);
		XmlParser.parseConfigXML(SourceMigrateAction.contextScanPath, "./config/spring/dispatcher-servlet.xml", outputDirPath);
		XmlParser.parseWebXML(file,outputDirPath);
		return errors;
	}
	
	public  ArrayList<String> processWebContent(File dir,String inputProjectPath,String outputAppPath){
		ArrayList<String> errorMessgs=new ArrayList<String>();
		try{
			
			String sourcePackageAppPath=inputProjectPath+"/WebContent/";
			String destPackageAppPath=outputAppPath+"/WebContent/";
			
			//2.Copy all files from source project to destination project
			for(File file:dir.listFiles()){
				System.out.println("list of files:"+file.getCanonicalPath());
				if(file.getName().endsWith(".jsp")){
					File jspDir=new File(destPackageAppPath+"/jsp");
					System.out.println("jsp path"+jspDir.getCanonicalPath());
					if(!jspDir.exists()){
						jspDir.mkdir();
					}
					FileUtils.copyFileToDirectory(file, jspDir);
					processJspFile(file.getName(),jspDir.getCanonicalPath(),errorMessgs);
				}else if(file.getName().equals("META-INF")){
					FileUtils.copyDirectory(new File(sourcePackageAppPath+"META-INF"), new File(destPackageAppPath+"META-INF"));
				}else if(file.getName().equals("WEB-INF")){
					for(File subFile:file.listFiles()){
						if(subFile.getName().equals("web.xml")){
						processConfigFile(subFile,destPackageAppPath+"WEB-INF",errorMessgs);	
						}else if(subFile.getName().equals("lib")){
							for(File libFile:subFile.listFiles()){
								if(libFile.getName().endsWith(".jar")){
									String destLibPath=destPackageAppPath+"/WEB-INF/lib";
									File destLibDir=new File(destLibPath);
									if(!StringUtils.containsIgnoreCase(libFile.getName(), "struts") && !new File(destLibPath,libFile.getName()).exists()){
										FileUtils.copyFileToDirectory(libFile,destLibDir);
									}
								}
							}
						}
					}
				}else{
					if(file.isFile() && !file.getName().startsWith(".")){
						FileUtils.copyFileToDirectory(file, new File(destPackageAppPath));
					}else if(file.isDirectory() && !file.getName().equals("lib")){
						FileUtils.copyDirectory(new File(sourcePackageAppPath+file.getName()), new File(destPackageAppPath+file.getName()));
					}
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return errorMessgs;
	}
}
