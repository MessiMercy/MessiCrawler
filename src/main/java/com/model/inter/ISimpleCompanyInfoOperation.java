package com.model.inter;

import com.model.SimpleCompanyInfo;

public interface ISimpleCompanyInfoOperation {
	SimpleCompanyInfo selectCompanyByID(int companyId);

	void addCompany(SimpleCompanyInfo simpleCompanyInfo);

	void deleteSimpleCompanyInfo(int companyId);

}
