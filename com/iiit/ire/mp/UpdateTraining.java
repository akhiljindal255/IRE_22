/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iiit.ire.mp;

/**
 *
 * @author messi
 */
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mysql.jdbc.PreparedStatement;

public class UpdateTraining {

	private String dbUrl = "jdbc:mysql://localhost/nasdaq?user=root&password=123";
	private String dbClass = "com.mysql.jdbc.Driver";
	private HashMap hm_code_id;
	private static HashMap hm_eng_keywords;
	private static HashMap hm_code_com;
	private static HashMap hm_stock_keywords;
	private Calendar date = Calendar.getInstance();
	// private Connection con;
	private HttpClientWrapper hcw;

	protected UpdateTraining() {
		hcw = new HttpClientWrapper();
	}

	public void putActualValues() {
		Connection con = GetDBConnection.getInstance().con;
		try {
			String query_train = "INSERT INTO training_table2 (comp_eng_score,comp_stock_score,comp_con_score,comp_senti_score,mkt_eng_score,mkt_stock_score,mkt_con_score,mkt_senti_score,percent_change,movement) VALUES (?,?, ?, ?,?, ?, ?,?,?,?)";
			PreparedStatement ps_train = (PreparedStatement) con
					.prepareStatement(query_train);
			String query = "select code,date,avg(eng_score),avg(stock_score),avg(context_score),avg(senti)  from stock_table where date between '2014-01-01' and '2014-03-31' group by code,date ";
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				String code = rs.getObject(1).toString();
				String date = rs.getObject(2).toString();
				String[] date_split = date.split("-");
				String year = date_split[0];
				String month = trim_day_month(date_split[1]);
				String day = trim_day_month(date_split[2]);
				int change = 0;
				if (isLastDay(year, month, day)) {
					month = Integer.toString((Integer.parseInt(month) + 1));
					day = Integer.toString(1);
				}
				change = isWeekend(year, month, day);
				if (change > 0) {
					day = Integer.toString((Integer.parseInt(day) + change));
				}
				String y_query = "http://ichart.yahoo.com/table.csv?s=" + code
						+ "&a=" + (Integer.parseInt(month) - 1) + "&b=" + day
						+ "&c=" + year + "&d=" + (Integer.parseInt(month) - 1)
						+ "&e=" + day + "&f=" + year + "&g=w&ignore=.csv";
				
				
				String result = hcw.getMarketValues(y_query);
				String movement[] = getActualValues(result);
				if (!movement[0].equals("")) {
					query = "select avg(eng_score),avg(stock_score),avg(context_score),avg(senti) from market_table where date='"
							+ date + "'";
					stmt = con.createStatement();
					ResultSet rs_SELECT = stmt.executeQuery(query);
					String es_s = rs.getObject(3).toString();
					String ss_s = rs.getObject(4).toString();
					String cs_s = rs.getObject(5).toString();
					String sm_s = rs.getObject(6).toString();
					while (rs_SELECT.next()) {
						String es_m = rs_SELECT.getObject(1).toString();
						String ss_m = rs_SELECT.getObject(2).toString();
						String cs_m = rs_SELECT.getObject(3).toString();
						String sm_m = rs_SELECT.getObject(4).toString();
							ps_train.setObject(1, es_s);
							ps_train.setObject(2, ss_s);
							ps_train.setObject(3, cs_s);
							ps_train.setObject(4, sm_s);
							ps_train.setObject(5, es_m);
							ps_train.setObject(6, ss_m);
							ps_train.setObject(7, cs_m);
							ps_train.setObject(8, sm_m);
							ps_train.setObject(9, Float.parseFloat(movement[0]));
							ps_train.setObject(10, movement[1]);
							System.out.println("Dumping into DB..");
							ps_train.executeUpdate();
							System.out.println("Finished Dumping..");
					}
				}
			}

			/*
			 * String query3 =
			 * "update prediction_table set stock_value_change='" + svc +
			 * "' ,actual='"+actual + "' where pred_date='" + date +
			 * "' and u_code='" + code + "'"; //System.out.println(query3);
			 * PreparedStatement preparedStmt = con.prepareStatement(query3);
			 * preparedStmt.execute();
			 */
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String trim_day_month(String val) {
		if (val.indexOf("0") == 0) {
			val = val.replaceFirst("0", "");
		}
		return val;
	}

	public int isWeekend(String year, String month, String day) {

		date.set(Integer.parseInt(year), Integer.parseInt(month) - 1,
				Integer.parseInt(day));
		int reply = 0;
		if (date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
			reply = 1;
		if (date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
			reply = 2;
		return reply;
	}

	public boolean isLastDay(String year, String month, String day) {
		date.set(Integer.parseInt(year), Integer.parseInt(month) - 1,
				Integer.parseInt(day));
		int noOfLastDay = date.getActualMaximum(Calendar.DAY_OF_MONTH);
		if (noOfLastDay == Integer.parseInt(day))
			return true;
		return false;
	}

	private String[] getActualValues(String results) {
		// Date,Open,High,Low,Close,Volume,Adj Close
		String[] market_change = new String[2];
		market_change[0] = "";
		market_change[1] = "";
		results = results.replace(
				"Date,Open,High,Low,Close,Volume,Adj Close\n", "");
		if (!results.isEmpty()) {
			String[] res = results.split(",");
			String open_val = getMatch("\\d+\\.\\d+",res[1]);
			String close_val = getMatch("\\d+\\.\\d+",res[6]);
			double change = Double.parseDouble(close_val)
					- Double.parseDouble(open_val);
			change = change / Double.parseDouble(open_val) * 100;
			market_change[0] = Double.toString(change);
			if (change > 0) {
				market_change[1] = "UP";
			} else if(change < 0){
				market_change[1] = "DOWN";
			}
		}
		return market_change;
	}

	private String getMatch(String str_pattern,String str) {
		Pattern pattern = Pattern.compile(str_pattern);
	    Matcher matcher = pattern.matcher(str);
	    if (matcher.find()){
	    	return matcher.group(); 
	    }
	    return "";
	}

	public static void main(String args[]) {
		UpdateTraining ut = new UpdateTraining();
		ut.putActualValues();
	}
}
