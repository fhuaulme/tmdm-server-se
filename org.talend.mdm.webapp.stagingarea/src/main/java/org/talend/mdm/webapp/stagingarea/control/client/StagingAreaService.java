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

import org.talend.mdm.webapp.base.client.exception.ServiceException;
import org.talend.mdm.webapp.stagingarea.control.shared.model.ConceptRelationshipModel;
import org.talend.mdm.webapp.stagingarea.control.shared.model.StagingAreaConfiguration;
import org.talend.mdm.webapp.stagingarea.control.shared.model.StagingAreaExecutionModel;
import org.talend.mdm.webapp.stagingarea.control.shared.model.StagingContainerModel;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("StagingAreaService")
public interface StagingAreaService extends RemoteService {

    public StagingAreaConfiguration getStagingAreaConfig();

    public ConceptRelationshipModel getConceptRelation() throws ServiceException;

    public StagingContainerModel getStagingContainerSummary(String dataContainer, String dataModel);

    public List<String> listCompletedTaskExecutions(String dataContainer, int start, int pageSize);

    public StagingAreaExecutionModel getExecutionStats(String dataContainer, String dataModel, String executionId);
}
