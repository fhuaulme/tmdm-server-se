// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.mdm.webapp.itemsbrowser2.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * DOC HSHU class global comment. Detailled comment
 */
public class AppHeader implements IsSerializable {

    private boolean isStandAloneMode = false;
    
    private boolean isUsingDefaultForm = false;

    private String datamodel = null;

    private String datacluster = null;

    /**
     * DOC HSHU AppHeader constructor comment.
     */
    public AppHeader() {

    }

    public boolean isStandAloneMode() {
        return isStandAloneMode;
    }

    public void setStandAloneMode(boolean isStandAloneMode) {
        this.isStandAloneMode = isStandAloneMode;
    }
    
    public boolean isUsingDefaultForm() {
        return isUsingDefaultForm;
    }

    public void setUsingDefaultForm(boolean isUsingDefaultForm) {
        this.isUsingDefaultForm = isUsingDefaultForm;
    }

    public String getDatamodel() {
        return datamodel;
    }

    public void setDatamodel(String datamodel) {
        this.datamodel = datamodel;
    }

    public String getDatacluster() {
        return datacluster;
    }

    public void setDatacluster(String datacluster) {
        this.datacluster = datacluster;
    }

    @Override
    public String toString() {
        return "AppHeader [datamodel=" + datamodel + ", datacluster=" + datacluster + ", isStandAloneMode=" + isStandAloneMode + "]";//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 
    }

}