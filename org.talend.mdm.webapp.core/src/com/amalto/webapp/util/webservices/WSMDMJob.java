// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation 
// Generated source version: 1.1.2

package com.amalto.webapp.util.webservices;


public class WSMDMJob {
    protected java.lang.String jobName;
    protected java.lang.String jobVersion;
    protected java.lang.String suffix;
    
    public WSMDMJob() {
    }
    
    public WSMDMJob(java.lang.String jobName, java.lang.String jobVersion, java.lang.String suffix) {
        this.jobName = jobName;
        this.jobVersion = jobVersion;
        this.suffix = suffix;
    }
    
    public java.lang.String getJobName() {
        return jobName;
    }
    
    public void setJobName(java.lang.String jobName) {
        this.jobName = jobName;
    }
    
    public java.lang.String getJobVersion() {
        return jobVersion;
    }
    
    public void setJobVersion(java.lang.String jobVersion) {
        this.jobVersion = jobVersion;
    }
    
    public java.lang.String getSuffix() {
        return suffix;
    }
    
    public void setSuffix(java.lang.String suffix) {
        this.suffix = suffix;
    }
}