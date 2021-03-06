/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.plugin.base.replace;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.regex.Pattern;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class CompiledParameters implements Serializable {
	Pattern regexp = Pattern.compile(".*",Pattern.DOTALL);
	private String resultingContentType = "text/xml";
	private String replacement = "";

	public Pattern getRegexp() {
		return regexp;
	}

	public void setRegexp(Pattern regexp) {
		this.regexp = regexp;
	}

	public String getResultingContentType() {
		return resultingContentType;
	}

	public void setResultingContentType(String resultingContentType) {
		this.resultingContentType = resultingContentType;
	}
	
	public String serialize() throws IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream ois = new ObjectOutputStream(bos);
		ois.writeObject(this);
		return new BASE64Encoder().encode(bos.toByteArray());
	}
	
	public static CompiledParameters deserialize(String base64String) throws IOException,ClassNotFoundException{
		byte[] bytes = new BASE64Decoder().decodeBuffer(base64String); 
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bis);
		return (CompiledParameters)ois.readObject();
	}

	public String getReplacement() {
		return replacement;
	}

	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}
	
}


