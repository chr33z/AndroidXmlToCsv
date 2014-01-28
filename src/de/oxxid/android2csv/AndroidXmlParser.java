package de.oxxid.android2csv;

import java.util.ArrayList;

import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Text;

public class AndroidXmlParser {
	
	public static ArrayList<String[]> readXmlStrings(Node current) {
		ArrayList<String[]> result = new ArrayList<String[]>();

		for (int i = 0; i < current.getChildCount(); i++) {
			String[] pair = readXmlElement((current.getChild(i)));
			if(pair != null){
				result.add(pair);
			}
		}
		
		return result;
	}
	
	private static String[] readXmlElement(Node current){
		if (current instanceof Element) {
			Element element = (Element) current;
			
			if(element.getLocalName().equals("string")){
				String resourceName = element.getAttributeValue("name");
				String resourceValue = element.getChildCount() > 0
						? readXmlText(element.getChild(0))
						: null;
				
				if(resourceName != null &&  resourceValue != null){
					System.out.println("resource name: "+resourceName+" resource value: "+resourceValue );
					return new String[]{resourceName, resourceValue};
				}
			}
		}
		return null;
	}
	
	
	private static String readXmlText(Node current){
		if (current instanceof Text) {
			return current.getValue();
		} else {
			return null;
		}
	}
}
