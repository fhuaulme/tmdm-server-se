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

package com.amalto.core.save.context;

import com.amalto.core.history.Action;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.history.accessor.Accessor;
import com.amalto.core.history.action.FieldUpdateAction;
import com.amalto.core.metadata.ComplexTypeMetadata;
import com.amalto.core.metadata.FieldMetadata;
import com.amalto.core.metadata.MetadataRepository;
import org.apache.commons.lang.StringUtils;

import java.util.*;

class PartialUpdateActionCreator extends UpdateActionCreator {

    private final String pivot;

    private final String key;

    private final Map<FieldMetadata, Integer> originalFieldToLastIndex = new HashMap<FieldMetadata, Integer>();

    private String lastMatchPath;

    private Map<String, String> keyValueToPath = new HashMap<String, String>();

    private final Stack<String> leftPath = new Stack<String>();

    private final Stack<String> rightPath = new Stack<String>();

    public PartialUpdateActionCreator(MutableDocument originalDocument,
                                      MutableDocument newDocument,
                                      boolean preserveCollectionOldValues,
                                      String pivot,
                                      String key,
                                      String source,
                                      String userName,
                                      MetadataRepository repository) {
        super(originalDocument, newDocument, preserveCollectionOldValues, source, userName, repository);
        this.pivot = pivot;
        this.key = key;
    }

    @Override
    public List<Action> visit(ComplexTypeMetadata complexType) {
        Accessor accessor = newDocument.createAccessor(pivot);
        for (int i = 1; i <= accessor.size(); i++) {
            String path = pivot + '[' + i + ']';
            Accessor keyAccessor = newDocument.createAccessor(path + '/' + key);
            keyValueToPath.put(keyAccessor.get(), path);
        }
        return super.visit(complexType);
    }

    @Override
    protected Closure getClosure() {
        return new Closure() {
            public void execute(FieldMetadata field) {
                String currentPath = getLeftPath();
                if (currentPath.startsWith(pivot)) {
                    compare(field);
                }
            }
        };
    }

    String getLeftPath() {
        return computePath(leftPath);
    }

    String getRightPath() {
        return computePath(rightPath);
    }

    private String computePath(Stack<String> path) {
        if (path.isEmpty()) {
            throw new IllegalStateException();
        } else {
            StringBuilder builder = new StringBuilder();
            Iterator<String> pathIterator = path.iterator();
            while (pathIterator.hasNext()) {
                builder.append(pathIterator.next());
                if (pathIterator.hasNext()) {
                    builder.append('/');
                }
            }
            return builder.toString();
        }
    }

    boolean inPivot;

    protected void handleField(FieldMetadata field, Closure closure) {
        leftPath.add(field.getName());
        if (pivot.equals(getLeftPath())) {
            inPivot = true;
        }
        rightPath.add(field.getName());
        if (field.isMany()) {
            Accessor leftAccessor;
            Accessor rightAccessor;
            try {
                leftAccessor = originalDocument.createAccessor(getLeftPath());
                rightAccessor = newDocument.createAccessor(getRightPath());
                if (!rightAccessor.exist()) {
                    // If new list does not exist, it means element was omitted in new version (legacy behavior).
                    return;
                }
            } finally {
                leftPath.pop();
                rightPath.pop();
            }
            // Proceed in "reverse" order (highest index to lowest) so there won't be issues when deleting elements in
            // a sequence (if element #2 is deleted before element #3, element #3 becomes #2...).
            int max = Math.max(leftAccessor.size(), rightAccessor.size());
            for (int i = max; i > 0; i--) {
                // XPath indexes are 1-based (not 0-based).
                leftPath.add(field.getName() + '[' + i + ']');
                if (inPivot) {
                    Accessor originalKeyAccessor = originalDocument.createAccessor(getLeftPath() + '/' + key);
                    String newDocumentPath = keyValueToPath.get(originalKeyAccessor.get());
                    if (newDocumentPath == null) {
                        return;
                    }
                    StringTokenizer pathIterator = new StringTokenizer(newDocumentPath, "/");
                    rightPath.clear();
                    while (pathIterator.hasMoreTokens()) {
                        rightPath.add(pathIterator.nextToken());
                    }
                } else {
                    rightPath.add(field.getName() + '[' + i + ']');
                }
                {
                    closure.execute(field);
                }
                rightPath.pop();
                leftPath.pop();
            }
            leftPath.add(field.getName() + '[' + max + ']');
            rightPath.add(field.getName() + '[' + max + ']');
            {
                lastMatchPath = getLeftPath();
            }
            rightPath.pop();
            leftPath.pop();
        } else {
            closure.execute(field);
            leftPath.pop();
            rightPath.pop();
        }
    }

    protected void compare(FieldMetadata comparedField) {
        if (comparedField.isKey()) {
            // Can't update a key: don't even try to compare the field (but update lastMatchPath in case next compared
            // element is right after key field).
            lastMatchPath = getLeftPath();
            return;
        }
        String leftPath = getLeftPath();
        String rightPath = getRightPath();
        Accessor originalAccessor = originalDocument.createAccessor(leftPath);
        Accessor newAccessor = newDocument.createAccessor(rightPath);
        if (!originalAccessor.exist()) {
            if (!newAccessor.exist()) {
                // No op
            } else { // new accessor exist
                generateNoOp(lastMatchPath);
                if (newAccessor.get() != null && !newAccessor.get().isEmpty()) { // Empty accessor means no op to ensure legacy behavior
                    actions.add(new FieldUpdateAction(date, source, userName, leftPath, StringUtils.EMPTY, newAccessor.get(), comparedField));
                    generateNoOp(leftPath);
                } else {
                    // No op.
                }
            }
        } else { // original accessor exist
            String oldValue = originalAccessor.get();
            lastMatchPath = leftPath;
            if (!newAccessor.exist()) {
                if (comparedField.isMany()) {
                    // Null values may happen if accessor is targeting an element that contains other elements
                    actions.add(new FieldUpdateAction(date, source, userName, leftPath, oldValue == null ? StringUtils.EMPTY : oldValue, null, comparedField));
                }
            } else { // new accessor exist
                if (oldValue != null && !oldValue.equals(newAccessor.get())) {
                    if (comparedField.isMany() && preserveCollectionOldValues) {
                        // Append at the end of the collection
                        if (!originalFieldToLastIndex.containsKey(comparedField)) {
                            originalFieldToLastIndex.put(comparedField, originalAccessor.size() + 1);
                        }
                        String previousPathElement = this.path.pop();
                        int newIndex = originalFieldToLastIndex.get(comparedField);
                        this.leftPath.push(comparedField.getName() + "[" + (newIndex + 1) + "]");
                        actions.add(new FieldUpdateAction(date, source, userName, leftPath, StringUtils.EMPTY, newAccessor.get(), comparedField));
                        this.leftPath.pop();
                        this.leftPath.push(previousPathElement);
                        originalFieldToLastIndex.put(comparedField, newIndex + 1);
                    } else {
                        actions.add(new FieldUpdateAction(date, source, userName, leftPath, oldValue, newAccessor.get(), comparedField));
                    }
                } else if (oldValue == null && newAccessor.get() != null) {
                    actions.add(new FieldUpdateAction(date, source, userName, leftPath, oldValue, newAccessor.get(), comparedField));
                }
            }
        }
    }
}
