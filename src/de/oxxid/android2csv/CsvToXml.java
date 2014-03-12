package de.oxxid.android2csv;

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

/**
 * Converts an csv file with android strings in different languages in xml files containing one
 * translation each.
 * 
 * @author Christopher Gebhardt
 *
 */
public class CsvToXml {
	
	private static final String CSV_DELIMITER = "\t";
	
	private static String stringItem = "<string name=\"%id%\">%value%</string>\n";
	
	/**
	 * Extract strings for each language in save them in a file with name "string-[locale].xml"
	 * @param path path to csv file
	 * @throws IOException
	 */
	public static void dataToXml(String pathCsvFile, String pathXmlDirectory, String[] targetLanguages) throws IOException{
		
		// get csv file
		File csvFile = new File(pathCsvFile);
		if(!csvFile.exists()){
			System.out.println("[ERROR]: CSV file" + csvFile + "does not exist!");
			return;
		}
		
		File xmlDirectory = getXmlDirectory(pathXmlDirectory);
		if(xmlDirectory != null){
			System.out.println("Created XML directory at " + xmlDirectory);
		} else {
			System.out.println("[ERROR]: Could not create XML folder " + pathXmlDirectory);
			return;
		}

		/* store a map containing a list of strings for each locale */
		HashMap<String, List<String>> fileMap = new HashMap<String, List<String>>();
		
		/* store indices of language columns */
		HashMap<Integer, String> indexMap;
		
		/* Store lines of csv file as string array of all cells */
		ArrayList<String[]> csvlines = readCsvLines(csvFile, CSV_DELIMITER);
		
		indexMap = getIndexMap(csvlines, targetLanguages);
		
		for (int i = 1; i < csvlines.size(); i++) {
			String[] strings = csvlines.get(i);
			
			String stringId = strings[1];
			
			for (int j = 0; j < strings.length; j++) {
				String string = strings[j];
				
				/*
				 * look if indexMap contains locale in this colum.
				 * Then initialize a list in the fileMap if necessary and put 
				 * the xml string in the map
				 */
				if(indexMap.containsKey(j)){
					if(!fileMap.containsKey(indexMap.get(j))){
						fileMap.put(indexMap.get(j), new ArrayList<String>());
					}
					fileMap.get(indexMap.get(j)).add(buildStringItem(stringId, string));
				}
			}
		}
		
		writeToFiles(fileMap, csvFile.getParent());
	}
	
	/**
	 * Get the xml folder where all files are stored and create parent folders if necessary
	 * @param path path to xml folder
	 * @return xml directory
	 */
	private static File getXmlDirectory(String path){
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
	 * Builds an valid android string item
	 * @return an xml string item for android strings
	 */
	private static String buildStringItem(String id, String value){
		String result = stringItem;
		
		result = result.replace("%id%", id.replace("\"", ""));
		result = result.replace("%value%", value.replace("\"", ""));
		return result;
	}

	/**
	 * Create an index of locales that should be exported
	 * @param csvlines
	 * @return a map containing the locale and the column index
	 */
	private static HashMap<Integer, String> getIndexMap(ArrayList<String[]> csvlines, String[] targetLanguages) {
		HashMap<Integer, String> indexMap = new HashMap<Integer, String>();
		
		if(csvlines != null && csvlines.size() > 0){
			String[] firstLine = csvlines.get(0);
			
			if(targetLanguages == null){
				if(firstLine.length > 2){
					for (int i = 2; i < firstLine.length; i++) {
						indexMap.put(i, firstLine[i].replace("\"", ""));
					}
				}
			} else {
				if(firstLine.length > 2){
					for (int i = 2; i < firstLine.length; i++) {
						for (int j = 0; j < targetLanguages.length; j++) {
							if(targetLanguages[j].equals(firstLine[i].replace("\"", ""))){
								indexMap.put(i, firstLine[i].replace("\"", ""));
							}
						}
					}
				}
			}
		}
		return indexMap;
	}
	
	private static ArrayList<String[]> readCsvLines(File file, String delimiter){
		ArrayList<String[]> lines = new ArrayList<String[]>();
		
		FileReader fileReader = null;
		String line = "";
		try {
			fileReader = new FileReader(file);
			BufferedReader reader = new BufferedReader(fileReader);
			while ((line = reader.readLine()) != null) {
				if(!line.equals("")){
					lines.add(line.split(delimiter));
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException("File not found");
		} catch (IOException e) {
			throw new RuntimeException("IO Error occured");
		} finally {
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return lines;
	}
	
	/**
	 * Split HashMap in different locales and save each in a file
	 * @param fileMap
	 */
	private static void writeToFiles(HashMap<String, List<String>> fileMap, String directoryPath){
		String header = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n";
		String footer = "</resources>";
		
		for (Entry<String, List<String>> localeEntry : fileMap.entrySet()) {
			String result = header;
			for (String stringItem : localeEntry.getValue()) {
				result += stringItem;
			}
			result += footer;
			
			String fileName = "string-"+localeEntry.getKey()+".xml";
			
			FileWriter fileWriter = null;
	        try {
	            File stringFile = new File(directoryPath + "/values-" + localeEntry.getKey() + "/" + fileName);
	    		File parentFile = stringFile.getParentFile();
	    		
	    		if(!parentFile.exists()){
	    			if(!parentFile.mkdirs()){
	    				System.out.println("[ERROR]: Could not create file " + stringFile);
	    			}
	    		}
    			fileWriter = new FileWriter(stringFile);
	            fileWriter.write(result);
	            fileWriter.close();
	            System.out.println("Wrote XML file to " + stringFile);
	            
	        } catch (IOException ex) {
	            System.out.println("[ERROR]: " + ex.getLocalizedMessage());
	        } finally {
	            try {
	                fileWriter.close();
	            } catch (IOException ex) {
	            	System.out.println("[ERROR]: " + ex.getLocalizedMessage());
	            	System.out.println(ex);
	            } catch(NullPointerException ex){
	            }
	        }
		}
	}
}
