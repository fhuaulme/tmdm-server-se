/*
 * Generated by XDoclet - Do not edit!
 */
package com.amalto.core.objects.transformers.v2.util;

/**
 * Local interface for Transformer Plugins.
 * @xdoclet-generated at 26-11-07
 * @copyright amalto
 * @author bgrieder
 * @version ${version}
 */
public interface TransformerPluginV2LocalInterface
//   extends javax.ejb.EJBLocalObject
{
   public void setGlobalContext(TransformerGlobalContext gcontext);
   public java.lang.String getJNDIName(  ) throws com.amalto.core.util.XtentisException;

   public java.lang.String getDescription( java.lang.String twoLetterLanguageCode ) throws com.amalto.core.util.XtentisException;

   public java.util.ArrayList getInputVariableDescriptors( java.lang.String twoLettersLanguageCode ) throws com.amalto.core.util.XtentisException;

   public java.util.ArrayList getOutputVariableDescriptors( java.lang.String twoLettersLanguageCode ) throws com.amalto.core.util.XtentisException;

   public void init( com.amalto.core.objects.transformers.v2.util.TransformerPluginContext context,java.lang.String compiledParameters ) throws com.amalto.core.util.XtentisException;

   public void execute( com.amalto.core.objects.transformers.v2.util.TransformerPluginContext context ) throws com.amalto.core.util.XtentisException;

   public void end( com.amalto.core.objects.transformers.v2.util.TransformerPluginContext context ) throws com.amalto.core.util.XtentisException;

   public java.lang.String getParametersSchema(  ) throws com.amalto.core.util.XtentisException;

   public java.lang.String getDocumentation( java.lang.String twoLettersLanguageCode ) throws com.amalto.core.util.XtentisException;

   public java.lang.String getConfiguration( java.lang.String optionalParameters ) throws com.amalto.core.util.XtentisException;

   public void putConfiguration( java.lang.String configuration ) throws com.amalto.core.util.XtentisException;

   public java.lang.String compileParameters( java.lang.String parameters ) throws com.amalto.core.util.XtentisException;

}
