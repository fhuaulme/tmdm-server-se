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

@XmlType(name="WSGetRoutingOrderV2ByCriteriaWithPaging")
public class WSGetRoutingOrderV2ByCriteriaWithPaging {
    protected com.amalto.core.webservice.WSRoutingOrderV2SearchCriteriaWithPaging wsSearchCriteriaWithPaging;
    
    public WSGetRoutingOrderV2ByCriteriaWithPaging() {
    }
    
    public WSGetRoutingOrderV2ByCriteriaWithPaging(com.amalto.core.webservice.WSRoutingOrderV2SearchCriteriaWithPaging wsSearchCriteriaWithPaging) {
        this.wsSearchCriteriaWithPaging = wsSearchCriteriaWithPaging;
    }
    
    public com.amalto.core.webservice.WSRoutingOrderV2SearchCriteriaWithPaging getWsSearchCriteriaWithPaging() {
        return wsSearchCriteriaWithPaging;
    }
    
    public void setWsSearchCriteriaWithPaging(com.amalto.core.webservice.WSRoutingOrderV2SearchCriteriaWithPaging wsSearchCriteriaWithPaging) {
        this.wsSearchCriteriaWithPaging = wsSearchCriteriaWithPaging;
    }
}
