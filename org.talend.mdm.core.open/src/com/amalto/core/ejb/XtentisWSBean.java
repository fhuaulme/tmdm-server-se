package com.amalto.core.ejb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.jboss.security.Base64Encoder;
import org.talend.mdm.commmon.util.core.MDMConfiguration;
import org.talend.mdm.commmon.util.webapp.XObjectType;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import sun.misc.BASE64Decoder;

import com.amalto.connector.jca.InteractionSpecImpl;
import com.amalto.connector.jca.RecordFactoryImpl;
import com.amalto.core.delegator.BeanDelegatorConfigReader;
import com.amalto.core.delegator.BeanDelegatorContainer;
import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.ejb.local.TransformerCtrlLocal;
import com.amalto.core.objects.backgroundjob.ejb.BackgroundJobPOJO;
import com.amalto.core.objects.backgroundjob.ejb.BackgroundJobPOJOPK;
import com.amalto.core.objects.datacluster.ejb.DataClusterPOJO;
import com.amalto.core.objects.datacluster.ejb.DataClusterPOJOPK;
import com.amalto.core.objects.datamodel.ejb.DataModelPOJO;
import com.amalto.core.objects.datamodel.ejb.DataModelPOJOPK;
import com.amalto.core.objects.menu.ejb.MenuEntryPOJO;
import com.amalto.core.objects.menu.ejb.MenuPOJO;
import com.amalto.core.objects.menu.ejb.MenuPOJOPK;
import com.amalto.core.objects.menu.ejb.local.MenuCtrlLocal;
import com.amalto.core.objects.routing.v2.ejb.AbstractRoutingOrderV2POJO;
import com.amalto.core.objects.routing.v2.ejb.AbstractRoutingOrderV2POJOPK;
import com.amalto.core.objects.routing.v2.ejb.ActiveRoutingOrderV2POJO;
import com.amalto.core.objects.routing.v2.ejb.ActiveRoutingOrderV2POJOPK;
import com.amalto.core.objects.routing.v2.ejb.CompletedRoutingOrderV2POJO;
import com.amalto.core.objects.routing.v2.ejb.CompletedRoutingOrderV2POJOPK;
import com.amalto.core.objects.routing.v2.ejb.FailedRoutingOrderV2POJO;
import com.amalto.core.objects.routing.v2.ejb.FailedRoutingOrderV2POJOPK;
import com.amalto.core.objects.routing.v2.ejb.RoutingEngineV2POJO;
import com.amalto.core.objects.routing.v2.ejb.RoutingRuleExpressionPOJO;
import com.amalto.core.objects.routing.v2.ejb.RoutingRulePOJO;
import com.amalto.core.objects.routing.v2.ejb.RoutingRulePOJOPK;
import com.amalto.core.objects.routing.v2.ejb.local.RoutingEngineV2CtrlLocal;
import com.amalto.core.objects.routing.v2.ejb.local.RoutingOrderV2CtrlLocal;
import com.amalto.core.objects.routing.v2.ejb.local.RoutingRuleCtrlLocal;
import com.amalto.core.objects.storedprocedure.ejb.StoredProcedurePOJO;
import com.amalto.core.objects.storedprocedure.ejb.StoredProcedurePOJOPK;
import com.amalto.core.objects.storedprocedure.ejb.local.StoredProcedureCtrlLocal;
import com.amalto.core.objects.transformers.v2.ejb.TransformerV2POJO;
import com.amalto.core.objects.transformers.v2.ejb.TransformerV2POJOPK;
import com.amalto.core.objects.transformers.v2.ejb.local.TransformerV2CtrlLocal;
import com.amalto.core.objects.transformers.v2.util.TransformerCallBack;
import com.amalto.core.objects.transformers.v2.util.TransformerContext;
import com.amalto.core.objects.transformers.v2.util.TransformerPluginVariableDescriptor;
import com.amalto.core.objects.transformers.v2.util.TransformerProcessStep;
import com.amalto.core.objects.transformers.v2.util.TransformerVariablesMapping;
import com.amalto.core.objects.transformers.v2.util.TypedContent;
import com.amalto.core.objects.universe.ejb.UniversePOJO;
import com.amalto.core.objects.view.ejb.ViewPOJO;
import com.amalto.core.objects.view.ejb.ViewPOJOPK;
import com.amalto.core.util.ArrayListHolder;
import com.amalto.core.util.LocalUser;
import com.amalto.core.util.TransformerPluginContext;
import com.amalto.core.util.TransformerPluginSpec;
import com.amalto.core.util.UpdateReportItem;
import com.amalto.core.util.Util;
import com.amalto.core.util.Version;
import com.amalto.core.util.XSDKey;
import com.amalto.core.util.XtentisException;
import com.amalto.core.webservice.*;
import com.amalto.xmlserver.interfaces.IWhereItem;
import com.amalto.xmlserver.interfaces.WhereAnd;
import com.amalto.xmlserver.interfaces.WhereCondition;
import com.amalto.xmlserver.interfaces.WhereOr;


