// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation 
// Generated source version: 1.1.2

package com.amalto.webapp.util.webservices;


public class WSGetView {
    protected com.amalto.webapp.util.webservices.WSViewPK wsViewPK;
    
    public WSGetView() {
    }
    
    public WSGetView(com.amalto.webapp.util.webservices.WSViewPK wsViewPK) {
        this.wsViewPK = wsViewPK;
    }
    
    public com.amalto.webapp.util.webservices.WSViewPK getWsViewPK() {
        return wsViewPK;
    }
    
    public void setWsViewPK(com.amalto.webapp.util.webservices.WSViewPK wsViewPK) {
        this.wsViewPK = wsViewPK;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj))
            return true;
        if (obj == null)
            return false;
        if (obj instanceof WSGetView) {
            WSGetView another = (WSGetView) obj;
            if (wsViewPK != null) {
                return wsViewPK.equals(another.getWsViewPK());
            } else {
                return wsViewPK == another.getWsViewPK();
            }
        } else {
            return false;
        }
    }
}
