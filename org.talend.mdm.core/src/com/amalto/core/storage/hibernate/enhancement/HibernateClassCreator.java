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

package com.amalto.core.storage.hibernate.enhancement;

import com.amalto.core.metadata.*;
import com.amalto.core.storage.hibernate.CompositeIdBridge;
import com.amalto.core.storage.hibernate.HibernateClassWrapper;
import com.amalto.core.storage.hibernate.StorageClassLoader;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.AnnotationMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import org.hibernate.search.annotations.*;

import java.io.Serializable;
import java.util.*;

// TODO Refactor
public class HibernateClassCreator extends DefaultMetadataVisitor<Void> {

    public static final String PACKAGE_PREFIX = "org.talend.mdm.storage.hibernate.";

    private final StorageClassLoader storageClassLoader;

    private final Stack<CtClass> classCreationStack = new Stack<CtClass>();

    private final Set<String> processedTypes = new HashSet<String>();

    private final LinkedList<String> fieldPrefix = new LinkedList<String>();

    private final Set<CtClass> classIndexed = new HashSet<CtClass>();

    private final ClassPool classPool;

    private CtClass listType;

    public HibernateClassCreator(StorageClassLoader storageClassLoader) {
        this.storageClassLoader = storageClassLoader;
        // Use a new ClassPool to prevent storing classes in default class pool.
        this.classPool = new ClassPool(ClassPool.getDefault());
        try {
            listType = classPool.get(List.class.getName());
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Void visit(MetadataRepository repository) {
        try {
            super.visit(repository);
            return null;
        } finally {
            // Clean up processed types.
            processedTypes.clear();
        }
    }

    @Override
    public Void visit(ContainedComplexTypeMetadata containedType) {
        fieldPrefix.add(containedType.getName());
        List<FieldMetadata> fields = containedType.getFields();
        for (FieldMetadata field : fields) {
            field.accept(this);
        }
        fieldPrefix.removeLast();
        return null;
    }

    @Override
    public Void visit(ComplexTypeMetadata complexType) {
        // Prevent infinite loop.
        String typeName = complexType.getName();
        if (processedTypes.contains(typeName)) {
            return null;
        } else {
            processedTypes.add(typeName);
        }

        try {
            CtClass newClass = classPool.makeClass(PACKAGE_PREFIX + typeName);
            CtClass hibernateClassWrapper = classPool.get(HibernateClassWrapper.class.getName());
            CtClass serializable = classPool.get(Serializable.class.getName());
            newClass.setInterfaces(new CtClass[]{hibernateClassWrapper, serializable});
            ClassFile classFile = newClass.getClassFile();
            newClass.setModifiers(Modifier.PUBLIC);

            // Mark new class as indexed for Hibernate search (full text) extensions.
            ConstPool cp = classFile.getConstPool();
            AnnotationsAttribute annotationsAttribute = new AnnotationsAttribute(cp, AnnotationsAttribute.visibleTag);
            Annotation indexedAnnotation = new Annotation(Indexed.class.getName(), cp);
            annotationsAttribute.setAnnotation(indexedAnnotation);
            classFile.addAttribute(annotationsAttribute);

            List<FieldMetadata> keyFields = complexType.getKeyFields();
            // Composite id class.
            if (keyFields.size() > 1) {
                String idClassName = PACKAGE_PREFIX + typeName + "_ID"; //$NON-NLS-1$
                CtClass newIdClass = classPool.makeClass(idClassName);
                newIdClass.setInterfaces(new CtClass[]{serializable});

                classCreationStack.push(newIdClass);
                {
                    for (FieldMetadata keyField : keyFields) {
                        keyField.accept(this);
                    }
                }

                StringBuilder initConstructorBody = new StringBuilder();
                initConstructorBody.append(typeName).append("_ID").append('('); //$NON-NLS-1$
                Iterator<FieldMetadata> keyFieldIterator = keyFields.iterator();
                while (keyFieldIterator.hasNext()) {
                    FieldMetadata currentKeyField = keyFieldIterator.next();
                    initConstructorBody.append(getFieldType(currentKeyField)).append(' ').append(currentKeyField.getName());
                    if (keyFieldIterator.hasNext()) {
                        initConstructorBody.append(", "); //$NON-NLS-1$
                    }
                }
                initConstructorBody.append(')');
                initConstructorBody.append('{');
                for (FieldMetadata keyField : keyFields) {
                    initConstructorBody.append("this.").append(keyField.getName()).append('=').append(keyField.getName()).append(";\n"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                initConstructorBody.append('}');
                CtConstructor initConstructor = CtNewConstructor.make(initConstructorBody.toString(), newIdClass);
                initConstructor.setModifiers(Modifier.PUBLIC);

                newIdClass.addConstructor(CtNewConstructor.defaultConstructor(newIdClass));
                newIdClass.addConstructor(initConstructor);

                StringBuilder toStringBody = new StringBuilder();
                toStringBody.append("public String toString() {\n"); //$NON-NLS-1$
                toStringBody.append("return "); //$NON-NLS-1$
                keyFieldIterator = keyFields.iterator();
                while (keyFieldIterator.hasNext()) {
                    toStringBody.append("\'[\' + this.").append(keyFieldIterator.next().getName()).append(" + \']\'"); //$NON-NLS-1$ //$NON-NLS-2$
                    if (keyFieldIterator.hasNext()) {
                        toStringBody.append(" + "); //$NON-NLS-1$
                    }
                }
                toStringBody.append(";\n}"); //$NON-NLS-1$
                newIdClass.addMethod(CtNewMethod.make(toStringBody.toString(), newIdClass));

                Class compiledNewClassId = classCreationStack.pop().toClass();
                storageClassLoader.register(idClassName, compiledNewClassId);
            }

            classCreationStack.push(newClass);
            {
                super.visit(complexType);
            }

            // Optimized getter
            StringBuilder getFieldsMethodBody = new StringBuilder();
            getFieldsMethodBody.append("public Object get(String name) {"); //$NON-NLS-1$
            List<FieldMetadata> typeFields = complexType.getFields();
            if (typeFields.isEmpty()) {
                throw new IllegalArgumentException("Type '" + typeName + "' does not contain any field.");
            }
            Iterator<FieldMetadata> fields = typeFields.iterator();
            while (fields.hasNext()) {
                String fieldName = fields.next().getName();
                getFieldsMethodBody.append("if(\"").append(fieldName).append("\".equals(name)) {\n"); //$NON-NLS-1$ //$NON-NLS-2$
                getFieldsMethodBody.append("\treturn get").append(fieldName).append("();\n"); //$NON-NLS-1$ //$NON-NLS-2$
                getFieldsMethodBody.append("}\n"); //$NON-NLS-1$

                if (fields.hasNext()) {
                    getFieldsMethodBody.append("else "); //$NON-NLS-1$
                }
            }
            getFieldsMethodBody.append("else { return null; }"); //$NON-NLS-1$
            getFieldsMethodBody.append("}"); //$NON-NLS-1$
            CtMethod getFieldsMethod = CtNewMethod.make(getFieldsMethodBody.toString(), newClass);
            newClass.addMethod(getFieldsMethod);

            // Optimized setter
            StringBuilder setFieldsMethodBody = new StringBuilder();
            setFieldsMethodBody.append("public void set(String name, Object value) {"); //$NON-NLS-1$
            fields = typeFields.iterator();
            while (fields.hasNext()) {
                String fieldName = fields.next().getName();
                setFieldsMethodBody.append("if(\"").append(fieldName).append("\".equals(name)) {\n"); //$NON-NLS-1$ //$NON-NLS-2$
                String setterName = "set" + fieldName; //$NON-NLS-1$
                CtMethod setterMethod = newClass.getDeclaredMethod(setterName);
                setFieldsMethodBody.append("\t").append(setterName).append("((").append(setterMethod.getParameterTypes()[0].getName()).append(") value);\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                setFieldsMethodBody.append("}\n"); //$NON-NLS-1$

                if (fields.hasNext()) {
                    setFieldsMethodBody.append("else "); //$NON-NLS-1$
                }
            }
            setFieldsMethodBody.append("}"); //$NON-NLS-1$
            CtMethod setFieldsMethod = CtNewMethod.make(setFieldsMethodBody.toString(), newClass);
            newClass.addMethod(setFieldsMethod);

            // Get time stamp method
            CtMethod getTimeStamp = createFixedFieldGetter(newClass, UserTypeMappingRepository.METADATA_TIMESTAMP, "timestamp"); //$NON-NLS-1$
            newClass.addMethod(getTimeStamp);

            // Set time stamp method
            CtMethod setTimeStamp = createFixedSetter(newClass, UserTypeMappingRepository.METADATA_TIMESTAMP, "timestamp"); //$NON-NLS-1$
            newClass.addMethod(setTimeStamp);

            // Get revision method
            CtMethod getRevision = createFixedFieldGetter(newClass, UserTypeMappingRepository.METADATA_REVISION_ID, "revision"); //$NON-NLS-1$
            newClass.addMethod(getRevision);

            // Set time stamp method
            CtMethod setRevision = createFixedSetter(newClass, UserTypeMappingRepository.METADATA_REVISION_ID, "revision"); //$NON-NLS-1$
            newClass.addMethod(setRevision);

            // Get task id method
            StringBuilder getTaskIdMethodBody = new StringBuilder();
            getTaskIdMethodBody.append("public String taskId() {"); //$NON-NLS-1$
            getTaskIdMethodBody.append("return (String) get" + UserTypeMappingRepository.METADATA_TASK_ID + "();"); //$NON-NLS-1$ //$NON-NLS-2$
            getTaskIdMethodBody.append("}"); //$NON-NLS-1$
            CtMethod getTaskId = CtNewMethod.make(getTaskIdMethodBody.toString(), newClass);
            newClass.addMethod(getTaskId);

            // Set task id method
            StringBuilder setTaskIdMethodBody = new StringBuilder();
            setTaskIdMethodBody.append("public void taskId(String value) {"); //$NON-NLS-1$
            setTaskIdMethodBody.append("set" + UserTypeMappingRepository.METADATA_TASK_ID + "(value);"); //$NON-NLS-1$ //$NON-NLS-2$
            setTaskIdMethodBody.append("}"); //$NON-NLS-1$
            CtMethod setTaskId = CtNewMethod.make(setTaskIdMethodBody.toString(), newClass);
            newClass.addMethod(setTaskId);

            Class compiledNewClass = classCreationStack.pop().toClass();
            storageClassLoader.register(complexType, compiledNewClass);
        } catch (Exception e) {
            throw new RuntimeException("Error during processing of type '" + typeName + "'", e);
        }

        return null;
    }

    private CtMethod createFixedSetter(CtClass newClass, String fieldName, String methodName) throws CannotCompileException {
        StringBuilder setTimeStampMethodBody = new StringBuilder();
        setTimeStampMethodBody.append("public void ").append(methodName).append("(long value) {"); //$NON-NLS-1$ //$NON-NLS-2$
        setTimeStampMethodBody.append("set").append(fieldName).append("(new Long(value));"); //$NON-NLS-1$ //$NON-NLS-2$
        setTimeStampMethodBody.append("}"); //$NON-NLS-1$
        return CtNewMethod.make(setTimeStampMethodBody.toString(), newClass);
    }

    private CtMethod createFixedFieldGetter(CtClass newClass, String fixedFieldName, String methodName) throws CannotCompileException {
        StringBuilder getTimeStampMethodBody = new StringBuilder();
        getTimeStampMethodBody.append("public long ").append(methodName).append("() {"); //$NON-NLS-1$ //$NON-NLS-2$
        getTimeStampMethodBody.append("Long longObject = get").append(fixedFieldName).append("();"); //$NON-NLS-1$ //$NON-NLS-2$
        getTimeStampMethodBody.append("return longObject == null ? 0 : longObject.longValue();"); //$NON-NLS-1$
        getTimeStampMethodBody.append("}"); //$NON-NLS-1$
        return CtNewMethod.make(getTimeStampMethodBody.toString(), newClass);
    }

    @Override
    public Void visit(ReferenceFieldMetadata referenceField) {
        try {
            CtClass currentClass = classCreationStack.peek();

            referenceField.getReferencedType().accept(this); // Visit referenced type in case it hasn't been created.
            CtClass fieldType = classPool.get(PACKAGE_PREFIX + referenceField.getReferencedType().getName());
            addNewField(getFieldName(referenceField), referenceField.isMany(), fieldType, currentClass);

            return super.visit(referenceField);
        } catch (Exception e) {
            throw new RuntimeException("Error during processing of reference field '" + referenceField.getName() + "' of type '" + referenceField.getContainingType().getName() + "'", e);
        }
    }

    @Override
    public Void visit(ContainedTypeFieldMetadata containedField) {
        fieldPrefix.add(containedField.getName());
        {
            TypeMetadata containedType = containedField.getContainedType();
            containedType.accept(this);
        }
        fieldPrefix.removeLast();

        return null;
    }

    @Override
    public Void visit(EnumerationFieldMetadata enumField) {
        return handleFieldMetadata(enumField);
    }

    @Override
    public Void visit(SimpleTypeFieldMetadata simpleField) {
        return handleFieldMetadata(simpleField);
    }

    private CtField addNewField(String name, boolean isMany, CtClass type, CtClass to) throws CannotCompileException {
        CtField newField;
        if (!isMany) {
            newField = new CtField(type, name, to);
        } else {
            newField = new CtField(listType, name, to);
        }

        newField.setModifiers(Modifier.PUBLIC);
        CtMethod newGetter = CtNewMethod.getter("get" + name, newField); //$NON-NLS-1$
        CtMethod newSetter = CtNewMethod.setter("set" + name, newField); //$NON-NLS-1$
        to.addMethod(newSetter);
        to.addMethod(newGetter);
        to.addField(newField);

        return newField;
    }

    private Void handleFieldMetadata(FieldMetadata metadata) {
        try {
            CtClass currentClass = classCreationStack.peek();
            ClassFile currentClassFile = currentClass.getClassFile();
            CtClass fieldType = classPool.get(getFieldType(metadata));

            CtField field = addNewField(getFieldName(metadata), metadata.isMany(), fieldType, currentClass);

            if (!currentClass.getName().endsWith("_ID")) { //$NON-NLS-1$
                ConstPool cp = currentClassFile.getConstPool();
                AnnotationsAttribute annotations = (AnnotationsAttribute) field.getFieldInfo().getAttribute(AnnotationsAttribute.visibleTag);
                if (annotations == null) {
                    annotations = new AnnotationsAttribute(cp, AnnotationsAttribute.visibleTag);
                    field.getFieldInfo().addAttribute(annotations);
                }

                // Adds "DocumentId" annotation for Hibernate search
                if (metadata.getContainingType().getKeyFields().size() == 1) {
                    if (metadata.isKey()) {
                        Annotation docIdAnnotation = new Annotation(DocumentId.class.getName(), cp);
                        annotations.addAnnotation(docIdAnnotation);
                    }
                } else {
                    if (!classIndexed.contains(currentClass)) {
                        // @ProvidedId(bridge = @FieldBridge(impl = CompositeIdBridge.class))
                        Annotation providedId = new Annotation(ProvidedId.class.getName(), cp);
                        Annotation fieldBridge = new Annotation(FieldBridge.class.getName(), cp);
                        fieldBridge.addMemberValue("impl", new ClassMemberValue(CompositeIdBridge.class.getName(), cp)); //$NON-NLS-1$
                        providedId.addMemberValue("bridge", new AnnotationMemberValue(fieldBridge, cp)); //$NON-NLS-1$

                        AnnotationsAttribute attribute = (AnnotationsAttribute) currentClassFile.getAttribute(AnnotationsAttribute.visibleTag);
                        attribute.addAnnotation(providedId);
                        classIndexed.add(currentClass);
                    }
                }
                // Mark field as value to index
                if (isIndexableField(metadata)) {
                    Annotation fieldAnnotation = new Annotation(Field.class.getName(), cp);
                    annotations.addAnnotation(fieldAnnotation);
                } else {
                    // TODO Support index on many fields / date fields / byte fields
                }
            }

            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error during class field creation for field '" + metadata.getName() + "' in type '" + metadata.getContainingType().getName() + "'", e);
        }
    }

    protected String getFieldType(FieldMetadata metadata) {
        return MetadataUtils.getJavaType(metadata.getType());
    }

    private static boolean isIndexableField(FieldMetadata metadata) {
        boolean validType = !("date".equals(metadata.getType().getName()) //$NON-NLS-1$
                || "dateTime".equals(metadata.getType().getName()) //$NON-NLS-1$
                || "time".equals(metadata.getType().getName()) //$NON-NLS-1$
                || "duration".equals(metadata.getType().getName()) //$NON-NLS-1$
                || "byte".equals(metadata.getType().getName()) //$NON-NLS-1$
                || "unsignedByte".equals(metadata.getType().getName()));//$NON-NLS-1$
        return !metadata.isMany() && validType;
    }

    private String getFieldName(FieldMetadata metadata) {
        StringBuilder buffer = new StringBuilder();
        Iterator<String> prefixIterator = fieldPrefix.iterator();
        while (prefixIterator.hasNext()) {
            buffer.append(prefixIterator.next());
            if (prefixIterator.hasNext()) {
                buffer.append('_');
            }
        }
        String name;
        if (buffer.length() > 0) {
            name = buffer.toString() + '_' + metadata.getName();
        } else {
            name = metadata.getName();
        }
        return name;
    }

}