/**
 * @author Bruno Grieder
 * 
 * @ejb.bean name="XtentisWS"   
 * 					display-name="The Xtentis"
 * 					description="The Xtentis WebServices"
 *					jndi-name="amalto/ws/xtentis" 
 * 					type="Stateless"
 *                  view-type="service-endpoint"
 *
 * @jboss.port-component 
 * 		auth-method = "BASIC" 
 * 		name = "XtentisPort" 
 * 		uri = "/talend/TalendPort"
 *                   
 *  @wsee.port-component 
 * 					description = "The Xtentis Port" 
 * 					display-name ="XtentisPort" 
 * 					name = "XtentisPort"				
 * 
 * 
 * Not generated by xdoclet:just an indication for the deployment descriptor but
 * we use the one generated by jwsdp
 * @ejb.interface service-endpoint-class =
 *                          "com.amalto.core.webservice.XtentisPort"
 * 
 */
@SuppressWarnings({"deprecation", "unchecked"})
public class XtentisWSBean implements SessionBean, XtentisPort {


	/**
	 *  
	 */
	public XtentisWSBean() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.ejb.essionBean#ejbRemove()
	 */
	public void ejbRemove() throws EJBException, RemoteException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.ejb.SessionBean#ejbActivate()
	 */
	public void ejbActivate() throws EJBException, RemoteException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.ejb.SessionBean#ejbPassivate()
	 */
	public void ejbPassivate() throws EJBException, RemoteException {
	}

	/**
	 * Default create method
	 * 
	 * @throws CreateException
	 * @ejb.create-method
	 */
	public void ejbCreate() throws CreateException {
	}

	/***************************************************************************
	 * 
	 * S E R V I C E S
	 *  
	 *	 **************************************************************************/

