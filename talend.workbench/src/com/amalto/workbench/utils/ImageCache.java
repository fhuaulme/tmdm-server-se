package com.amalto.workbench.utils;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;

import com.amalto.workbench.AmaltoWorbenchPlugin;

public class ImageCache {
	static org.eclipse.jface.resource.ImageRegistry registry=null;
	static{
		registry=JFaceResources.getImageRegistry();
		Map<String, EImage> map=EImage.getAlllEimages();
		Iterator<EImage> it=map.values().iterator();
		while(it.hasNext()){
			EImage image =it.next();			
			registry.put(image.getPath(), AmaltoWorbenchPlugin.getImageDescriptor(image.getPath()));
		}		
	}
	
	public static ImageDescriptor getImage(String path){
		if(null!=registry.getDescriptor(path))
			return registry.getDescriptor(path);
		else{
			return registry.getDescriptor("icons/talend-picto-small.gif");
		}
	}
}
