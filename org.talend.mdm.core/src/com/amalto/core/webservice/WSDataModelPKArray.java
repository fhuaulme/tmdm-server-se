/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.webservice;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="WSDataModelPKArray")
public class WSDataModelPKArray {
    protected com.amalto.core.webservice.WSDataModelPK[] wsDataModelPKs;
    
    public WSDataModelPKArray() {
    }
    
    public WSDataModelPKArray(com.amalto.core.webservice.WSDataModelPK[] wsDataModelPKs) {
        this.wsDataModelPKs = wsDataModelPKs;
    }
    
    public com.amalto.core.webservice.WSDataModelPK[] getWsDataModelPKs() {
        return wsDataModelPKs;
    }
    
    public void setWsDataModelPKs(com.amalto.core.webservice.WSDataModelPK[] wsDataModelPKs) {
        this.wsDataModelPKs = wsDataModelPKs;
    }
}
