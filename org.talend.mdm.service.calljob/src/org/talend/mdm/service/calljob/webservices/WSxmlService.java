// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.mdm.service.calljob.webservices;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.spi.ServiceDelegate;

/**
 * This class was generated by the JAX-WS RI. JAX-WS RI 2.1.1-b03- Generated source version: 2.0
 * 
 */
@WebServiceClient(name = "WSxmlService", targetNamespace = "http://talend.org", wsdlLocation = "http://localhost:8083/WSxml_0.1/services/WSxml?WSDL")
public class WSxmlService extends Service {

    private final static URL WSXMLSERVICE_WSDL_LOCATION;

    private ServiceDelegate delegateInUse;

    static {
        URL url = null;
        try {
            url = new URL("http://localhost:8083/WSxml_0.1/services/WSxml?WSDL");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        WSXMLSERVICE_WSDL_LOCATION = url;
    }

    public WSxmlService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);

        delegateInUse = new org.jboss.ws.core.jaxws.spi.ProviderImpl().createServiceDelegate(wsdlLocation, serviceName,
                this.getClass());
    }

    public WSxmlService() {
        super(WSXMLSERVICE_WSDL_LOCATION, new QName("http://talend.org", "WSxmlService"));

        delegateInUse = new org.jboss.ws.core.jaxws.spi.ProviderImpl().createServiceDelegate(WSXMLSERVICE_WSDL_LOCATION,
                new QName("http://talend.org", "WSxmlService"), this.getClass());
    }

    /**
     * 
     * @return returns WSxml
     */
    @WebEndpoint(name = "WSxml")
    public WSxml getWSxml() {
        return delegateInUse.getPort(new QName("http://talend.org", "WSxml"), WSxml.class);
    }

}
