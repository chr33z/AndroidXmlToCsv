package de.oxxid.android2csv;

import java.util.ArrayList;
import java.util.LinkedList;

import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Text;

public class AndroidXmlParser {
	
	public static ArrayList<ArrayList<String>> readXmlStrings(Node current, ArrayList<ArrayList<String>> table, int coloumns) {
		for (int i = 0; i < current.getChildCount(); i++) {
			String[] pair = readXmlElement((current.getChild(i)));
			if(pair != null){
				ArrayList<String> row = new ArrayList<String>(coloumns);
				row.add(pair[0]);
				row.add(pair[1]);
				table.add(row);
			}
		}
		return table;
	}
	
	private static String[] readXmlElement(Node current){
		if (current instanceof Element) {
			Element element = (Element) current;
			
			if(element.getLocalName().equals("string")){
				String resourceName = element.getAttributeValue("name");
				String resourceValue = element.getChildCount() > 0
						? readXmlText(element.getChild(0))
						: null;
				
				if(resourceName != null && 
					resourceValue != null &&
					!resourceValue.startsWith("@string/") &&
					!resourceValue.startsWith("@array/")){
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
	
	public static void appendTable(ArrayList<ArrayList<String>> table1, ArrayList<ArrayList<String>> table2){
		table1.addAll(table2);
	}
	
	public static ArrayList<ArrayList<String>> mergeTables(ArrayList<ArrayList<String>> table1, ArrayList<ArrayList<String>> table2){
		if(table1.size() < 1){
			table1.addAll(table2);
			return table1;
		} else {
			for (int i = 0; i < table1.size(); i++) {
				for (int j = 0; j < table2.size(); j++) {
					String stringName1 = ((ArrayList<String>)table1.get(i)).get(1);
					String stringName2 = ((ArrayList<String>)table2.get(j)).get(1);
					
					if(stringName1.equals(stringName2)){
						String value = ((ArrayList<String>)table2.get(j)).get(2);
						((ArrayList<String>)table1.get(i)).add(value);
					}
				}
			}
		}
		
		return null;
	}
}
