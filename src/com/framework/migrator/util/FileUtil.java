package com.framework.migrator.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class FileUtil {

	public static List<File> getSubdirs(File file) {
		List<File> subdirs = Arrays.asList(file.listFiles(new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory();
			}
		}));
		subdirs = new ArrayList<File>(subdirs);
		List<File> deepSubdirs = new ArrayList<File>();
		for (File subdir : subdirs) {
			deepSubdirs.addAll(getSubdirs(subdir));
		}
		subdirs.addAll(deepSubdirs);
		return subdirs;
	}
	
	public static void createDirectory(String path){
		 try {
			FileUtils.forceMkdir(new File(path));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static boolean createNewFile(String path){
		try {
			return new File(path).createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
