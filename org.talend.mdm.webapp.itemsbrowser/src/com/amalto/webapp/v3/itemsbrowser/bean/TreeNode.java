package com.amalto.webapp.v3.itemsbrowser.bean;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.amalto.core.util.Util;
import com.amalto.webapp.core.dmagent.SchemaWebAgent;
import com.sun.xml.xsom.XSAnnotation;

public class TreeNode implements Cloneable {

    public TreeNode() {
        super();
        if (!Util.isEnterprise()) {
            readOnly = false;
            denyCreatable = false;
            denyLogicalDeletable = false;
            denyPhysicalDeletable = false;
        }
    }

    private String name;

    private String description;

    private String value;

    private String valueInfo;

    private boolean expandable;

    private String type;

    private int nodeId;

    // add by ymLi 0008917
    private TreeNode parent;

    private String taskId;

    // browse item specific
    private String typeName;

    private String xmlTag;

    private String documentation;

    private String labelOtherLanguage;

    private boolean readOnly = true;

    private int maxOccurs;

    private int minOccurs;

    private boolean nillable = true;

    private boolean choice;

    private ArrayList<Restriction> restrictions;

    private ArrayList<String> enumeration;

    private boolean retrieveFKinfos = false;

    private String fkFilter;

    private String foreignKey;

    private String usingforeignKey;

    private boolean visible;

    private boolean key = false;

    private int keyIndex = -1;

    // add by fliu 0009157
    private HashMap<String, String> facetErrorMsgs = new HashMap<String, String>();

    private String realValue;

    private String bindingPath;

    private boolean polymiorphism;

    private ArrayList<SubTypeBean> subTypes;

    private String realType;

    private boolean isAbstract = false;

    private boolean autoExpand;

    private String label;

    private boolean isDisplayDefalutValue = true;

    public boolean isDisplayDefalutValue() {
        return isDisplayDefalutValue;
    }

    public void setDisplayDefalutValue(boolean isDisplayDefalutValue) {
        this.isDisplayDefalutValue = isDisplayDefalutValue;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }

    // extra access
    boolean denyCreatable;

    boolean denyLogicalDeletable;

    boolean denyPhysicalDeletable;

    public boolean isDenyCreatable() {
        return denyCreatable;
    }

    public void setDenyCreatable(boolean denyCreatable) {
        this.denyCreatable = denyCreatable;
    }

    public boolean isDenyLogicalDeletable() {
        return denyLogicalDeletable;
    }

    public void setDenyLogicalDeletable(boolean denyLogicalDeletable) {
        this.denyLogicalDeletable = denyLogicalDeletable;
    }

    public boolean isDenyPhysicalDeletable() {
        return denyPhysicalDeletable;
    }

    public void setDenyPhysicalDeletable(boolean denyPhysicalDeletable) {
        this.denyPhysicalDeletable = denyPhysicalDeletable;
    }

    public boolean isPolymiorphism() {
        return polymiorphism;
    }

    public void setPolymiorphism(boolean polymiorphism) {
        this.polymiorphism = polymiorphism;
    }

    public ArrayList<SubTypeBean> getSubTypes() {
        return subTypes;
    }

    public void setSubTypes(ArrayList<SubTypeBean> subTypes) {
        this.subTypes = subTypes;
    }

    public String getRealType() {
        return realType;
    }

    public void setRealType(String realType) {
        this.realType = realType;
    }

    public void setRealValue(String realValue) {
        this.realValue = realValue;
    }

    public String getRealValue() {
        return realValue;
    }

    public String getBindingPath() {
        return bindingPath;
    }

    public void setBindingPath(String bindingPath) {
        this.bindingPath = bindingPath;
    }

