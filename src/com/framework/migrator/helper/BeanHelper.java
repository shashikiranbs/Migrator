package com.framework.migrator.helper;

import java.util.List;

public class BeanHelper {

	private String name;
	private String oldPackageInfo;
	private String newPackageInfo;
	private List<String> dependentForms;
	private boolean isActionBean;
	private boolean isFormBean;

	public boolean isActionBean() {
		return isActionBean;
	}

	public void setActionBean(boolean isActionBean) {
		this.isActionBean = isActionBean;
	}

	public boolean isFormBean() {
		return isFormBean;
	}

	public void setFormBean(boolean isFormBean) {
		this.isFormBean = isFormBean;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOldPackageInfo() {
		return oldPackageInfo;
	}

	public void setOldPackageInfo(String oldPackageInfo) {
		this.oldPackageInfo = oldPackageInfo;
	}

	public String getNewPackageInfo() {
		return newPackageInfo;
	}

	public void setNewPackageInfo(String newPackageInfo) {
		this.newPackageInfo = newPackageInfo;
	}

	public List<String> getDependentForms() {
		return dependentForms;
	}

	public void setDependentForms(List<String> dependentForms) {
		this.dependentForms = dependentForms;
	}

	

}
