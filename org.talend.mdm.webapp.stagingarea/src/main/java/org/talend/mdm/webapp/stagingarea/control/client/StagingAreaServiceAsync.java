/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.stagingarea.control.client;

import java.util.List;

import org.talend.mdm.webapp.stagingarea.control.shared.model.ConceptRelationshipModel;
import org.talend.mdm.webapp.stagingarea.control.shared.model.StagingAreaConfiguration;
import org.talend.mdm.webapp.stagingarea.control.shared.model.StagingAreaExecutionModel;
import org.talend.mdm.webapp.stagingarea.control.shared.model.StagingContainerModel;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface StagingAreaServiceAsync {

    void getStagingAreaConfig(AsyncCallback<StagingAreaConfiguration> callback);

    void getConceptRelation(AsyncCallback<ConceptRelationshipModel> callback);

    void getStagingContainerSummary(String dataContainer, String dataModel, AsyncCallback<StagingContainerModel> callback);

    void listCompletedTaskExecutions(String dataContainer, int start, int pageSize, AsyncCallback<List<String>> callback);

    void getExecutionStats(String dataContainer, String dataModel, String executionId,
            AsyncCallback<StagingAreaExecutionModel> callback);
}
