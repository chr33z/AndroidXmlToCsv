package de.oxxid.android2csv;

import java.awt.PageAttributes.OriginType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.Text;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * Converts an csv file with android strings in different languages in xml files containing one
 * translation each.
 * 
 * @author Christopher Gebhardt
 *
 */
public class MergeInCsv {
	
	private static final char CSV_DELIMITER = '\t';
	
	private static String stringItem = "<string name=\"%id%\">%value%</string>\n";
	
	public static void mergeInCsv(String pathDirectoryXml, String pathCsv, String origLanguage, String[] targetLanguages) throws IOException{
		
		// make backup of original csv file
		File csvFile = new File(pathCsv);
		File csvNewFile = new File(csvFile.getAbsolutePath().replace(".csv", "-new-strings.csv"));
		File backup = new File(csvFile.getAbsolutePath()+".bak");
		Utils.copy(csvFile, backup);
		
		// create temp csv file
		CSVWriter writer = new CSVWriter(new FileWriter(csvNewFile), CSV_DELIMITER);
		
		// Store column indices of locales 
		HashMap<String, Integer> indexMap;
		// store all string ids in the csv file to quickly look if they are already there
		HashMap<String, String> csvLookup;
		
		if(csvFile.exists()){
			csvLookup = createCsvLookupMap(new File(pathCsv));
		} else {
			System.out.println("[ERROR]: CSV-file in "+pathCsv+" does not exist.");
			writer.close();
			return;
		}
		
		// table containing all strings from all languages
		ArrayList<ArrayList<String>> table = new ArrayList<ArrayList<String>>();
		int coloumns = targetLanguages.length + 3;

		File sourcePath = new File(pathDirectoryXml);
		File[] files = sourcePath.listFiles();
		
		// iterate all directoryies in source directory
		for (File file : files) {
			if(file.isDirectory() && equalsSpecifiedLanguage(file.getName(), origLanguage, targetLanguages)){
				ArrayList<File> xmlFiles = Utils.getXmlFilesFromDirectory(file);

				System.out.println("Found "+xmlFiles.size()+" files in directory \""+ file.getAbsolutePath() +"\". Crunching through...");

				// store lines for each language (from all language files) in one table
				ArrayList<ArrayList<String>> langTable = new ArrayList<ArrayList<String>>();

				// iterate all xml files in locale directory
				for (File xmlFile : xmlFiles) {
					if(xmlFile.isDirectory()) continue;

					try {
						Builder parser = new Builder();
						Document doc = parser.build(xmlFile);
						Element root = doc.getRootElement();

						// get file contents and store in separate table (one table per xml file)
						ArrayList<ArrayList<String>> xmlTable = new ArrayList<ArrayList<String>>();
						readXmlStrings(root, xmlTable, coloumns, xmlFile.getName());

						// add files to table that are NOT in the csvLookupMap
						for (ArrayList<String> xmlList : xmlTable) {
							if(!csvLookup.containsKey(xmlList.get(1))){
								langTable.add(xmlList);
							}
						}

					} catch (ParsingException ex) {
						System.err.println("Parsing error in file \""+xmlFile.getAbsolutePath()+"\"!");
					}
					catch (IOException ex) {
						System.err.println("File \""+xmlFile.getAbsolutePath()+"\" might not exist or is broken");
					}
				}

				// merge language in table containing all languages
				XmlToCsv.mergeTables(table, langTable);

			} else {
				System.out.println("Directory "+file.getAbsolutePath()+" has not the right format. Try renaming it like a target language you chose.");
			}
		}

		System.out.println(csvFile.getAbsolutePath());
		for (ArrayList<String> row : table) {
			writer.writeNext(row.toArray(new String[row.size()]));
		}
		writer.close();
		
		/*
		 * make a copy of original
		 */
		File origCsvTemp = new File(csvFile.getAbsoluteFile()+".orig.tmp");
		Utils.copy(csvFile, origCsvTemp);
		Utils.mergeFiles(new File[]{csvNewFile} ,csvFile);
		
		origCsvTemp.delete();
	}

	private static boolean equalsSpecifiedLanguage(String name, String origLanguage, String[] targetLanguages){
		boolean result = false;
		for (String string : targetLanguages) {
			if(name.equals(string)) result = true;
		}
		if(origLanguage.equals(name)) result = true;

		return result;
	}
	
	public static ArrayList<ArrayList<String>> readXmlStrings(Node current, ArrayList<ArrayList<String>> table, int coloumns, String fileName) {
		for (int i = 0; i < current.getChildCount(); i++) {
			String[] pair = readXmlElement((current.getChild(i)));
			if(pair != null){
				ArrayList<String> row = new ArrayList<String>(coloumns);
				row.add(fileName);
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
	
	/**
	 * Creates a lookup map to save all ids currently in the csv file
	 * @param csvFile
	 * @return map with string id as key and filename as value
	 * @throws IOException
	 */
	private static HashMap<String, String> createCsvLookupMap(File csvFile) throws IOException{
		HashMap<String, String> result = new HashMap<String, String>();
		CSVReader reader = new CSVReader(new FileReader(csvFile), '\t');
	    String [] nextLine;
	    while ((nextLine = reader.readNext()) != null) {
	    	if(nextLine != null && nextLine.length > 1){
	    		result.put(nextLine[1], nextLine[0]);
	    	}
	    }
	    reader.close();
	    
	    return result;
	}
}
