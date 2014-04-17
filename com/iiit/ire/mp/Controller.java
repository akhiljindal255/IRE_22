/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iiit.ire.mp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.mysql.jdbc.PreparedStatement;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

/**
 * 
 * @author messi
 */
public class Controller {

	private GATEImplementation gate_imple;
	private StructuredUnstructured parser;
	private FetchDBData fetch_db_data;
	private HashMap mailhm;
	private ArrayList percent_money_indexes;
	private ArrayList arr_entity_score;
	private Properties props;
	private StanfordCoreNLP pipeline;
	private ArrayList arrScores;
	// private static Logger logger;

	public Controller() {
		gate_imple = new GATEImplementation();
		parser = new StructuredUnstructured();
		mailhm = new HashMap();
		percent_money_indexes = new ArrayList();
		arr_entity_score = new ArrayList();
		fetch_db_data = new FetchDBData().getInstance();
		props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        pipeline = new StanfordCoreNLP(props);
        arrScores = new ArrayList<String>();
        
	}

	/*
	 * static { try { boolean append = true; FileHandler fh = new
	 * FileHandler("/usr/local/TestLog1.log", append); //fh.setFormatter(new
	 * XMLFormatter()); fh.setFormatter(new SimpleFormatter()); logger =
	 * Logger.getLogger("com.hcl.entityscore.Controller");
	 * logger.addHandler(fh); } catch (IOException e) { e.printStackTrace(); } }
	 */
	private ArrayList getArrEntity(String date, String body) throws Exception {
		// System.out.println(xml);
		HashMap xmlhm = getparsedXML(date, body);
		Iterator it_xmlhm = xmlhm.keySet().iterator();
		String[] date_body;
		String[] header_content_url;
		// logger.info(body);
		while (it_xmlhm.hasNext()) {
			Object key = it_xmlhm.next();
			date_body = (String[]) xmlhm.get(key);

			// mailhm = parser.parseMailBody(date_body[1],logger);
			//System.out.println("Gone for mail parsing ......");
			mailhm = parser.parseMailBody(date_body[1]);

			Iterator it_mailhm = mailhm.keySet().iterator();
			while (it_mailhm.hasNext()) {
				ArrayList arr_com = new ArrayList();
				ArrayList arr_stock = new ArrayList();
				Object mkey = it_mailhm.next();
				header_content_url = (String[]) mailhm.get(mkey);
				//System.out.println("Gone for entity extraction ......");
				HashMap hm_header_entities = gate_imple
						.getEntities(header_content_url[0]);
				setOrganization(hm_header_entities, arr_com);
				setStock_Code(hm_header_entities, arr_stock,
						header_content_url[0]);
				setMoneyIndexes(hm_header_entities);
				setPercentIndexes(hm_header_entities);
				gate_imple.clear_hm();
				//System.out.println("Gone for entity extraction 2 ......");
				HashMap hm_content_entities = gate_imple
						.getEntities(header_content_url[3]);
				setOrganization(hm_content_entities, arr_com);
				if (arr_stock.size() == 0) {
					setStock_Code(hm_content_entities, arr_stock,
							header_content_url[3]);
				}
				setMoneyIndexes(hm_content_entities);
				setPercentIndexes(hm_content_entities);
				gate_imple.clear_hm();
				// Added 10th Dec
				String header_content = header_content_url[0] + " "
						+ header_content_url[3];

				float eng_score = parser.getScore(fetch_db_data.getHm_eng(),
						percent_money_indexes, header_content);
				float stock_score = parser.getScore(
						fetch_db_data.getHm_stock(), percent_money_indexes,
						header_content);
				float context_score = parser.getStockContextScore(
						fetch_db_data.getHm_eng(), fetch_db_data.getHm_stock(),
						header_content);
				
				
				HashMap hm = new HashMap();
				hm.put("Header", header_content_url[0]);
				hm.put("Company", arr_com);
				hm.put("Code", arr_stock);
				hm.put("Eng_Score", eng_score);
				hm.put("Stock_Score", stock_score);
				hm.put("Context_Score", context_score);
				hm.put("Keywords", parser.getHm_keyword_match().toString());
				hm.put("Url", header_content_url[2]);
				hm.put("Date", date_body[0]);
				hm.put("Senti",Integer.toString(findSentiment(header_content)));
				arr_entity_score.add(hm);
				parser.setHm_keyword_match();
				percent_money_indexes.clear();

			}
		}
		return arr_entity_score;
	}

