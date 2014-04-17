/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iiit.ire.mp;

/**
 *
 * @author messi
 */
import java.sql.*;
import java.util.HashMap;
import javax.sql.*;

public class FetchDBData {

    private static FetchDBData instance = null;
    private static HashMap hm_eng = new HashMap();
    private static HashMap hm_stock = new HashMap();
    private static HashMap hm_code_com = new HashMap();
    private Connection con;
    private String dbUrl;

    protected FetchDBData() {
        String ekey;
        String evalue;
        String skey;
        String svalue;
        dbUrl = "jdbc:mysql://localhost/nasdaq?user=root&password=123";
        String dbClass = "com.mysql.jdbc.Driver";
        String english_query = "Select name,scale FROM english_terms";
        String stock_query = "Select name,scale FROM stock_terms";
        String code_com_query = "Select code,company FROM company_code";
        try {

            Class.forName(dbClass);
            con = DriverManager.getConnection(dbUrl);
            Statement stmt = con.createStatement();
            ResultSet rs_eng = stmt.executeQuery(english_query);

            while (rs_eng.next()) {
                ekey = rs_eng.getString(1);
                evalue = rs_eng.getString(2);
                //System.out.println(ekey);
                //System.out.println(evalue);
                hm_eng.put(ekey.toLowerCase().trim(), evalue);
            } //end while

            ResultSet rs_stock = stmt.executeQuery(stock_query);

            while (rs_stock.next()) {
                skey = rs_stock.getString(1);
                svalue = rs_stock.getString(2);
                //System.out.println(skey);
                //System.out.println(svalue);
                hm_stock.put(skey.toLowerCase().trim(), svalue);
            } //end while

            ResultSet rs_code_com = stmt.executeQuery(code_com_query);

            while (rs_code_com.next()) {
                skey = rs_code_com.getString(2);
                svalue = rs_code_com.getString(1);
                //System.out.println(skey);
                //System.out.println(svalue);
                hm_code_com.put(skey.toLowerCase().trim(), svalue.toLowerCase().trim());
            } //end while
            //System.out.println(hm_code_com.toString());


            con.close();
        //System.out.println(hm_eng.toString());
        //System.out.println(hm_stock.toString());
        } //end try
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }  //end constructor

    public static synchronized FetchDBData getInstance() {
        if (instance == null) {
            instance = new FetchDBData();
        }
        return instance;
    }

    public static HashMap getHm_eng() {
        return hm_eng;
    }

    public static HashMap getHm_stock() {
        return hm_stock;
    }

    public static HashMap getHm_code_com() {
        return hm_code_com;
    }

    public String getMarketData(String code, String date) {
        String db_vals = "";
        try {
            con = DriverManager.getConnection(dbUrl);
            Statement stmt = con.createStatement();
            String query1 = "select sum(eng_score),sum(stock_score),sum(context_score) from stock_table where code='" + code + "' and date < '" + date + "'";
            //System.out.println(query1);
            ResultSet rs = stmt.executeQuery(query1);
            if(rs.next()){
                db_vals = rs.getString(1) + "," + rs.getString(2) + "," + rs.getString(3);
            }
            String query2 = "select sum(eng_score),sum(stock_score),sum(context_score) from market_table where date like'%" + date + "%'";
            //System.out.println(query2);
            Statement stmt2 = con.createStatement();
            ResultSet rs2 = stmt2.executeQuery(query2);
            if(rs2.next()){
                db_vals = db_vals + "," + rs2.getString(1) + "," + rs2.getString(2) + "," + rs2.getString(3);
            }    
        } catch (SQLException e) {
            e.printStackTrace();
        }
        db_vals = db_vals.replaceAll("null", "0");
        return db_vals;
    }
    
    public static void main(String args[]) {
    FetchDBData fb = new FetchDBData().getInstance();
    String date = "2012/10/22";
    String date_arr[] = date.split("/");
    //date = date.replaceAll("/","-");
    //System.out.println(date);
    //System.out.println(fb.getMarketData("DDDD","202-11-01"));
    }
}  //end class

