/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iiit.ire.mp;

import gate.*;
import gate.creole.*;
import gate.util.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author messi
 */
public class GATEImplementation {

	private Pattern pattern;
	private Matcher matcher;
	private ArrayList anno_Person_values = new ArrayList();
	private ArrayList anno_Location_values = new ArrayList();
	private ArrayList anno_Time_values = new ArrayList();
	private ArrayList anno_Organization_values = new ArrayList();
	private ArrayList anno_Date_values = new ArrayList();
	private ArrayList anno_Money_values = new ArrayList();
	private ArrayList anno_Percent_values = new ArrayList();
	private ArrayList anno_Stock_Code_values = new ArrayList();
	private HashMap hm_entities = new HashMap();
	private GATESingleton gate = null;
	private GATEImplementation annie;
	private Corpus corpus;
	private SerialAnalyserController annieController;

	public GATEImplementation() {
		try {
			gate = GATESingleton.getInstance();
			annie = gate.annie;
			corpus = gate.corpus;
			annieController = gate.annieController;
		} catch (GateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getMatch(String text, String strp) {

		pattern = Pattern.compile(strp);
		matcher = pattern.matcher(text);
		String match = "";
		String matches = "";
		while (matcher.find()) {
			match = matcher.group();
			matches = matches + " " + match;
		}
		return matches;
	}

	/*
	*//** Tell ANNIE's controller about the corpus you want to run on */
	/*
	 * public void setCorpus(Corpus corpus) { annieController.setCorpus(corpus);
	 * } // setCorpus
	 *//** Run ANNIE */
	/*
	 * public void execute() throws GateException { //
	 * Out.prln("Running ANNIE..."); annieController.execute(); //
	 * Out.prln("...ANNIE complete"); } // execute()
	 */
	/**
	 * Run from the command-line, with a list of URLs as argument.
	 * <P>
	 * <B>NOTE:</B><BR>
	 * This code will run with all the documents in memory - if you want to
	 * unload each from memory after use, add code to store the corpus in a
	 * DataStore.
	 */
	public HashMap getEntities(String text) throws GateException, IOException {
		String xmlDocument = "";
		Document doc1 = (Document) Factory.newDocument(text);
		corpus.add(doc1);
		annieController.setCorpus(corpus);
		annieController.execute();
		Iterator iter = corpus.iterator();

		while (iter.hasNext()) {
			Document doc = (Document) iter.next();
			AnnotationSet defaultAnnotSet = doc.getAnnotations();

			Set annotTypesRequired = new HashSet();
			annotTypesRequired.add("Person");
			annotTypesRequired.add("Location");
			annotTypesRequired.add("Time");
			annotTypesRequired.add("Organization");
			annotTypesRequired.add("Date");
			annotTypesRequired.add("Money");
			annotTypesRequired.add("Percent");
			annotTypesRequired.add("Stock_Code");
			Set<Annotation> peopleAndPlaces = new HashSet<Annotation>(
					defaultAnnotSet.get(annotTypesRequired));
			getEntitiesHash(peopleAndPlaces, text);
		} // for each doc
		corpus.remove(doc1);
		return hm_entities;
	} // main

	public String getAnnotatedText(String text) throws GateException,
			IOException {
		String xmlDocument = "";

		Document doc1 = (Document) Factory.newDocument(text);
		corpus.add(doc1);
		annieController.setCorpus(corpus);
		annieController.execute();

		Iterator iter = corpus.iterator();
		String xml = "";
		while (iter.hasNext()) {
			Document doc = (Document) iter.next();
			AnnotationSet defaultAnnotSet = doc.getAnnotations();

			Set annotTypesRequired = new HashSet();
			annotTypesRequired.add("Person");
			annotTypesRequired.add("Location");
			annotTypesRequired.add("Time");
			annotTypesRequired.add("Organization");
			annotTypesRequired.add("Date");
			annotTypesRequired.add("Money");
			annotTypesRequired.add("Percent");
			annotTypesRequired.add("Stock_Code");
			Set<Annotation> peopleAndPlaces = new HashSet<Annotation>(
					defaultAnnotSet.get(annotTypesRequired));
			xml = doc.toXml(peopleAndPlaces, false);
		} // for each doc
		corpus.remove(doc1);
		return xml;
	} // main

	public void clear_hm() {
		hm_entities.clear();
		anno_Person_values.clear();
		anno_Location_values.clear();
		anno_Time_values.clear();
		anno_Organization_values.clear();
		anno_Date_values.clear();
		anno_Money_values.clear();
		anno_Percent_values.clear();
		anno_Stock_Code_values.clear();
	}

	private HashMap getEntitiesHash(Set<Annotation> peopleAndPlaces, String text) {
		Iterator it = peopleAndPlaces.iterator();
		Annotation a;
		while (it.hasNext()) {
			a = (Annotation) it.next();
			String type = a.getType();
			int start = a.getStartNode().getOffset().intValue();
			int end = a.getEndNode().getOffset().intValue();
			String[] value_offset = new String[2];
			if (text.length() >= end) {
				value_offset[0] = text.subSequence(start, end).toString();
				value_offset[1] = Integer.toString(start);
				if (type.equals("Person")) {
					anno_Person_values.add(value_offset);
				}
				if (type.equals("Location")) {
					anno_Location_values.add(value_offset);
				}
				if (type.equals("Time")) {
					anno_Time_values.add(value_offset);
				}
				if (type.equals("Organization")) {
					anno_Organization_values.add(value_offset);
				}
				if (type.equals("Date")) {
					anno_Date_values.add(value_offset);
				}
				if (type.equals("Money")) {
					anno_Money_values.add(value_offset);
				}
				if (type.equals("Percent")) {
					anno_Percent_values.add(value_offset);
				}
				if (type.equals("Stock_Code")) {
					anno_Stock_Code_values.add(value_offset);
				}
			}
		}

		if (anno_Person_values.size() > 0) {
			hm_entities.put("Person", anno_Person_values);
		}
		if (anno_Location_values.size() > 0) {
			hm_entities.put("Location", anno_Location_values);
		}
		if (anno_Time_values.size() > 0) {
			hm_entities.put("Time", anno_Time_values);
		}
		if (anno_Organization_values.size() > 0) {
			hm_entities.put("Organization", anno_Organization_values);
		}
		if (anno_Date_values.size() > 0) {
			hm_entities.put("Date", anno_Date_values);
		}
		if (anno_Money_values.size() > 0) {
			hm_entities.put("Money", anno_Money_values);
		}
		if (anno_Percent_values.size() > 0) {
			hm_entities.put("Percent", anno_Percent_values);
		}
		if (anno_Stock_Code_values.size() > 0) {
			hm_entities.put("Stock_Code", anno_Stock_Code_values);
		}

		return hm_entities;
	}

	// class StandAloneAnnie

	
	

	
	
	public static void main(String args[]) throws GateException, IOException {
		GATEImplementation g = new GATEImplementation();
		/*
		 * HashMap h = g.getEntities(
		 * "TAIPEI, Taiwan, Oct. 15, 2012 (GLOBE NEWSWIRE) -- Dalian Commodity Exchange (DCE) and The NASDAQ OMX Group, Inc. (Nasdaq:NDAQ) (Nasdaq OMX) signed a memorandum of understanding (MOU) on October 15 in Taipei, with an aim to seek new business opportunities on a global scale as well as substantial business exchanges and cooperation in more fields. DCE President & CEO Liu Xingqiang and NASDAQ OMX Vice Chairman Sandy Frucher were present at the signing ceremony."
		 * ); Iterator it = h.keySet().iterator(); while (it.hasNext()) { Object
		 * k = it.next(); System.out.println(k + " " + h.get(k).toString()); }
		 */
		System.out
				.println(g
						.getAnnotatedText("TAIPEI, Taiwan, Oct. 15, 2012 (GLOBE NEWSWIRE) -- Dalian Commodity Exchange (DCE) and The NASDAQ OMX Group, Inc. (Nasdaq:NDAQ) (Nasdaq OMX) signed a memorandum of understanding (MOU) on October 15 in Taipei, with an aim to seek new business opportunities on a global scale as well as substantial business exchanges and cooperation in more fields. DCE President & CEO Liu Xingqiang and NASDAQ OMX Vice Chairman Sandy Frucher were present at the signing ceremony."));
		System.out
				.println(g
						.getAnnotatedText("TAIPEI, Taiwan, Oct. 15, 2012 (GLOBE NEWSWIRE) -- Dalian Commodity Exchange (DCE) and The NASDAQ OMX Group, Inc. (Nasdaq:NDAQ) (Nasdaq OMX) signed a memorandum of understanding (MOU) on October 15 in Taipei, with an aim to seek new business opportunities on a global scale as well as substantial business exchanges and cooperation in more fields. DCE President & CEO Liu Xingqiang and NASDAQ OMX Vice Chairman Sandy Frucher were present at the signing ceremony."));
		System.out
				.println(g
						.getAnnotatedText("TAIPEI, Taiwan, Oct. 15, 2012 (GLOBE NEWSWIRE) -- Dalian Commodity Exchange (DCE) and The NASDAQ OMX Group, Inc. (Nasdaq:NDAQ) (Nasdaq OMX) signed a memorandum of understanding (MOU) on October 15 in Taipei, with an aim to seek new business opportunities on a global scale as well as substantial business exchanges and cooperation in more fields. DCE President & CEO Liu Xingqiang and NASDAQ OMX Vice Chairman Sandy Frucher were present at the signing ceremony."));
		System.out
				.println(g
						.getAnnotatedText("TAIPEI, Taiwan, Oct. 15, 2012 (GLOBE NEWSWIRE) -- Dalian Commodity Exchange (DCE) and The NASDAQ OMX Group, Inc. (Nasdaq:NDAQ) (Nasdaq OMX) signed a memorandum of understanding (MOU) on October 15 in Taipei, with an aim to seek new business opportunities on a global scale as well as substantial business exchanges and cooperation in more fields. DCE President & CEO Liu Xingqiang and NASDAQ OMX Vice Chairman Sandy Frucher were present at the signing ceremony."));

	}
}