	public String getStockMarketData(String code, String date) {
		return fetch_db_data.getMarketData(code, date);
	}

	private void setMoneyIndexes(HashMap hm_entities) {
		if (hm_entities.containsKey("Money")) {
			Iterator it = ((ArrayList) hm_entities.get("Money")).iterator();
			while (it.hasNext()) {
				String money_index = ((String[]) it.next())[1].trim();
				percent_money_indexes.add(money_index);
			}
		}
	}

	private void setPercentIndexes(HashMap hm_entities) {
		if (hm_entities.containsKey("Percent")) {
			Iterator it = ((ArrayList) hm_entities.get("Percent")).iterator();
			while (it.hasNext()) {
				String percent_index = ((String[]) it.next())[1].trim();
				percent_money_indexes.add(percent_index);
			}
		}
	}

	private void setOrganization(HashMap hm_entities, ArrayList arr_com) {
		if (hm_entities.containsKey("Organization")) {
			Iterator it = ((ArrayList) hm_entities.get("Organization"))
					.iterator();
			while (it.hasNext()) {
				String company = ((String[]) it.next())[0].trim();
				if (!arr_com.contains(company)
						&& !(company.toLowerCase().equals("nasdaq"))) {
					arr_com.add(company);
				}
			}
		}
	}
	
	private void setStock_Code(HashMap hm_entities, ArrayList arr_stock,
			String text) {

		if (hm_entities.containsKey("Stock_Code")) {
			Iterator its = ((ArrayList) hm_entities.get("Stock_Code"))
					.iterator();
			while (its.hasNext()) {
				String stock_code = ((String[]) its.next())[0].trim();
				if (!arr_stock.contains(stock_code)
						&& (text.indexOf(stock_code) < 100)) {
					arr_stock.add(stock_code);
				}
			}
		}

	}

	public void clear_hm() {
		mailhm.clear();
		arr_entity_score.clear();
		percent_money_indexes.clear();
		arrScores.clear();
	}
	

	public String getNamedEntities(String text) throws Exception {
		String result = "";
		result = gate_imple.getAnnotatedText(text);

		result = result.replaceAll("<", "&lt;").replace(">", "&gt;");

		gate_imple.clear_hm();
		return result;
	}

	public ArrayList getEntityXML(String date, String body) throws Exception {
		ArrayList arr = getArrEntity(date, body);
		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		Element root = doc.createElement("root");
		doc.appendChild(root);
		// System.out.println(arr.toString());

		for (int i = 0; i < arr.size(); i++) {
			HashMap hmval = ((HashMap) arr.get(i));
			ArrayList arr_code = ((ArrayList) hmval.get("Code"));
			ArrayList arr_com = ((ArrayList) hmval.get("Company"));

			if (!(arr_code.size() == 1)) {
				if (arr_code.size() == 0 && arr_com.size() > 0) {
					arr_code = getCode(arr_com, fetch_db_data.getHm_code_com());
				}
				if (arr_code.size() == 0) {
					arr_code.add("Market");
				}
				hmval.remove("Code");
				hmval.put("Code", arr_code);
			}
		}
		
		return arr;
	}

