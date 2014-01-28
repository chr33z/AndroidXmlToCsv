package de.oxxid.android2csv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import nu.xom.Builder;
import nu.xom.Comment;
import nu.xom.DocType;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.ProcessingInstruction;
import nu.xom.Text;
import nu.xom.ParsingException;
import nu.xom.jaxen.expr.EqualityExpr;

public class Main {

	static String path;

	static String origLanguage;

	static String[] targetLanguages;

	public static void main(String[] args){
		if(processArguments(args)){
			parseData();
		} else {
			System.out.println("usage: android2csv --path [path to files] --origLang [original language] --targetLangs [target1,target2,...]");
		}
	}

	private static void parseData(){
		ArrayList<File> files = getFilesFromDirectory(new File(path));

		if(files.size() < 1){
			System.out.println("There are no xml file in this directory.");
		}

		for (File file : files) {
			try {
				Builder parser = new Builder();
				Document doc = parser.build(file);
				Element root = doc.getRootElement();
				readXmlStrings(root);
			}
			catch (ParsingException ex) {
				System.err.println("Cafe con Leche is malformed today. How embarrassing!");
			}
			catch (IOException ex) {
				System.err.println("Could not connect to Cafe con Leche. The site may be down.");
			}
		}




	}

	public static void readXmlStrings(Node current) {

		String data = "";
		if (current instanceof Element) {
			Element temp = (Element) current;
			
			System.out.println(temp.getLocalName());
			
			if(temp.getLocalName().equals("string")){
			}
		}
		else if (current instanceof Text) {
			String value = current.getValue();
			System.out.println(value);
		}

		// Attributes are never returned by getChild()
		for (int i = 0; i < current.getChildCount(); i++) {
			readXmlStrings(current.getChild(i));
		}
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
				targetLanguages = args[i+1].split(",");
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
	private static ArrayList<File> getFilesFromDirectory(File directory){
		System.out.println(directory.getAbsolutePath());
		
		File[] files = directory.listFiles();

		ArrayList<File> results = new ArrayList<File>();

		for (File inFile : files) {
			
			System.out.println(inFile.getName());
			
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