    // add by ymli; fix the bug:0013463
    // private HashMap<String, String> displayFomats = new HashMap<String, String>();
    private String[] displayFomats = new String[2];

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.getMessage());
        }
    }

    public void fetchAnnotations(XSAnnotation xsa, ArrayList<String> roles, String language) throws Exception {
        org.apache.log4j.Logger.getLogger(this.getClass()).debug("fetchAnnotation() ");
        String X_Label = "X_Label_" + language.toUpperCase();
        try {
            if (xsa != null && xsa.getAnnotation() != null) {
                Element el = (Element) xsa.getAnnotation();
                NodeList annotList = el.getChildNodes();
                ArrayList<String> pkInfoList = new ArrayList<String>();
                ArrayList<String> fkInfoList = new ArrayList<String>();
                boolean writable = false;
                boolean denycreatable = false;
                for (int k = 0; k < annotList.getLength(); k++) {
                    if ("appinfo".equals(annotList.item(k).getLocalName())) {
                        Node source = annotList.item(k).getAttributes().getNamedItem("source");
                        if (source == null)
                            continue;
                        String appinfoSource = annotList.item(k).getAttributes().getNamedItem("source").getNodeValue();
                        if (X_Label.equals(appinfoSource)) {
                            setName(annotList.item(k).getFirstChild().getNodeValue());
                            setLabel(annotList.item(k).getFirstChild().getNodeValue());
                        } else if (appinfoSource.contains("X_Label")) {
                            setLabelOtherLanguage(annotList.item(k).getFirstChild().getNodeValue());
                        } else if ("X_Write".equals(appinfoSource)) {
                            if (roles.contains(annotList.item(k).getFirstChild().getNodeValue())) {
                                // setReadOnly(false);
                                writable = true;
                            }
                        } else if ("X_Hide".equals(appinfoSource)) {
                            if (roles.contains(annotList.item(k).getFirstChild().getNodeValue())) {
                                setVisible(false);
                            }
                        } else if ("X_ForeignKey".equals(appinfoSource)) {
                            setForeignKey(annotList.item(k).getFirstChild().getNodeValue());
                        }else if ("X_AutoExpand".equals(appinfoSource)) {
                        	String v=annotList.item(k).getFirstChild().getNodeValue();
                        	if(v!=null){
                        		setAutoExpand(Boolean.valueOf(v));
                        	}
                        } else if ("X_ForeignKey_Filter".equals(appinfoSource)) {
                            setFkFilter(annotList.item(k).getFirstChild().getNodeValue());
                        } else if ("X_ForeignKeyInfo".equals(appinfoSource)) {
                            fkInfoList.add(annotList.item(k).getFirstChild().getNodeValue());
                        } else if ("X_Retrieve_FKinfos".equals(appinfoSource)) {
                            setRetrieveFKinfos("true".equals(annotList.item(k).getFirstChild().getNodeValue()));
                        } else if ("X_PrimaryKeyInfo".equals(appinfoSource)) {
                            pkInfoList.add(annotList.item(k).getFirstChild().getNodeValue());
                        } else if (("X_Description_" + language.toUpperCase()).equals(appinfoSource)) {
                            Node description = annotList.item(k).getFirstChild();
                            String encodedDESP = description != null ? description.getNodeValue().replaceAll("\"", "&quot;") : "";
                            setDescription(encodedDESP);
                        } else if (appinfoSource.indexOf("X_Facet_") != -1) {
                            Pattern p = Pattern.compile("X_Facet_(.*?)");
                            Matcher matcher = p.matcher(appinfoSource);
                            if (matcher.matches()) {
                                setFacetErrorMsg(matcher.group(1).toLowerCase(), annotList.item(k).getFirstChild().getNodeValue());
                            }
                        } else if (appinfoSource.indexOf("X_Display_Format_") != -1) {
                            Pattern p = Pattern.compile("X_Display_Format_(.*?)");
                            Matcher matcher = p.matcher(appinfoSource);
                            if (matcher.matches() && matcher.group(1).toLowerCase().equals(language)) {
                                setDisplayFomats(matcher.group(1).toLowerCase(), annotList.item(k).getFirstChild().getNodeValue());
                            }
                        } else if ("X_Deny_Create".equals(appinfoSource)) {
                            if (roles.contains(annotList.item(k).getFirstChild().getNodeValue())) {
                                setDenyCreatable(true);
                                denycreatable = true;
                            }
                        } else if ("X_Deny_LogicalDelete".equals(appinfoSource)) {
                            if (roles.contains(annotList.item(k).getFirstChild().getNodeValue())) {
                                setDenyLogicalDeletable(true);
                            }
                        } else if ("X_Deny_PhysicalDelete".equals(appinfoSource)) {
                            if (roles.contains(annotList.item(k).getFirstChild().getNodeValue())) {
                                setDenyPhysicalDeletable(true);
                            }
                        }
                    }

                    if ("documentation".equals(annotList.item(k).getLocalName())) {
                        setDocumentation(annotList.item(k).getFirstChild().getNodeValue());
                    }
                }
                if (Util.isEnterprise()) {
                    readOnly = !writable;
                }
                setForeignKeyInfo(fkInfoList);
                setPrimaryKeyInfo(pkInfoList);
            }
        } catch (Exception e) {
            throw new Exception(e.getClass().getName() + ": " + e.getLocalizedMessage());
        }
    }

    public void fetchAttributes(Document d, String xpath) throws Exception {

        if (d == null || xpath == null)
            return;

        org.apache.log4j.Logger.getLogger(this.getClass()).debug("fetchAttributes() ");
        NodeList nodelist = Util.getNodeList(d, xpath);
        if (nodelist != null && nodelist.getLength() > 0) {
            NamedNodeMap attrs = nodelist.item(0).getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                Node attr = attrs.item(i);
                if (attr.getNodeName().equals("tmdm:type")) {
                    String foreignKeyType = attr.getNodeValue();
                    setUsingforeignKey(SchemaWebAgent.getInstance().getFirstBusinessConceptFromRootType(foreignKeyType).getName());
                    break;
                }
            }
        }
    }

    private ArrayList<String> primaryKeyInfo;

    public ArrayList<String> getPrimaryKeyInfo() {
        return primaryKeyInfo;
    }

    public void setPrimaryKeyInfo(ArrayList<String> primaryKeyInfo) {
        this.primaryKeyInfo = primaryKeyInfo;
    }

    private ArrayList<String> foreignKeyInfo;

    public ArrayList<String> getForeignKeyInfo() {
        return foreignKeyInfo;
    }

    public void setForeignKeyInfo(ArrayList<String> foreignKeyInfo) {
        this.foreignKeyInfo = foreignKeyInfo;
    }

    public String getForeignKey() {
        return foreignKey;
    }

    public void setForeignKey(String foreignKey) {
        this.foreignKey = foreignKey;
    }

    public String getUsingforeignKey() {
        return usingforeignKey;
    }

    public void setUsingforeignKey(String usingforeignKey) {
        this.usingforeignKey = usingforeignKey;
    }

    public boolean isChoice() {
        return choice;
    }

    public void setChoice(boolean choice) {
        this.choice = choice;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public boolean isExpandable() {
        return expandable;
    }

    public void setExpandable(boolean expandable) {
        this.expandable = expandable;
    }

    public String getLabelOtherLanguage() {
        return labelOtherLanguage;
    }

    public void setLabelOtherLanguage(String labelOtherLanguage) {
        this.labelOtherLanguage = labelOtherLanguage;
    }

    public int getMaxOccurs() {
        return maxOccurs;
    }

    public void setMaxOccurs(int maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    public int getMinOccurs() {
        return minOccurs;
    }

    public void setMinOccurs(int minOccurs) {
        this.minOccurs = minOccurs;
    }

    public boolean isNillable() {
        return nillable;
    }

    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getEnumeration() {
        return enumeration;
    }

    public void setEnumeration(ArrayList<String> enumeration) {
        this.enumeration = enumeration;
    }

    public boolean isRetrieveFKinfos() {
        return retrieveFKinfos;
    }

    public void setRetrieveFKinfos(boolean retrieveFKinfos) {
        this.retrieveFKinfos = retrieveFKinfos;
    }

    public String getFkFilter() {
        return fkFilter;
    }

    public void setFkFilter(String fkFilter) {
        this.fkFilter = fkFilter;
    }

    public ArrayList<Restriction> getRestrictions() {
        // edit by ymli; fix the bug:0011733
        // if there are more than one pattern, connect them to be one( and "|"
        // between them)
        ArrayList<Restriction> newRestrictions = new ArrayList<Restriction>();
        String value = "";
        if (restrictions != null) {
            for (int i = 0; i < restrictions.size(); i++) {
                Restriction re = restrictions.get(i);
                if (re.getName().equals("pattern"))
                    value += re.getValue() + "|";
                else
                    newRestrictions.add(re);
            }
            if (value.length() > 0) {
                Restriction re1 = new Restriction("pattern", value.substring(0, value.length() - 1));
                newRestrictions.add(re1);
            }

            return newRestrictions;
        }
        return restrictions;
    }

    public void setRestrictions(ArrayList<Restriction> restrictions) {
        this.restrictions = restrictions;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) throws ParseException {
        // System.out.println("type: "+type);
        // System.out.println("type Name: "+typeName);
        // edit by ymli; fix the bug:0013463
        this.realValue = value;

        if (displayFomats != null && displayFomats.length > 0) {
            String format = displayFomats[1];
            String lang = displayFomats[0];
            if (format != null && value != null && !"".equals(value) && !"".equals(format)) { //$NON-NLS-1$ //$NON-NLS-2$
                Locale locale = new Locale(lang);
                Object object = com.amalto.webapp.core.util.Util.getTypeValue("en", typeName, value, format);//$NON-NLS-1$
                if (object != null && object instanceof Calendar) {
                    value = com.amalto.webapp.core.util.Util.formatDate(format, (Calendar) object);
                } else if (object != null)
                    value = Util.printWithFormat(locale, format, object);
            }
        }
        this.value = value;
    }

    public String getValueInfo() {
        return valueInfo;
    }

    public void setValueInfo(String info) {
        this.valueInfo = info;
    }

    public String getXmlTag() {
        return xmlTag;
    }

    public void setXmlTag(String xmlTag) {
        this.xmlTag = xmlTag;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean hidden) {
        this.visible = hidden;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isKey() {
        return key;
    }

    public void setKey(boolean key) {
        this.key = key;
    }

    public int getKeyIndex() {
        return keyIndex;
    }

    public void setKeyIndex(int keyIndex) {
        this.keyIndex = keyIndex;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public boolean isAutoExpand() {
		return autoExpand;
	}

	public void setAutoExpand(boolean autoExpand) {
		this.autoExpand = autoExpand;
	}

	/**
     * add by fliu 0009157
     * 
     * @param lang
     * @param facet
     */
    public void setFacetErrorMsg(String lang, String facet) {
        if (facetErrorMsgs.containsKey(lang))
            facetErrorMsgs.remove(lang);
        facetErrorMsgs.put(lang, facet);
    }

    /**
     * add by fliu 0009157
     * 
     * @return the hashMap contains facet error msg
     */
    public HashMap getFacetErrorMsg() {
        return facetErrorMsgs;
    }

    /**
     * @author ymli fix the bug:0013463
     * @param lang
     * @param fomat
     */
    public void setDisplayFomats(String lang, String format) {
        /*
         * if(displayFomats.containsKey(lang)) displayFomats.remove(lang); displayFomats.put(lang, fomat);
         */
        displayFomats[0] = lang;
        displayFomats[1] = format;
    }

    /**
     * @author ymli fix the bug:0013463
     * @param lang
     * @param fomat
     */
    public String[] getDisplayFomats() {
        return displayFomats;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + nodeId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TreeNode other = (TreeNode) obj;
        if (nodeId != other.nodeId)
            return false;
        return true;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}