	/***************************************************************************
	 * Components Management
	 * **************************************************************************/
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSVersion getComponentVersion(WSGetComponentVersion wsGetComponentVersion) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getComponentVersion(wsGetComponentVersion);
	}

	
	/***************************************************************************
	 * Ping
	 * **************************************************************************/

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSString ping(WSPing wsPing) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().ping(wsPing);
	}

	/***************************************************************************
	 * Logout
	 * **************************************************************************/

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSString logout(WSLogout logout) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().logout(logout);
	}
	

	
	/***************************************************************************
	 * Initialize
	 * **************************************************************************/
	

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSInt initMDM(WSInitData initData) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().initMDM(initData);
	}
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSMDMConfig getMDMConfiguration() throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getMDMConfiguration();
	}
	/***************************************************************************
	 * Data Model
	 * **************************************************************************/
    
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
    public WSDataModel getDataModel(WSGetDataModel wsDataModelget)
    throws RemoteException {
    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getDataModel(wsDataModelget);
    }
    
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
    public WSBoolean existsDataModel(WSExistsDataModel wsExistsDataModel)
    throws RemoteException {
    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().existsDataModel(wsExistsDataModel);
    }
    
    
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 * 	
	 */       
    public WSDataModelPKArray getDataModelPKs(WSRegexDataModelPKs regexp)
    throws RemoteException {
    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getDataModelPKs(regexp);
    }
    
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
    public WSDataModelPK deleteDataModel(WSDeleteDataModel wsDeleteDataModel)
    throws RemoteException {
    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().deleteDataModel(wsDeleteDataModel);
    }	
    
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * 
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
    public WSDataModelPK putDataModel(WSPutDataModel wsDataModel)
    throws RemoteException {
    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().putDataModel(wsDataModel);
    }
 
    
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * 
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */    
	public WSString checkSchema(WSCheckSchema wsSchema) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().checkSchema(wsSchema);
	}
	
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * 
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
    public WSString putBusinessConcept(WSPutBusinessConcept wsPutBusinessConcept)
    throws RemoteException {
    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().putBusinessConcept(wsPutBusinessConcept);
	}
     
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * 
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
    public WSString putBusinessConceptSchema(WSPutBusinessConceptSchema wsPutBusinessConceptSchema)
    throws RemoteException {
    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().putBusinessConceptSchema(wsPutBusinessConceptSchema);
	}
    
	    
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */ 
    public WSString deleteBusinessConcept(
            WSDeleteBusinessConcept wsDeleteBusinessConcept)
            throws RemoteException {
    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().deleteBusinessConcept(wsDeleteBusinessConcept);
    }
    
    
    
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */   
    public WSStringArray getBusinessConcepts(
            WSGetBusinessConcepts wsGetBusinessConcepts) throws RemoteException {
    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getBusinessConcepts(wsGetBusinessConcepts);
    }
    
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */   
    public WSConceptKey getBusinessConceptKey(
            WSGetBusinessConceptKey wsGetBusinessConceptKey) throws RemoteException {
    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getBusinessConceptKey(wsGetBusinessConceptKey);
    }
	
	/***************************************************************************
	 * DataCluster
	 * **************************************************************************/
    
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */	
	   public WSDataCluster getDataCluster(WSGetDataCluster wsDataClusterGet)
	    throws RemoteException {
		   return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getDataCluster(wsDataClusterGet);
	    }
	    
		/**
		 * @ejb.interface-method view-type = "service-endpoint"
		 * @ejb.permission 
		 * 	role-name = "authenticated"
		 * 	view-type = "service-endpoint"
		 */	
		   public WSBoolean existsDataCluster(WSExistsDataCluster wsExistsDataCluster)
		    throws RemoteException {
			   return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().existsDataCluster(wsExistsDataCluster);
		    }
			/**
			 * @ejb.interface-method view-type = "service-endpoint"
			 * @ejb.permission 
			 * 	role-name = "authenticated"
			 * 	view-type = "service-endpoint"
			 */	
			   public WSBoolean existsDBDataCluster(WSExistsDBDataCluster wsExistsDataCluster)
			    throws RemoteException {
				   return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().existsDBDataCluster(wsExistsDataCluster);
			    }


		/**
		* @ejb.interface-method view-type = "service-endpoint"
		* @ejb.permission 
		* 	role-name = "authenticated"
		* 	view-type = "service-endpoint"
		*/    
	    public WSDataClusterPKArray getDataClusterPKs(WSRegexDataClusterPKs regexp)
	    throws RemoteException {
	    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getDataClusterPKs(regexp);
	    }

	    
		/**
		 * @ejb.interface-method view-type = "service-endpoint"
		 * @ejb.permission 
		 * 	role-name = "authenticated"
		 * 	view-type = "service-endpoint"
		 */
	    public WSDataClusterPK deleteDataCluster(WSDeleteDataCluster wsDeleteDataCluster)
	    throws RemoteException {
	    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().deleteDataCluster(wsDeleteDataCluster);
	    }	
	    
		/**
		 * @ejb.interface-method view-type = "service-endpoint"
		 * @ejb.permission 
		 * 	role-name = "authenticated"
		 * 	view-type = "service-endpoint"
		 */   
	    public WSDataClusterPK putDataCluster(WSPutDataCluster wsDataCluster)
	    throws RemoteException {
	    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().putDataCluster(wsDataCluster);
	    }
		/**
		 * @ejb.interface-method view-type = "service-endpoint"
		 * @ejb.permission 
		 * 	role-name = "authenticated"
		 * 	view-type = "service-endpoint"
		 */   
	    public WSBoolean putDBDataCluster(WSPutDBDataCluster wsDataCluster)
	    throws RemoteException {
	    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().putDBDataCluster(wsDataCluster);
	    }

	    /**
		 * @ejb.interface-method view-type = "service-endpoint"
		 * @ejb.permission 
		 * 	role-name = "authenticated"
		 * 	view-type = "service-endpoint"
		 */	
		public WSStringArray getConceptsInDataCluster(WSGetConceptsInDataCluster wsGetConceptsInDataCluster) throws RemoteException {
			return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getConceptsInDataCluster(wsGetConceptsInDataCluster);
		}
		

		/**
		 * @ejb.interface-method view-type = "service-endpoint"
		 * @ejb.permission 
		 * 	role-name = "authenticated"
		 * 	view-type = "service-endpoint"
		 */	
		public WSConceptRevisionMap getConceptsInDataClusterWithRevisions(WSGetConceptsInDataClusterWithRevisions wsGetConceptsInDataClusterWithRevisions) throws RemoteException {
			return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getConceptsInDataClusterWithRevisions(wsGetConceptsInDataClusterWithRevisions);
		}

	/***************************************************************************
	 * View
	 * **************************************************************************/
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */	
   public WSView getView(WSGetView wsViewGet)
    throws RemoteException {
	   return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getView(wsViewGet);
    }

   
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */	
  public WSBoolean existsView(WSExistsView wsExistsView)
   throws RemoteException {
	  return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().existsView(wsExistsView);
   }
   

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */    
    public WSViewPKArray getViewPKs(WSGetViewPKs regexp) throws RemoteException {
    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getViewPKs(regexp);
    }
		    
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
    public WSViewPK deleteView(WSDeleteView wsDeleteView)
    throws RemoteException {
    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().deleteView(wsDeleteView);
    }	
    
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */   
    public WSViewPK putView(WSPutView wsView)
    throws RemoteException {
    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().putView(wsView);
    }

	/***************************************************************************
	 * Search
	 * **************************************************************************/
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSStringArray viewSearch(WSViewSearch wsViewSearch) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().viewSearch(wsViewSearch);
	}
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSStringArray xPathsSearch(WSXPathsSearch wsXPathsSearch) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().xPathsSearch(wsXPathsSearch);
	}

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSStringArray getItemsPivotIndex(WSGetItemsPivotIndex wsGetItemsPivotIndex) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getItemsPivotIndex(wsGetItemsPivotIndex);
	}
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSStringArray getChildrenItems(WSGetChildrenItems wsGetChildrenItems) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getChildrenItems(wsGetChildrenItems);
	}

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSString count(WSCount wsCount) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().count(wsCount);
	}

	
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */	
	public WSStringArray getItems(WSGetItems wsGetItems) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getItems(wsGetItems);
	}
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */	
	public WSItemPKsByCriteriaResponse getItemPKsByCriteria(WSGetItemPKsByCriteria wsGetItemPKsByCriteria) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getItemPKsByCriteria(wsGetItemPKsByCriteria);
	}
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */	
	public WSItemPKsByCriteriaResponse getItemPKsByFullCriteria(WSGetItemPKsByFullCriteria wsGetItemPKsByFullCriteria) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getItemPKsByFullCriteria(wsGetItemPKsByFullCriteria);
	}

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSItem getItem(WSGetItem wsGetItem) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getItem(wsGetItem);
	}	
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSBoolean existsItem(WSExistsItem wsExistsItem) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().existsItem(wsExistsItem);
	}	
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSStringArray quickSearch(WSQuickSearch wsQuickSearch) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().quickSearch(wsQuickSearch);
	}

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */		
	public WSString getBusinessConceptValue(
			WSGetBusinessConceptValue wsGetBusinessConceptValue)
			throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getBusinessConceptValue(wsGetBusinessConceptValue);
	}
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */	
	public WSStringArray getFullPathValues(WSGetFullPathValues wsGetFullPathValues)
			throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getFullPathValues(wsGetFullPathValues);
	}



	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSItemPK putItem(WSPutItem wsPutItem) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().putItem(wsPutItem);
	}	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSItemPKArray putItemArray(WSPutItemArray wsPutItemArray) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().putItemArray(wsPutItemArray);
	}
	

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSItemPKArray putItemWithReportArray(com.amalto.core.webservice.WSPutItemWithReportArray wsPutItemWithReportArray) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().putItemWithReportArray(wsPutItemWithReportArray);
	}	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSItemPK putItemWithReport(com.amalto.core.webservice.WSPutItemWithReport wsPutItemWithReport) throws RemoteException {
		
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().putItemWithReport(wsPutItemWithReport);
		
	}

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSItemPK putItemWithCustomReport(com.amalto.core.webservice.WSPutItemWithCustomReport wsPutItemWithCustomReport) throws RemoteException{
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().putItemWithCustomReport(wsPutItemWithCustomReport);
		
	}
    
	/***************************************************************************
	 *Extract Items
	 * **************************************************************************/

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSPipeline extractUsingTransformer(WSExtractUsingTransformer wsExtractUsingTransformer) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().extractUsingTransformer(wsExtractUsingTransformer);
	}

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSPipeline extractUsingTransformerThruView(WSExtractUsingTransformerThruView wsExtractUsingTransformerThruView) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().extractUsingTransformerThruView(wsExtractUsingTransformerThruView);
	}

	/***************************************************************************
	 * Delete Items
	 * **************************************************************************/
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSItemPK deleteItem(WSDeleteItem wsDeleteItem)
	throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().deleteItem(wsDeleteItem);
	}    
    
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSInt deleteItems(WSDeleteItems wsDeleteItems)
	throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().deleteItems(wsDeleteItems);
	} 
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSDroppedItemPK dropItem(WSDropItem wsDropItem)
		throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().dropItem(wsDropItem);
	}
	
	
	/***************************************************************************
	 * DirectQuery
	 * **************************************************************************/
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "administration, DataManagerAdministration"
	 * 	view-type = "service-endpoint"
	 */
	public WSStringArray runQuery(WSRunQuery wsRunQuery) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().runQuery(wsRunQuery);
	}    
	
	

	/***************************************************************************
	 * SERVICES
	 * **************************************************************************/
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSServiceGetDocument getServiceDocument(WSString serviceName) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getServiceDocument(serviceName);
	}

	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSString getServiceConfiguration(WSServiceGetConfiguration wsGetConfiguration) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getServiceConfiguration(wsGetConfiguration);
	}
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSCheckServiceConfigResponse checkServiceConfiguration(WSCheckServiceConfigRequest serviceName) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().checkServiceConfiguration(serviceName);
	}
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSString putServiceConfiguration(WSServicePutConfiguration wsPutConfiguration) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().putServiceConfiguration(wsPutConfiguration);
	}

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSString serviceAction(WSServiceAction wsServiceAction) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().serviceAction(wsServiceAction);
	}
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 	 
	 *  	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSServicesList getServicesList(WSGetServicesList wsGetServicesList) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getServicesList(wsGetServicesList);
	}
	
	
	
	/***************************************************************************
	 * Ping - test that we can authenticate by getting a server response
	 * **************************************************************************/
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSString ping()	throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().ping();
	}    
	
    	

	/***************************************************************************
	 * Xtentis JCA Connector support
	 * **************************************************************************/

	private transient ConnectionFactory cxFactory = null;
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSConnectorInteractionResponse connectorInteraction(WSConnectorInteraction wsConnectorInteraction) throws RemoteException {
		// This one does not call an EJB
		
		WSConnectorInteractionResponse response = new WSConnectorInteractionResponse();
		Connection conx = null;
		try {

			String JNDIName = wsConnectorInteraction.getJNDIName();
			conx = getConnection(JNDIName);
			
			Interaction interaction = conx.createInteraction();
	    	InteractionSpecImpl interactionSpec = new InteractionSpecImpl();
	    	
			MappedRecord recordIn = new RecordFactoryImpl().createMappedRecord(RecordFactoryImpl.RECORD_IN);
			
			WSConnectorFunction cf = wsConnectorInteraction.getFunction();
			if ((WSConnectorFunction.GET_STATUS).equals(cf)) {
				interactionSpec.setFunctionName(InteractionSpecImpl.FUNCTION_GET_STATUS);
			} else 	if ((WSConnectorFunction.PULL).equals(cf)) {
				interactionSpec.setFunctionName(InteractionSpecImpl.FUNCTION_PULL);
			} else 	if ((WSConnectorFunction.PUSH).equals(cf)) {
				interactionSpec.setFunctionName(InteractionSpecImpl.FUNCTION_PUSH);
			} else 	if ((WSConnectorFunction.START).equals(cf)) {
				interactionSpec.setFunctionName(InteractionSpecImpl.FUNCTION_START);
			} else 	if ((WSConnectorFunction.STOP).equals(cf)) {
				interactionSpec.setFunctionName(InteractionSpecImpl.FUNCTION_STOP);
			}
			
			recordIn.put(RecordFactoryImpl.PARAMS_HASHMAP_IN, getMapFromKeyValues(wsConnectorInteraction.getParameters()));
						
			MappedRecord recordOut = (MappedRecord)interaction.execute(interactionSpec, recordIn);

			String code = (String)recordOut.get(RecordFactoryImpl.STATUS_CODE_OUT);
			HashMap map = (HashMap)recordOut.get(RecordFactoryImpl.PARAMS_HASHMAP_OUT);
			
			if ("OK".equals(code)) {
				response.setCode(WSConnectorResponseCode.OK);
			} else if ("STOPPED".equals(code)) {
				response.setCode(WSConnectorResponseCode.STOPPED);
			} else if ("ERROR".equals(code)) {
				response.setCode(WSConnectorResponseCode.ERROR);
			} else {
				throw new RemoteException("Unknown code: "+code);
			}
			response.setParameters(getKeyValuesFromMap(map));
			
		} catch (ResourceException e) {
			throw new RemoteException(e.getLocalizedMessage());
		} catch (Exception e) {
			throw new RemoteException(e.getClass().getName()+": "+e.getLocalizedMessage());
		} finally {
			try {conx.close();} catch (Exception cx) {
				org.apache.log4j.Category.getInstance(this.getClass()).debug("connectorInteraction() Connection close exception: "+cx.getLocalizedMessage());
			}
		}
		return response;		
		
	}

    private Connection getConnection(String JNDIName) throws RemoteException {
    	try {
    		if (cxFactory == null)
    			cxFactory = (ConnectionFactory)(new InitialContext()).lookup(JNDIName);
	    	return cxFactory.getConnection();
    	} catch (Exception e) {
    		throw new RemoteException(e.getClass().getName()+": "+e.getLocalizedMessage());
    	}
    }
    
	private HashMap getMapFromKeyValues(WSBase64KeyValue[] params) throws RemoteException{
		try {
	    	HashMap map = new HashMap();
	    	if (params != null) {
				for (int i = 0; i < params.length; i++) {
					if (params[i]!=null) {
						String key = params[i].getKey();
						byte[] bytes = (new BASE64Decoder()).decodeBuffer(params[i].getBase64StringValue());
						if (bytes!=null) {
							ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
							ObjectInputStream ois = new ObjectInputStream(bais);
							map.put(key, ois.readObject());
						} else {
							map.put(key, null);
						}
					}
				}
	    	}
			return map;
		} catch (Exception e) {
			throw new RemoteException(e.getClass().getName()+": "+e.getLocalizedMessage());
		}    	
    }
    

	private WSBase64KeyValue[] getKeyValuesFromMap(HashMap params) throws RemoteException{    	
    	try {
    		if (params==null) return null;
    		WSBase64KeyValue[] keyValues = new WSBase64KeyValue[params.size()];
    		Set keys = params.keySet();
    		int i=0;
    		for (Iterator iter = keys.iterator(); iter.hasNext(); ) {
				String key = (String) iter.next();
				Object value = params.get(key);
				if (value!=null) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(baos);
					oos.writeObject(value);
					String base64Value = Base64Encoder.encode(baos.toByteArray());
					keyValues[i] = new WSBase64KeyValue();
					keyValues[i].setKey(key);
					keyValues[i].setBase64StringValue(base64Value);
					i++;
				}
			}
			return keyValues;
		} catch (Exception e) {
			throw new RemoteException(e.getClass().getName()+": "+e.getLocalizedMessage());
		}    	
    }
    
    

	/***************************************************************************
	 * Stored Procedure
	 * **************************************************************************/
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
    public WSStoredProcedurePK deleteStoredProcedure(WSDeleteStoredProcedure wsStoredProcedureDelete) throws RemoteException {
    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().deleteStoredProcedure(wsStoredProcedureDelete);
	}
    
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSStringArray executeStoredProcedure(WSExecuteStoredProcedure wsExecuteStoredProcedure) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().executeStoredProcedure(wsExecuteStoredProcedure);
	}

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSStoredProcedure getStoredProcedure(WSGetStoredProcedure wsGetStoredProcedure) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getStoredProcedure(wsGetStoredProcedure);
	}
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSBoolean existsStoredProcedure(WSExistsStoredProcedure wsExistsStoredProcedure) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().existsStoredProcedure(wsExistsStoredProcedure);
	}

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSStoredProcedurePKArray getStoredProcedurePKs(WSRegexStoredProcedure regex) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getStoredProcedurePKs(regex);
	}

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSStoredProcedurePK putStoredProcedure(WSPutStoredProcedure wsStoredProcedure) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().putStoredProcedure(wsStoredProcedure);
	}
 	
	/***************************************************************************
	 * Menu
	 * **************************************************************************/
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
    public WSMenuPK deleteMenu(WSDeleteMenu wsMenuDelete) throws RemoteException {
    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().deleteMenu(wsMenuDelete);
	}
 	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSMenu getMenu(WSGetMenu wsGetMenu) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getMenu(wsGetMenu);
	}


	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSBoolean existsMenu(WSExistsMenu wsExistsMenu) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().existsMenu(wsExistsMenu);
	}
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSMenuPKArray getMenuPKs(WSGetMenuPKs regex) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getMenuPKs(regex);
	}

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSMenuPK putMenu(WSPutMenu wsMenu) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().putMenu(wsMenu);
	}


	/***************************************************************************
	 * BackgroundJob
	 * **************************************************************************/
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 */    
	/*
	public WSBackgroundJobPK deleteBackgroundJob(WSBackgroundJobDelete wsjobpk)
			throws RemoteException {
		try {
			BackgroundJobPK cpk = XtentisUtil.getLocalHome().create().deleteBackgroundJob(new BackgroundJobPK(wsjobpk.getPk()));
			WSBackgroundJobPK wspk = new WSBackgroundJobPK();
			wspk.setPk(cpk.getId());
			return wspk;
		} catch (Exception e) {
			throw new EJBException((e.getCause() == null ? e.getLocalizedMessage() : e.getCause().getLocalizedMessage()));
		}
	}
	*/

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */	
   public WSBackgroundJob getBackgroundJob(WSGetBackgroundJob wsBackgroundJobGet)
    throws RemoteException {
	   return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getBackgroundJob(wsBackgroundJobGet);
    }
	    
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */    
    public WSBackgroundJobPKArray findBackgroundJobPKs(WSFindBackgroundJobPKs wsFindBackgroundJobPKs)
    throws RemoteException {
    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().findBackgroundJobPKs(wsFindBackgroundJobPKs);
    }

    
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */    
	public WSBackgroundJobPK putBackgroundJob(WSPutBackgroundJob wsputjob)
			throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().putBackgroundJob(wsputjob);
	}

	/***************************************************************************
	 * Universe
	 * **************************************************************************/
		
    /**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
     */	
	public WSUniverse getCurrentUniverse(WSGetCurrentUniverse wsGetCurrentUniverse) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getCurrentUniverse(wsGetCurrentUniverse);
	}


	/***************************************************************************
	 * 
	 * 
	 *   D E P R E C A T E D    S T U F F
	 * 
	 * 
	 * **************************************************************************/
	
	
	
	/***************************************************************************
	 * Transformer DEPRECATED
	 * **************************************************************************/
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
    public WSTransformerPK deleteTransformer(WSDeleteTransformer wsTransformerDelete) throws RemoteException {
    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().deleteTransformer(wsTransformerDelete);
	}
    


	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSTransformer getTransformer(WSGetTransformer wsGetTransformer) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getTransformer(wsGetTransformer);
	}
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSBoolean existsTransformer(WSExistsTransformer wsExistsTransformer) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().existsTransformer(wsExistsTransformer);
	}

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSTransformerPKArray getTransformerPKs(WSGetTransformerPKs regex) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getTransformerPKs(regex);
	}

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSTransformerPK putTransformer(WSPutTransformer wsTransformer) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().putTransformer(wsTransformer);
	}
    

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
    public WSPipeline processBytesUsingTransformer(WSProcessBytesUsingTransformer wsProjectBytes) throws RemoteException {
    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().processBytesUsingTransformer(wsProjectBytes);
	}

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSPipeline processFileUsingTransformer(WSProcessFileUsingTransformer wsProcessFile) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().processFileUsingTransformer(wsProcessFile);
	}
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSBackgroundJobPK processBytesUsingTransformerAsBackgroundJob(WSProcessBytesUsingTransformerAsBackgroundJob wsProcessBytesUsingTransformerAsBackgroundJob) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().processBytesUsingTransformerAsBackgroundJob(wsProcessBytesUsingTransformerAsBackgroundJob);
	}
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSBackgroundJobPK processFileUsingTransformerAsBackgroundJob(WSProcessFileUsingTransformerAsBackgroundJob wsProcessFileUsingTransformerAsBackgroundJob) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().processFileUsingTransformerAsBackgroundJob(wsProcessFileUsingTransformerAsBackgroundJob);
	}

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSDroppedItemPKArray findAllDroppedItemsPKs(WSFindAllDroppedItemsPKs regex)
			throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().findAllDroppedItemsPKs(regex);
	}
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSDroppedItem loadDroppedItem(WSLoadDroppedItem wsLoadDroppedItem)
			throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().loadDroppedItem(wsLoadDroppedItem);
	}

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSItemPK recoverDroppedItem(WSRecoverDroppedItem wsRecoverDroppedItem)
			throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().recoverDroppedItem(wsRecoverDroppedItem);
	}

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSDroppedItemPK removeDroppedItem(WSRemoveDroppedItem wsRemoveDroppedItem)
			throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().removeDroppedItem(wsRemoveDroppedItem);
	}

	/***************************************************************************
	 * RoutingRule
	 * **************************************************************************/
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */	
   public WSRoutingRule getRoutingRule(WSGetRoutingRule wsRoutingRuleGet)
    throws RemoteException {
	   return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getRoutingRule(wsRoutingRuleGet);
    }
  
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */	
	  public WSBoolean existsRoutingRule(WSExistsRoutingRule wsExistsRoutingRule)
	   throws RemoteException {
		  return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().existsRoutingRule(wsExistsRoutingRule);
	   }	    

		    
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
    public WSRoutingRulePK deleteRoutingRule(WSDeleteRoutingRule wsDeleteRoutingRule)
    throws RemoteException {
    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().deleteRoutingRule(wsDeleteRoutingRule);
    }	
    
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */   
    public WSRoutingRulePK putRoutingRule(WSPutRoutingRule wsRoutingRule)
    throws RemoteException {
    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().putRoutingRule(wsRoutingRule);
    }
    
    
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSRoutingRulePKArray getRoutingRulePKs(WSGetRoutingRulePKs regex) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getRoutingRulePKs(regex);
	}

	/***************************************************************************
	 * TransformerV2
	 * **************************************************************************/
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
    public WSTransformerV2PK deleteTransformerV2(WSDeleteTransformerV2 wsTransformerV2Delete) throws RemoteException {
    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().deleteTransformerV2(wsTransformerV2Delete);
	}
    


	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSTransformerV2 getTransformerV2(WSGetTransformerV2 wsGetTransformerV2) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getTransformerV2(wsGetTransformerV2);
	}
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSBoolean existsTransformerV2(WSExistsTransformerV2 wsExistsTransformerV2) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().existsTransformerV2(wsExistsTransformerV2);
	}

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSTransformerV2PKArray getTransformerV2PKs(WSGetTransformerV2PKs regex) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getTransformerV2PKs(regex);
	}

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSTransformerV2PK putTransformerV2(WSPutTransformerV2 wsTransformerV2) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().putTransformerV2(wsTransformerV2);
	}
	
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSTransformerContext executeTransformerV2(WSExecuteTransformerV2 wsExecuteTransformerV2) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().executeTransformerV2(wsExecuteTransformerV2);
	}

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSBackgroundJobPK executeTransformerV2AsJob(WSExecuteTransformerV2AsJob wsExecuteTransformerV2AsJob) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().executeTransformerV2AsJob(wsExecuteTransformerV2AsJob);
	}
	
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSTransformerContext extractThroughTransformerV2(WSExtractThroughTransformerV2 wsExtractThroughTransformerV2) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().extractThroughTransformerV2(wsExtractThroughTransformerV2);
	}
	

	/***************************************************************************
	 * TRANSFORMER PLUGINS V2
	 * **************************************************************************/
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 	 
	 *  	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSBoolean existsTransformerPluginV2(WSExistsTransformerPluginV2 wsExistsTransformerPlugin) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().existsTransformerPluginV2(wsExistsTransformerPlugin);
	}
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSString getTransformerPluginV2Configuration(WSTransformerPluginV2GetConfiguration wsGetConfiguration) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getTransformerPluginV2Configuration(wsGetConfiguration);
	}

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSString putTransformerPluginV2Configuration(WSTransformerPluginV2PutConfiguration wsPutConfiguration) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().putTransformerPluginV2Configuration(wsPutConfiguration);
	}

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 	 
	 *  	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSTransformerPluginV2Details getTransformerPluginV2Details(WSGetTransformerPluginV2Details wsGetTransformerPluginDetails) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getTransformerPluginV2Details(wsGetTransformerPluginDetails);
	}


	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 	 
	 *  	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSTransformerPluginV2SList getTransformerPluginV2SList(WSGetTransformerPluginV2SList wsGetTransformerPluginsList) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getTransformerPluginV2SList(wsGetTransformerPluginsList);
	}

	/***************************************************************************
	 * Routing Order V2
	 * **************************************************************************/
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSRoutingOrderV2 getRoutingOrderV2(WSGetRoutingOrderV2 wsGetRoutingOrder) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getRoutingOrderV2(wsGetRoutingOrder);
	}
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSRoutingOrderV2 existsRoutingOrderV2(WSExistsRoutingOrderV2 wsExistsRoutingOrder) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().existsRoutingOrderV2(wsExistsRoutingOrder);
	}
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSRoutingOrderV2PK deleteRoutingOrderV2(WSDeleteRoutingOrderV2 wsDeleteRoutingOrder) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().deleteRoutingOrderV2(wsDeleteRoutingOrder);
	}
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSRoutingOrderV2PK executeRoutingOrderV2Asynchronously(WSExecuteRoutingOrderV2Asynchronously wsExecuteRoutingOrderAsynchronously) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().executeRoutingOrderV2Asynchronously(wsExecuteRoutingOrderAsynchronously);
	}
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSString executeRoutingOrderV2Synchronously(WSExecuteRoutingOrderV2Synchronously wsExecuteRoutingOrderSynchronously) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().executeRoutingOrderV2Synchronously(wsExecuteRoutingOrderSynchronously);
	}

	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSRoutingOrderV2PKArray getRoutingOrderV2PKsByCriteria(WSGetRoutingOrderV2PKsByCriteria wsGetRoutingOrderV2PKsByCriteria) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getRoutingOrderV2PKsByCriteria(wsGetRoutingOrderV2PKsByCriteria);
	}
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSRoutingOrderV2Array getRoutingOrderV2SByCriteria(WSGetRoutingOrderV2SByCriteria wsGetRoutingOrderV2SByCriteria) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getRoutingOrderV2SByCriteria(wsGetRoutingOrderV2SByCriteria);
	}
	/***************************************************************************
	 * Routing Engine V2
	 * **************************************************************************/
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSRoutingRulePKArray routeItemV2(WSRouteItemV2 wsRouteItem) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().routeItemV2(wsRouteItem);
	}
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
	public WSRoutingEngineV2Status routingEngineV2Action(WSRoutingEngineV2Action wsRoutingEngineAction) throws RemoteException {
		return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().routingEngineV2Action(wsRoutingEngineAction);

	}
	
	/**
	 * @ejb.interface-method view-type = "service-endpoint"
	 * @ejb.permission 
	 * 	role-name = "authenticated"
	 * 	view-type = "service-endpoint"
	 */
    public WSCategoryData getMDMCategory(WSCategoryData request) throws RemoteException {
    	return BeanDelegatorContainer.getUniqueInstance().getXtentisWSDelegator().getMDMCategory(request);
    }
}
