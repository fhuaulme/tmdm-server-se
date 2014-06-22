// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation 
// Generated source version: 1.1.2

package com.amalto.webapp.util.webservices;


import java.util.Map;
import java.util.HashMap;

public class WSRoutingEngineV2ActionCode {
    private java.lang.String value;
    private static Map valueMap = new HashMap();
    public static final String _STARTString = "START";
    public static final String _STOPString = "STOP";
    public static final String _SUSPENDString = "SUSPEND";
    public static final String _RESUMEString = "RESUME";
    public static final String _STATUSString = "STATUS";
    
    public static final java.lang.String _START = new java.lang.String(_STARTString);
    public static final java.lang.String _STOP = new java.lang.String(_STOPString);
    public static final java.lang.String _SUSPEND = new java.lang.String(_SUSPENDString);
    public static final java.lang.String _RESUME = new java.lang.String(_RESUMEString);
    public static final java.lang.String _STATUS = new java.lang.String(_STATUSString);
    
    public static final WSRoutingEngineV2ActionCode START = new WSRoutingEngineV2ActionCode(_START);
    public static final WSRoutingEngineV2ActionCode STOP = new WSRoutingEngineV2ActionCode(_STOP);
    public static final WSRoutingEngineV2ActionCode SUSPEND = new WSRoutingEngineV2ActionCode(_SUSPEND);
    public static final WSRoutingEngineV2ActionCode RESUME = new WSRoutingEngineV2ActionCode(_RESUME);
    public static final WSRoutingEngineV2ActionCode STATUS = new WSRoutingEngineV2ActionCode(_STATUS);
    
    protected WSRoutingEngineV2ActionCode(java.lang.String value) {
        this.value = value;
        valueMap.put(this.toString(), this);
    }
    
    public java.lang.String getValue() {
        return value;
    }
    
    public static WSRoutingEngineV2ActionCode fromValue(java.lang.String value)
        throws java.lang.IllegalStateException {
        if (START.value.equals(value)) {
            return START;
        } else if (STOP.value.equals(value)) {
            return STOP;
        } else if (SUSPEND.value.equals(value)) {
            return SUSPEND;
        } else if (RESUME.value.equals(value)) {
            return RESUME;
        } else if (STATUS.value.equals(value)) {
            return STATUS;
        }
        throw new IllegalArgumentException();
    }
    
    public static WSRoutingEngineV2ActionCode fromString(String value)
        throws java.lang.IllegalStateException {
        WSRoutingEngineV2ActionCode ret = (WSRoutingEngineV2ActionCode)valueMap.get(value);
        if (ret != null) {
            return ret;
        }
        if (value.equals(_STARTString)) {
            return START;
        } else if (value.equals(_STOPString)) {
            return STOP;
        } else if (value.equals(_SUSPENDString)) {
            return SUSPEND;
        } else if (value.equals(_RESUMEString)) {
            return RESUME;
        } else if (value.equals(_STATUSString)) {
            return STATUS;
        }
        throw new IllegalArgumentException();
    }
    
    public String toString() {
        return value.toString();
    }
    
    private Object readResolve()
        throws java.io.ObjectStreamException {
        return fromValue(getValue());
    }
    
    public boolean equals(Object obj) {
        if (!(obj instanceof WSRoutingEngineV2ActionCode)) {
            return false;
        }
        return ((WSRoutingEngineV2ActionCode)obj).value.equals(value);
    }
    
    public int hashCode() {
        return value.hashCode();
    }
}
