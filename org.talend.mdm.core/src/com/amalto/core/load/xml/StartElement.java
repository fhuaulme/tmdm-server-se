// ============================================================================
//
// Copyright (c) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package com.amalto.core.load.xml;

import com.amalto.core.load.Constants;
import com.amalto.core.load.State;
import com.amalto.core.load.context.StateContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 */
public class StartElement implements State {
    public static final State INSTANCE = new StartElement();

    private StartElement() {
    }

    public void parse(StateContext context, XMLStreamReader reader) throws XMLStreamException {
        try {
            context.getWriter().writeStartElement(reader);
        } catch (Exception e) {
            throw new XMLStreamException(e);
        }

        String elementLocalName = reader.getName().getLocalPart();
        boolean isIdElement = context.enterElement(elementLocalName);
        if (!context.getPayLoadElementName().equals(elementLocalName) && isIdElement) {
            context.setCurrent(SetId.INSTANCE);
        } else {
            context.setCurrent(Selector.INSTANCE);
        }
    }

    public boolean isFinal() {
        return Constants.NON_FINAL_STATE;
    }

}