	private ArrayList getCode(ArrayList company, HashMap hm_code_com) {
		int size = company.size();
		ArrayList code = new ArrayList();
		for (int i = 0; i < size; i++) {
			if (hm_code_com.containsKey(company.get(0))) {
				code.add(hm_code_com.get(company.get(0)).toString());
			}
		}
		return code;
	}
	// 1- 25 scale
	public int findSentiment(String line) {
		int mainSentiment = 0; 
        line = line.replaceAll("\n","");
		if (line != null && line.length() > 0) {
            Annotation annotation = pipeline.process(line);
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                mainSentiment = mainSentiment + sentiment;
            }
        }
        return mainSentiment;
    }
	
	private HashMap getparsedXML(String date, String body) {
		// System.out.println(body);
		HashMap xmlhm = parser.getXMLDetails(date, body);
		return xmlhm;
	}

	private Element getXMLChild(Document doc, String element, String val) {
		Element child = doc.createElement(element);
		Text text = doc.createTextNode(val);
		child.appendChild(text);
		return child;
	}

	public void processOutput(ArrayList arr,GetDBConnection gb) throws SQLException {
		
		String query_mkt = "INSERT INTO market_table (code, header, company, eng_score, stock_score,context_score,senti,keywords,url,date) VALUES (?, ?, ?,?, ?, ?,?, ?, ?,?)";
		String query_stock = "INSERT INTO stock_table (code, header, company, eng_score, stock_score,context_score,senti,keywords,url,date) VALUES (?, ?, ?,?, ?, ?,?, ?, ?,?)";
		
		PreparedStatement ps_mkt = (PreparedStatement) gb.con.prepareStatement(query_mkt);
		PreparedStatement ps_stk = (PreparedStatement) gb.con.prepareStatement(query_stock);
		
		for (int i = 0; i < arr.size(); i++) {
			HashMap hmval = ((HashMap) arr.get(i));
			ArrayList arr_code = (ArrayList) hmval.get("Code");
			for (int j = 0; j < arr_code.size(); j++) {
				String header = hmval.get("Header").toString();
				String company = hmval.get("Company").toString();
				String eng_score = hmval.get("Eng_Score").toString();
				String stock_score = hmval.get("Stock_Score").toString();
				String context_score = hmval.get("Context_Score").toString();
				String keywords = hmval.get("Keywords").toString();
				String url = hmval.get("Url").toString();
				String date = hmval.get("Date").toString();
				String senti = hmval.get("Senti").toString(); 
				if(arr_code.get(j).toString().equals("Market")){
					ps_mkt.setObject(1,"Market");
					ps_mkt.setObject(2,header);
					ps_mkt.setObject(3,company);
					ps_mkt.setObject(4,eng_score);
					ps_mkt.setObject(5,stock_score);
					ps_mkt.setObject(6,context_score);
					ps_mkt.setObject(7,senti);
					ps_mkt.setObject(8,keywords);
					ps_mkt.setObject(9,url);
					ps_mkt.setObject(10,date);
					ps_mkt.executeUpdate();
				}else{
					ps_stk.setObject(1,arr_code.get(j).toString());
					ps_stk.setObject(2,header);
					ps_stk.setObject(3,company);
					ps_stk.setObject(4,eng_score);
					ps_stk.setObject(5,stock_score);
					ps_stk.setObject(6,context_score);
					ps_stk.setObject(7,senti);
					ps_stk.setObject(8,keywords);
					ps_stk.setObject(9,url);
					ps_stk.setObject(10,date);
					ps_stk.executeUpdate();
				}
				
			}
			
			
		}
		
		clear_hm();
	}

	public ArrayList<String> getScores(String body) throws Exception{
		ArrayList arr_com = new ArrayList();
		ArrayList arr_stock = new ArrayList();
		HashMap hm_entities = gate_imple
				.getEntities(body);
		setOrganization(hm_entities, arr_com);
		setStock_Code(hm_entities, arr_stock,
				body);
		setMoneyIndexes(hm_entities);
		setPercentIndexes(hm_entities);
		float eng_score = parser.getScore(fetch_db_data.getHm_eng(),
				percent_money_indexes, body);
		float stock_score = parser.getScore(
				fetch_db_data.getHm_stock(), percent_money_indexes,
				body);
		float context_score = parser.getStockContextScore(
				fetch_db_data.getHm_eng(), fetch_db_data.getHm_stock(),
				body);
		int senti = findSentiment(body);
		ArrayList<String> arr = new ArrayList<String>();
		String code = "Market";
		if(arr_stock.size()>0){
			code = arr_stock.get(0).toString();
		}
		arr.add(Float.toString(eng_score));
		arr.add(Float.toString(stock_score));
		arr.add(Float.toString(context_score));
		arr.add(Integer.toString(senti));
		arr.add(code);
		gate_imple.clear_hm();
		return arr;
	}
	
	
	public static void main(String args[]) throws Exception {
		Controller c = new Controller();
		//System.out.println(c.findSentiment("He is the best boy in the class. He is the worst fighter though"));
		GetDBConnection fb = new GetDBConnection().getInstance();
		String date1 = args[0];
		String date2 = args[1];
		/*date1 = "2013-01-01";
		date2 = "2013-01-02";
		*/
		String query = "select Date,Body from mail_table where Date between '"+date1+"' and '"+ date2+"'";
		Statement stmt = fb.con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        String date;
        String body;
    	long start_time = System.currentTimeMillis();
        while (rs.next()) {
            date = rs.getString(1);
            body = rs.getString(2);
            ArrayList arr = c.getEntityXML(date, body);
            System.out.println(date + " " + arr.size());
            c.processOutput(arr,fb);
            c.clear_hm();
        }
        long end_time = System.currentTimeMillis();
		System.out.println("TIME TAKEN " + (end_time - start_time)/1000 + " secs");
	}
}
