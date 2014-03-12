package de.oxxid.android2csv;

import java.io.IOException;

public class Main {

	/** absolute path of csv file - optional */
	static String pathCsvFile;
	
	/** name of csv file - optional */
	static String fileNameCsv;
	
	/** path to xml directory */
	static String pathXmlDirectory;
	
	/** path to android project folder */
	static String pathProjectDirectory;

	static String fileName;

	/** original language used as first column an csv */
	static String origLanguage;

	/** target languages */
	static String[] targetLanguages;

	public static void main(String[] args){
		if(processArgumentsXmlToCsv(args)){
			try {
				XmlToCsv.dataToCsv(pathProjectDirectory, pathCsvFile, origLanguage, targetLanguages);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if(processArgumentsCsvToXml(args)){
			try {
				CsvToXml.dataToXml(pathCsvFile, pathXmlDirectory, targetLanguages);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if(processArgumentsMergeXmlInCsv(args)){
			try {
				MergeInCsv.mergeInCsv(pathProjectDirectory, pathCsvFile, origLanguage, targetLanguages);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			System.out.println(
					"usage XML to CSV: android-metaphrase --to-csv --project-directory --orig-lang [original language]" +
					" --target-langs [target1,target2,...] --path-csv [path of output csv file]" + "\n\n" +
					
					"usage CSV to XML: android-metaphrase --to-xml --path-csv [path of output csv file] --target-langs [target1,target2,...] --xml-directory [directory where the xml files are stored]" + "\n\n" +
					
					"usage MERGE XML in CSV: android-metaphrase --merge --file-csv --project-directory --target-langs [target1,target2,...]"
			);
		}
	}

	private static boolean processArgumentsCsvToXml(String[] args){
		boolean toXml = false;
		
		for (int i=0; i<args.length; i++) {
			if(args[i].equals("--to-xml")){
				toXml = true;
			}
			if(args[i].equals("--path-csv") && args.length > i+1){
				pathCsvFile = args[i+1];
				i++;
			}
			if(args[i].equals("--targetLangs") && args.length > i+1){
				targetLanguages = args[i+1].trim().split(",");
			}
			if(args[i].equals("--xml-directory") && args.length > i+1){
				pathXmlDirectory = args[i+1];
			}
		}
		if(!toXml) return false;
		if(pathCsvFile == null || pathCsvFile.equals("")) return false;
		if(pathXmlDirectory == null || pathXmlDirectory.equals("")) return false;
		return true;
	}
	
	private static boolean processArgumentsMergeXmlInCsv(String[] args){
		boolean merge = false;
		
		for (int i=0; i<args.length; i++) {
			if(args[i].equals("--merge")){
				merge = true;
			}
			if(args[i].equals("--project-directory") && args.length > i){
				pathProjectDirectory = args[i+1];
				i++;
			}
			if(args[i].equals("--file-csv") && args.length > i){
				pathCsvFile = args[i+1];
				i++;
			}
			else if(args[i].equals("--targetLangs") && args.length > i){
				targetLanguages = args[i+1].trim().split(",");
			}
			else if(args[i].equals("--filename") && args.length > i){
				fileName = args[i+1];
			}
		}

		if(	pathCsvFile == null || pathCsvFile.equals("") || 
			pathProjectDirectory == null || pathProjectDirectory.equals("") || 
			!merge){
			
			return false;
		}
		if(targetLanguages == null || targetLanguages.length < 1){
			return false;
		}

		return true;
	}

	private static boolean processArgumentsXmlToCsv(String[] args){
//		"usage XML to CSV: android2csv --to-csv --project-directory --orig-lang [original language]" +
//				" --target-langs [target1,target2,...] --path-csv [path of output csv file]" + "\n\n" +
		
		boolean toCsv = false;
		
		for (int i=0; i<args.length; i++) {
			if(args[i].equals("--to-csv")){
				toCsv = true;
			}
			if(args[i].equals("--project-directory") && args.length > i+1){
				pathProjectDirectory = args[i+1];
				i++;
			}
			else if(args[i].equals("--orig-lang") && args.length > i+1){
				origLanguage = args[i+1];
			}
			else if(args[i].equals("--target-langs") && args.length > i+1){
				targetLanguages = args[i+1].trim().split(",");
			}
			else if(args[i].equals("--path-csv") && args.length > i+1){
				pathCsvFile = args[i+1];
			}
		}
		if(!toCsv) return false;
		if(pathProjectDirectory == null || pathProjectDirectory.equals("")) return false;
		if(origLanguage == null || origLanguage.equals("")) return false;
		if(targetLanguages == null || targetLanguages.length < 1) return false;

		return true;
	}
}
