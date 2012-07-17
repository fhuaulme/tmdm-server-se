/*
 * Copyright (C) 2006-2012 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.save.context;

import com.amalto.core.ejb.local.XmlServerSLWrapperLocal;
import com.amalto.core.history.Action;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.load.action.LoadAction;
import com.amalto.core.metadata.ComplexTypeMetadata;
import com.amalto.core.save.DocumentSaverContext;
import com.amalto.core.save.UserAction;
import com.amalto.core.util.XSDKey;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.List;

class BulkLoadContext implements DocumentSaverContext {

    private final String dataCluster;

    private final String dataModelName;

    private final BulkLoadSaver bulkLoadSaver;

    public BulkLoadContext(String dataCluster, String dataModelName, XSDKey keyMetadata, InputStream documentStream, LoadAction loadAction, XmlServerSLWrapperLocal server) {
        this.dataCluster = dataCluster;
        this.dataModelName = dataModelName;
        bulkLoadSaver = new BulkLoadSaver(loadAction, documentStream, keyMetadata, server);
    }

    public DocumentSaver createSaver() {
        return bulkLoadSaver;
    }

    public MutableDocument getDatabaseDocument() {
        throw new UnsupportedOperationException();
    }

    public MutableDocument getDatabaseValidationDocument() {
        throw new UnsupportedOperationException();
    }

    public MutableDocument getUserDocument() {
        throw new UnsupportedOperationException();
    }

    public void setUserDocument(MutableDocument document) {
        throw new UnsupportedOperationException();
    }

    public List<Action> getActions() {
        throw new UnsupportedOperationException();
    }

    public void setActions(List<Action> actions) {
        throw new UnsupportedOperationException();
    }

    public ComplexTypeMetadata getType() {
        throw new UnsupportedOperationException();
    }

    public String[] getId() {
        throw new UnsupportedOperationException();
    }

    public void setId(String[] id) {
        throw new UnsupportedOperationException();
    }

    public String getDataCluster() {
        return dataCluster;
    }

    public String getDataModelName() {
        return dataModelName;
    }

    public String getRevisionID() {
        throw new UnsupportedOperationException();
    }

    public void setDatabaseDocument(MutableDocument databaseDocument) {
        throw new UnsupportedOperationException();
    }

    public void setDatabaseValidationDocument(MutableDocument databaseValidationDocument) {
        throw new UnsupportedOperationException();
    }

    public void setRevisionId(String revisionID) {
        throw new UnsupportedOperationException();
    }

    public void setType(ComplexTypeMetadata type) {
        throw new UnsupportedOperationException();
    }

    public boolean hasMetAutoIncrement() {
        throw new UnsupportedOperationException();
    }

    public void setHasMetAutoIncrement(boolean hasMetAutoIncrement) {
        throw new UnsupportedOperationException();
    }

    public boolean preserveOldCollectionValues() {
        return false;
    }

    public MutableDocument getUpdateReportDocument() {
        throw new UnsupportedOperationException("No supported in this implementation.");
    }

    public void setUpdateReportDocument(MutableDocument updateReportDocument) {
        throw new UnsupportedOperationException("No supported in this implementation.");
    }

    @Override
    public UserAction getUserAction() {
        return UserAction.REPLACE;
    }

    @Override
    public void setUserAction(UserAction userAction) {
        // Only REPLACE for this context.
    }

    @Override
    public String getPartialUpdatePivot() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getPartialUpdateKey() {
        return StringUtils.EMPTY;
    }
}
