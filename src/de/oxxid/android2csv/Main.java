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
import nu.xom.ParsingException;

public class Main {

	static String path;

	static String fileName;

	static String origLanguage;

	static String[] targetLanguages;

	static final char DEL = '\t';

	public static void main(String[] args){
		if(processArguments(args)){
			try {
				parseData();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("usage: android2csv --path [path to files] --origLang [original language]" +
					" --targetLangs [target1,target2,...] --filename [filename]");
		}
	}

	private static void parseData() throws IOException{
		ArrayList<ArrayList<String>> table = new ArrayList<ArrayList<String>>();
		int coloumns = targetLanguages.length + 3;

		File csvFile = createCsvDirectory(path);
		CSVWriter writer = new CSVWriter(new FileWriter(csvFile), DEL);
		addHeaderToCsv(writer);

		File sourcePath = new File(path);
		File[] files = sourcePath.listFiles();

		for (File file : files) {
			if(file.isDirectory() && equalsSpecifiedLanguage(file.getName())){
				ArrayList<File> xmlFiles = getXmlFilesFromDirectory(file);
				
				System.out.println("Found "+xmlFiles.size()+" files in directory \""+ file.getAbsolutePath() +"\". Crunching through...");

				ArrayList<ArrayList<String>> langTable = new ArrayList<ArrayList<String>>();

				for (File xmlFile : xmlFiles) {
					if(xmlFile.isDirectory()) continue;

					try {
						Builder parser = new Builder();
						Document doc = parser.build(xmlFile);
						Element root = doc.getRootElement();

						// get file contents
						ArrayList<ArrayList<String>> xmlTable = new ArrayList<ArrayList<String>>();
						AndroidXmlParser.readXmlStrings(root, xmlTable, coloumns);

						for (ArrayList<String> arrayList : xmlTable) {
							arrayList.add(0, xmlFile.getName());
						}

						AndroidXmlParser.appendTable(langTable, xmlTable);

					} catch (ParsingException ex) {
						System.err.println("Parsing error in file \""+xmlFile.getAbsolutePath()+"\"!");
					}
					catch (IOException ex) {
						System.err.println("File \""+xmlFile.getAbsolutePath()+"\" might not exist or is broken");
					}
				}

				// merge language in big table
				AndroidXmlParser.mergeTables(table, langTable);

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

	private static void addHeaderToCsv(CSVWriter writer){
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

	private static File createCsvDirectory(String path){
		File newDir = new File(path + "csv/");
		newDir.mkdir();
		
		String tmp = newDir.getAbsolutePath() + "/" + ((fileName != null) ? fileName : "androidStringResources") +".csv";
		return new File(tmp);
	}

	private static boolean equalsSpecifiedLanguage(String name){
		boolean result = false;
		for (String string : targetLanguages) {
			if(name.equals(string)) result = true;
		}
		if(origLanguage.equals(name)) result = true;

		return result;
	}

	private static boolean processArguments(String[] args){
		for (int i=0; i<args.length; i++) {
			if(args[i].equals("--path") && args.length > i){
				path = args[i+1];
				i++;
			}
			else if(args[i].equals("--origLang") && args.length > i){
				origLanguage = args[i+1];
			}
			else if(args[i].equals("--targetLangs") && args.length > i){
				targetLanguages = args[i+1].trim().split(",");
			}
			else if(args[i].equals("--filename") && args.length > i){
				fileName = args[i+1];
			}
		}

		if(path == null || path.equals("")) return false;
		if(origLanguage == null || origLanguage.equals("")) return false;
		if(targetLanguages == null || targetLanguages.length < 1) return false;

		return true;
	}

	/*
	 * Read directory contents
	 */
	private static ArrayList<File> getXmlFilesFromDirectory(File directory){
		File[] files = directory.listFiles();

		ArrayList<File> results = new ArrayList<File>();

		for (File inFile : files) {
			if (!inFile.isDirectory()) {
				String fileName = inFile.getName();

				// if file ends on ".xml"
				String extension = "";
				int i = fileName.lastIndexOf('.');
				int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
				if (i > p) {
					extension = fileName.substring(i+1);
				}

				if(extension.equals("xml")){
					results.add(inFile);
				}
			}
		}
		return results;
	}
}
