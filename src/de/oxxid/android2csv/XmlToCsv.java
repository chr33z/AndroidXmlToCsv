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
	
	public static void dataToCsv(
			String pathProjectDirectory,
			String pathCsvFile,
			String origLanguage,
			String[] targetLanguages) throws IOException{
		
		// create csv file and add a header to the file
		File csvFile = createCsvFile(pathCsvFile);
		if(csvFile != null){
			System.out.println("Created csv file at " + csvFile);
		} else {
			System.out.println("Couldn't create file: " + pathCsvFile);
			return;
		}
		
		// get android resource folder
		File androidResourceDirectory = findResourceFolder(new File(pathProjectDirectory), 3, 0);
		if(androidResourceDirectory != null){
			System.out.println("Found resource at " + androidResourceDirectory);
		} else {
			System.out.println("Couldn't find android resource folder in: " + new File(pathProjectDirectory));
			return;
		}
		
		// initialize csv writer
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), DEL);
		addHeaderToCsv(writer, origLanguage, targetLanguages);
		
		ArrayList<ArrayList<String>> table = new ArrayList<ArrayList<String>>();
		int coloumns = targetLanguages.length + 3;

		// get folders in resource folder and iterate over them
		File[] files = androidResourceDirectory.listFiles();
		for (File file : files) {
			if(file.isDirectory() && isLanguageFolder(file.getName(), origLanguage, targetLanguages)){
				
				// get all xml files from that directory
				ArrayList<File> xmlFiles = Utils.getXmlFilesFromDirectory(file);
				System.out.println("Found "+xmlFiles.size()+" files in directory \""+ file +"\". Crunching through...");

				// store lines for each language (from all xml files) in one table
				ArrayList<ArrayList<String>> langTable = new ArrayList<ArrayList<String>>();

				// iterate all xml files in locale directory
				for (File xmlFile : xmlFiles) {
					if(xmlFile.isDirectory()){
						continue;
					}
					try {
						Builder parser = new Builder();
						Document doc = parser.build(xmlFile);
						Element root = doc.getRootElement();

						// get file contents and store in separate table (one table for each xml file)
						ArrayList<ArrayList<String>> xmlTable = new ArrayList<ArrayList<String>>();
						readXmlFile(root, xmlTable, coloumns, xmlFile.getName());

						// add file table to language table
						langTable.addAll(xmlTable);

					} catch (ParsingException ex) {
						System.err.println("Parsing error in file \""+xmlFile.getAbsolutePath()+"\"!");
					}
					catch (IOException ex) {
						System.err.println("File \""+xmlFile.getAbsolutePath()+"\" might not exist or is broken");
					}
				}

				// merge current xml table in table containing all languages
				XmlToCsv.mergeTables(table, langTable);

			}
		}

		for (ArrayList<String> row : table) {
			writer.writeNext(row.toArray(new String[row.size()]));
		}
		writer.close();
		System.out.println("Done. Wrote string table to "+csvFile);
	}

	/**
	 * recursive method looking for res folder 
	 * @param directory
	 * @param depth search depth, normally just 1
	 * @param currentDepth initialize with 0
	 * @return resource folder named "res" if available or null
	 */
	private static File findResourceFolder(File directory, int depth, int currentDepth){
		File resDirectory = null;
		
		if(currentDepth > depth || !directory.isDirectory() || directory.getName().equals("bin")){
			resDirectory = null;
		} else {
			if(directory.getName().equals("res")){
				resDirectory =  directory;
			} else {
				
				for (File child : directory.listFiles()) {
					resDirectory = findResourceFolder(child, depth, currentDepth + 1);
					if(resDirectory != null && resDirectory.getName().equals("res")){
						return resDirectory;
					}
				}
			}
		}
		return resDirectory;
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

	/**
	 * Create csv file and all necessary parent directories
	 * @param path absolute path to csv file including file name
	 * @return csv file
	 */
	private static File createCsvFile(String path){
		if(path == null || path.equals("")){
			path = "androidStringResources.csv";
		}
		
		File csvFile = new File(path);
		File parentFile = csvFile.getParentFile();
		
		if(!parentFile.exists() && !parentFile.mkdirs()){
		    return null;
		}
		return csvFile;
	}

	/**
	 * Check if the current folder is a values folder with the matching locale
	 * @param name name of directory
	 * @param origLanguage
	 * @param targetLanguages
	 * @return
	 */
	private static boolean isLanguageFolder(String name, String origLanguage, String[] targetLanguages){
		if(!name.contains("values")){
			return false;
		} else {
//			for (String string : targetLanguages) {
//				if(name.equals("values-" + string)){
//					return true;
//				}
//			}
			if(name.equals("values-" + origLanguage)){
				return true;
			}
		}
		return false;
	}
	
	public static ArrayList<ArrayList<String>> readXmlFile(Node current, ArrayList<ArrayList<String>> table, int coloumns, String fileName) {
		for (int i = 0; i < current.getChildCount(); i++) {
			
			// pair of string id and string value
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
					
					if(resourceValue.contains("\n")){
						System.out.println("[WARNING]: String resource " + resourceName + "contains line breaks." +
								" Generated csv file might be invalid!");
					}
					
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
