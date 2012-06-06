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

package com.amalto.core.storage;

import com.amalto.core.metadata.FieldMetadata;
import com.amalto.core.metadata.SimpleTypeFieldMetadata;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class FullTextResultsWriter implements DataRecordWriter {
    private final String keyword;

    public FullTextResultsWriter(String keyword) {
        this.keyword = keyword;
    }

    public void write(DataRecord record, OutputStream output) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(output);
        List<FieldMetadata> keyFields = record.getType().getKeyFields();

        writer.write("<item>");
        {
            {
                writer.write("<ids>");
                for (FieldMetadata keyField : keyFields) {
                    writer.write("<id>" + record.get(keyField) + "</id>");
                }
                writer.write("</ids>");
            }
            {
                writer.write("<title>");
                writer.write(record.getType().getName());
                for (FieldMetadata keyField : keyFields) {
                    writer.write(" " + record.get(keyField));
                }
                writer.write("</title>");
            }
            {
                writer.write("<text>");
                String[] snippetWords = new String[3];
                boolean hasMetKeyword = false;
                for (FieldMetadata field : record.getSetFields()) {
                    if (field instanceof SimpleTypeFieldMetadata) {
                        Object recordFieldValue = record.get(field);
                        if (recordFieldValue != null) {
                            String value = String.valueOf(recordFieldValue);
                            if (value.contains(keyword)) {
                                snippetWords[1] = "<b>" + value + "</b>";
                                hasMetKeyword = true;
                            } else {
                                snippetWords[hasMetKeyword ? 0 : 2] = value;
                                if (hasMetKeyword) {
                                    break;
                                }
                            }
                        }
                    }
                }
                StringBuilder builder = new StringBuilder();
                for (String snippetWord : snippetWords) {
                    builder.append(snippetWord).append(" ... ");
                }
                writer.write(builder.toString());
                writer.write("</text>");
            }
            {
                writer.write("<typeName>");
                writer.write(record.getType().getName());
                writer.write("</typeName>");
            }
        }
        writer.write("</item>");
        writer.flush();
    }
}
