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

package com.amalto.core.metadata;

import org.apache.ws.commons.schema.XmlSchemaAnnotation;
import org.apache.ws.commons.schema.XmlSchemaAppInfo;

import java.util.*;

class ForeignKeyProcessor implements XmlSchemaAnnotationProcessor {

    public void process(MetadataRepository repository, ComplexTypeMetadata type, XmlSchemaAnnotation annotation, XmlSchemaAnnotationProcessorState state) {
        if (annotation != null) {
            Iterator annotations = annotation.getItems().getIterator();
            while (annotations.hasNext()) {
                XmlSchemaAppInfo appInfo = (XmlSchemaAppInfo) annotations.next();
                if ("X_ForeignKey".equals(appInfo.getSource())) { //$NON-NLS-1$
                    handleForeignKey(repository, state, appInfo);
                } else if ("X_ForeignKeyInfo".equals(appInfo.getSource())) { //$NON-NLS-1$
                    handleForeignKeyInfo(repository, type, state, appInfo);
                } else if ("X_FKIntegrity".equals(appInfo.getSource())) { //$NON-NLS-1$
                    state.setFkIntegrity(Boolean.valueOf(appInfo.getMarkup().item(0).getTextContent()));
                } else if ("X_FKIntegrity_Override".equals(appInfo.getSource())) { //$NON-NLS-1$
                    state.setFkIntegrityOverride(Boolean.valueOf(appInfo.getMarkup().item(0).getTextContent()));
                }
            }
        }
    }

    private void handleForeignKeyInfo(MetadataRepository repository, ComplexTypeMetadata type, XmlSchemaAnnotationProcessorState state, XmlSchemaAppInfo appInfo) {
        String path = appInfo.getMarkup().item(0).getTextContent();
        String[] typeAndFields = path.split("/"); //$NON-NLS-1$
        String typeName;
        if (typeAndFields[0].equals(".")) { //$NON-NLS-1$
            typeName = type.getName();
        } else {
            typeName = typeAndFields[0].trim();
        }

        List<String> fieldList = Arrays.asList(typeAndFields);
        FieldMetadata fieldMetadata = createFieldReference(repository, new SoftTypeRef(repository, repository.getUserNamespace(), typeName), fieldList);
        if (fieldMetadata == null) {
            throw new IllegalArgumentException("Path '" + path + "' is not supported.");
        }
        state.setForeignKeyInfo(fieldMetadata);
    }

    private void handleForeignKey(MetadataRepository repository, XmlSchemaAnnotationProcessorState state, XmlSchemaAppInfo appInfo) {
        state.setReference(true);
        String path = appInfo.getMarkup().item(0).getTextContent();
        String[] typeAndFields = path.split("/"); //$NON-NLS-1$
        String userNamespace = repository.getUserNamespace();
        state.setFieldType(new SoftTypeRef(repository, userNamespace, typeAndFields[0].trim()));
        state.setReferencedType(new SoftTypeRef(repository, userNamespace, typeAndFields[0].trim()));

        List<String> fieldList = Arrays.asList(typeAndFields);
        FieldMetadata fieldMetadata = createFieldReference(repository, state.getFieldType(), fieldList);
        if (fieldMetadata == null) {
            throw new IllegalArgumentException("Path '" + path + "' is not supported.");
        }
        state.setReferencedField(fieldMetadata);
    }

    private static FieldMetadata createFieldReference(MetadataRepository repository, TypeMetadata rootTypeName, List<String> path) {
        Queue<String> processQueue = new LinkedList<String>(path);
        processQueue.poll(); // Remove first (first is type name and required additional processing for '.')
        SoftTypeRef currentType = new SoftTypeRef(repository, rootTypeName.getNamespace(), rootTypeName.getName());
        if (processQueue.isEmpty()) {
            // handle case where referenced type "Type" only and not "Type/Id" (way to reference composite id).
            return new SoftIdFieldRef(repository, rootTypeName.getName());
        }

        SoftFieldRef fieldMetadata = null;
        while (!processQueue.isEmpty()) {
            if (fieldMetadata == null) {
                fieldMetadata = new SoftFieldRef(repository, processQueue.poll().trim(), currentType);
            } else {
                fieldMetadata = new SoftFieldRef(repository, processQueue.poll().trim(), fieldMetadata);
            }
        }
        return fieldMetadata;
    }
}
