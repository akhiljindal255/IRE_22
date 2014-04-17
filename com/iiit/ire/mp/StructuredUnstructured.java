/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iiit.ire.mp;

import java.util.HashMap;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author messi
 */
public class StructuredUnstructured {

    private Pattern pattern;
    private Matcher matcher;
    private HashMap hm_mail;
    private HashMap hm_xmlbody;
    private ArrayList hm_keyword_match;
    private HttpClientWrapper http;
    private String[] strArray;
    
    public StructuredUnstructured() {

        hm_mail = new HashMap();
        hm_xmlbody = new HashMap();
        hm_keyword_match = new ArrayList();
        http = new HttpClientWrapper();
    }

    public ArrayList getHm_keyword_match() {
        return hm_keyword_match;
    }

    public void setHm_keyword_match() {
        hm_keyword_match.clear();
    }

    public Document stringToDom(String xmlSource)
            throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xmlSource)));
    }
    // FUNCTION MODIFIED 29/10/12 //
    public HashMap getXMLDetails(String date,String body) {
        /*try {
            System.out.println(xml);
            Document doc=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            //Document doc = stringToDom(xml);
            NodeList body_nodes = doc.getElementsByTagName("Body");
            NodeList date_nodes = doc.getElementsByTagName("RDate");
            String body = "";
            String date = "";
            for (int i = 0; i < body_nodes.getLength(); i++) {
                String[] valueArray = new String[2];
                body = body_nodes.item(i).getTextContent();
                date = date_nodes.item(i).getTextContent();
                valueArray[0] = date;
                valueArray[1] = body;
                hm_xmlbody.put(Integer.toString(i), valueArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        String[] valueArray = new String[2];
        valueArray[0] = date;
        valueArray[1] = body;
        
        int i = 0;
        hm_xmlbody.put(Integer.toString(i), valueArray);
        //System.out.println(body);
        return hm_xmlbody;
    }

    public void clear_hm() {
        hm_mail.clear();
        hm_xmlbody.clear();
        hm_keyword_match.clear();
    }

    public HashMap parseMailBody(String text) {
        //System.out.println(text);
        String[] strArray;
        String header = "";
        String sub = "";    // NOT USED CAN BE USED 
        String url = "";    // USED FOR CRAWLING
        String content;
        strArray = text.split("See all stories on this topic:");
        //System.out.println("----------------"+strArray.length);
        //logger.info("Length------> "+ text);
        for (int i = 0; i < strArray.length-1; i++) {
            String[] valueArray = new String[4];
            if(i>0){
                content = strArray[i].replaceFirst("<.*>", "");
            }else{
                content = strArray[i];
            }
            content = content.trim();
            content = content.replaceAll("=== News.*===/n", "");
            header = content.split("\n")[0];
            sub = content.split("\n")[1];
            if(header.indexOf("=== News") > -1){
                header = content.split("\n")[2];
                //System.out.println(header);
                sub = content.split("\n")[3];
            }    
            url = getMatch(content, "<http.*>");
            url = url.replaceAll("<","").replaceAll(">", "");
            content = content.replace(header, "").replace(url, "");
            String body = "";
            try{
                //Thread.sleep(1000);
            	//System.out.println("Gone for downloading......" + url);
                body = http.getNewsContent(url,content.substring(10,30));
            }catch(Exception e){
                e.printStackTrace();
            }
            //System.out.println(url);
            //logger.info(content);
            if(body.equals("")){
                body = content;
            }
            if ((content != null) && (header != null) && (url != null) && ((body != null))) {
                valueArray[0] = header;
                valueArray[1] = content;
                valueArray[2] = url;
                valueArray[3] = body;
                //System.out.println(body);
                hm_mail.put(Integer.toString(i), valueArray);
            }
        }
        
        return hm_mail;
    }

    public float getScore(HashMap hm, ArrayList pm_indexes, String text) {
        Iterator it_hm = hm.keySet().iterator();
        Iterator it_pm_idexes = pm_indexes.iterator();
        float score = 0;
        while (it_hm.hasNext()) {
            Object key = it_hm.next();
            float scale = Float.parseFloat(hm.get(key).toString());
            
            if (scale != 0) {
                //System.out.println(" " + key.toString() +"&&&&&&&&&&&");
                int index = text.toLowerCase().indexOf(" " + key.toString() + " ");
                if (index > -1) {
                    if (!hm_keyword_match.contains(key.toString())) {
                        //System.out.println(key.toString());
                        hm_keyword_match.add(key.toString());
                    }
                    if (pm_indexes.size() != 0) {
                        while (it_pm_idexes.hasNext()) {
                            float distance = 0;
                            Object ikey = it_pm_idexes.next();
                            int index_pm = Integer.parseInt(ikey.toString());
                            if (index > index_pm) {
                                distance = index - index_pm;
                                if (distance > 0) {
                                    score = score + scale / distance;
                                }
                            } else {
                                distance = index_pm - index;
                                if (distance > 0) {
                                    score = score + scale / distance;
                                }
                            }
                        }
                    } else {
                        score = score + scale / index;
                    }
                }
            }
        }
        return score;
    }

    public float getStockContextScore(HashMap eng_hm, HashMap stock_hm, String text) {
        Iterator it_eng = eng_hm.keySet().iterator();
        Iterator it_sc = stock_hm.keySet().iterator();

        float score = 0;
        while (it_sc.hasNext()) {
            Object sckey = it_sc.next();
            float sc_scale = Float.parseFloat(stock_hm.get(sckey).toString());
            if (sc_scale == 0) {
                int sc_index = text.toLowerCase().indexOf(" " + sckey.toString().trim() + " ");
                if (sc_index > -1) {
                    //System.out.println(sckey.toString());
                    while (it_eng.hasNext()) {
                        Object ekey = it_eng.next();
                        float e_scale = Float.parseFloat(eng_hm.get(ekey).toString());
                        int eindex = text.indexOf(" " + ekey.toString() + " ");
                        float distance = 0;
                        if (eindex > -1) {
                            if (sc_index > eindex) {
                                distance = sc_index - eindex;
                                if (distance > 0) {
                                    score = score + e_scale / distance;
                                }
                            } else {
                                distance = eindex - sc_index;
                                if (distance > 0) {
                                    score = score + e_scale / distance;
                                }
                            }
                        }
                    }
                }
            }
        }
        return score;
    }

    private String getMatch(String text, String strp) {

        pattern = Pattern.compile(strp);
        matcher = pattern.matcher(text);
        String match = "";
        if (matcher.find()) {
            match = matcher.group();
        }
        return match;
    }

    public static void main(String args[]) throws Exception{
        StructuredUnstructured s = new StructuredUnstructured();
        s.parseMailBody("Report Claims Microsoft Surface Priced at $499\nNASDAQ\nAfter months of speculation, Microsoft (NASDAQ: MSFT ) has reported\nannounced that it will charge $499 for the 32GB base model of its\nlong-awaited tablet. According to Time Techland , Microsoft will also sell\na device and keyboard combo for $599. For ...\n<http://community.nasdaq.com/News/2012-10/report-claims-microsoft-surface-priced-at-499.aspx?storyid=181821>\nSee all stories on this topic:\n<http://news.google.com/news/story?ncl=http://community.nasdaq.com/News/2012-10/report-claims-microsoft-surface-priced-at-499.aspx%\n3Fstoryid%3D181821&hl=en&geo=us>");
    }
}
