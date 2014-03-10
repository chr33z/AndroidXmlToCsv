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

import javax.management.modelmbean.XMLParseException;

import au.com.bytecode.opencsv.CSVWriter;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;

public class Main {

	static String pathCsv;
	
	static String pathDirectoryXml;

	static String fileName;

	static String origLanguage;

	static String[] targetLanguages;

	public static void main(String[] args){
		if(processArgumentsImport(args)){
			try {
				XmlToCsv.dataToCsv(pathDirectoryXml, fileName, origLanguage, targetLanguages);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if(processArgumentsExport(args)){
			try {
				CsvToXml.dataToXml(pathCsv);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if(processArgumentsMerge(args)){
			try {
				MergeInCsv.mergeInCsv(pathDirectoryXml, pathCsv, origLanguage, targetLanguages);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			System.out.println("usage: android2csv --toCsv --toXml --path [path to file(s)] --origLang [original language]" +
					" --targetLangs [target1,target2,...] --filename [filename]");
		}
	}

	private static boolean processArgumentsExport(String[] args){
		boolean toXml = false;
		
		for (int i=0; i<args.length; i++) {
			if(args[i].equals("--toXml")){
				toXml = true;
			}
			if(args[i].equals("--csvFile") && args.length > i){
				pathCsv = args[i+1];
				i++;
			}
		}
		if(pathCsv == null || pathCsv.equals("") || !toXml) return false;
		return true;
	}
	
	private static boolean processArgumentsMerge(String[] args){
		boolean merge = false;
		
		for (int i=0; i<args.length; i++) {
			if(args[i].equals("--merge")){
				merge = true;
			}
			if(args[i].equals("--directoryXml") && args.length > i){
				pathDirectoryXml = args[i+1];
				i++;
			}
			if(args[i].equals("--csvFile") && args.length > i){
				pathCsv = args[i+1];
				i++;
			}
			else if(args[i].equals("--targetLangs") && args.length > i){
				targetLanguages = args[i+1].trim().split(",");
			}
			else if(args[i].equals("--filename") && args.length > i){
				fileName = args[i+1];
			}
		}

		if(	pathCsv == null || pathCsv.equals("") || 
			pathDirectoryXml == null || pathDirectoryXml.equals("") || 
			!merge){
			
			return false;
		}
		if(targetLanguages == null || targetLanguages.length < 1){
			return false;
		}

		return true;
	}

	private static boolean processArgumentsImport(String[] args){
		boolean toCsv = false;
		
		for (int i=0; i<args.length; i++) {
			if(!args[i].equals("--toCsv")){
				toCsv = true;
			}
			if(args[i].equals("--path") && args.length > i){
				pathDirectoryXml = args[i+1];
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

		if(pathDirectoryXml == null || pathDirectoryXml.equals("")) return false;
		if(origLanguage == null || origLanguage.equals("")) return false;
		if(targetLanguages == null || targetLanguages.length < 1) return false;

		return true;
	}
}
