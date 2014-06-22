package org.talend.mdm.webapp.browserecords.client.widget.inputfield.converter;

import java.util.Date;

import org.talend.mdm.webapp.browserecords.client.util.DateUtil;

import com.extjs.gxt.ui.client.binding.Converter;


public class DateTimeConverter extends Converter {

    public Object convertModelValue(Object value) {
        return DateUtil.convertStringToDate(DateUtil.dateTimePattern, (String)value);
    }

    public Object convertFieldValue(Object value) {
        return DateUtil.getDateTime((Date)value);
    }
}
