/*
 * Copyright (C) 2006-2014 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.ext.publish.model;

import java.io.Serializable;

public class PicturePojo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 7280227322541249838L;

    private String name;

    private String fileName;

    private String catalog;

    private String uri;

    private String redirectUri;

    public PicturePojo(String name, String fileName, String catalog, String uri, String redirectUri) {
        super();
        this.name = name;
        this.fileName = fileName;
        this.catalog = catalog;
        this.uri = uri;
        this.redirectUri = redirectUri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

}