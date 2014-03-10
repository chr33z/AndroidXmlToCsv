package de.oxxid.android2csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import au.com.bytecode.opencsv.CSVWriter;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.Text;

public class XmlToCsv {
	
	static final char DEL = '\t';
	
	public static void dataToCsv(String pathDirectoryXml, String fileName,
			String origLanguage, String[] targetLanguages) throws IOException{
		
		ArrayList<ArrayList<String>> table = new ArrayList<ArrayList<String>>();
		int coloumns = targetLanguages.length + 3;

		// create csv file and add a header to the file
		File csvFile = createCsvDirectory(pathDirectoryXml, fileName);
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), DEL);
		addHeaderToCsv(writer, origLanguage, targetLanguages);

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

						// add file table to language table
						langTable.addAll(xmlTable);

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
	}

	private static void addHeaderToCsv(CSVWriter writer, String origLanguage, String[] targetLanguages){
		if(writer != null){
			ArrayList<String> header = new ArrayList<String>();
			header.add("filename");
			header.add("stringname");
			header.add(origLanguage);
			for (String lang : targetLanguages) {
				header.add(lang);
			}
			writer.writeNext(header.toArray(new String[header.size()]));
		}
	}

	private static File createCsvDirectory(String path, String fileName){
		File newDir = new File(path + "csv/");
		newDir.mkdir();

		String tmp = newDir.getAbsolutePath() + "/" + ((fileName != null) ? fileName : "androidStringResources") +".csv";
		return new File(tmp);
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
}
