/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iiit.ire.mp;

/**
 *
 * @author messi
 */
import java.io.*;
import java.util.Iterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.jsoup.*;
import org.jsoup.nodes.Element;

import de.l3s.boilerpipe.extractors.ArticleExtractor;

public class HttpClientWrapper {

	private DefaultHttpClient httpclient;

	public HttpClientWrapper() {
		httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
		httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				5000);
	}

	public String getMarketValues(String url) {
		String response = "";
		try {
			HttpGet httpGet = new HttpGet(url);
			HttpResponse response1 = httpclient.execute(httpGet);
			if (response1.getStatusLine().getStatusCode() != 200) {
				httpGet.releaseConnection();
				return response;
			} else {
				HttpEntity entity1 = response1.getEntity();
				response = EntityUtils.toString(entity1);
			}
			httpGet.releaseConnection();
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
		return response;
	}

	public String getNewsContent(String url, String match) throws Exception {

		String response = match;
		try {
			HttpGet httpGet = new HttpGet(url);
			HttpResponse response1 = httpclient.execute(httpGet);

			if (response1.getStatusLine().getStatusCode() != 200) {
				httpGet.releaseConnection();
				return response;
			} else {
				HttpEntity entity1 = response1.getEntity();
				response = EntityUtils.toString(entity1);
			}
			httpGet.releaseConnection();
		} catch (Exception e) {
			// TODO Auto-generated catch block

		}
		response = clean(response, match);
		// System.out.println(clean(response));
		return response;
	}

	public String getContent(String url) throws Exception {

		String response = "";
		try {
			HttpGet httpGet = new HttpGet(url);
			HttpResponse response1 = httpclient.execute(httpGet);

			if (response1.getStatusLine().getStatusCode() != 200) {
				httpGet.releaseConnection();
				return response;
			} else {
				HttpEntity entity1 = response1.getEntity();
				response = EntityUtils.toString(entity1);
			}
			httpGet.releaseConnection();
		} catch (Exception e) {
			// TODO Auto-generated catch block

		}
		response = ArticleExtractor.INSTANCE.getText(response);
		
		// System.out.println(clean(response));
		return response;
	}
	
	
	private String clean(String data, String match)
			throws UnsupportedEncodingException {
		String response = "";
		match = match.replaceAll("\n", " ");
		Iterator ele = Jsoup.parse(data).getElementsContainingText(match)
				.iterator();
		while (ele.hasNext()) {
			Element e = (Element) ele.next();
			String text = e.text();
			if (text.indexOf(match) == 0)
				response = text;
			break;
		}
		return response;
	}

	public static void main(String args[]) throws Exception {
		HttpClientWrapper hcw = new HttpClientWrapper();
		/*String s = hcw
				.getNewsContent(
						"http://www.bizjournals.com/pacific/news/2013/01/02/hawaiian-telcom-holdco-stock-on-nasdaq.html",
						"Hawaiian Telcom Holdco Inc. said Wednesday that its stock is now being traded on the Nasdaq Global Select Market.");
		*/
		System.out.println(hcw.getMarketValues("http://ichart.yahoo.com/table.csv?s=ZUMZ&a=5&b=27&c=2013&d=5&e=27&f=2013&g=w&ignore=.csv"));
		//System.out.println(hcw.getMarketValues("http://ichart.yahoo.com/table.csv?s=GOOG&a=0&b=10&c=2012&d=0&e=10&f=2012&g=w&ignore=.csv"));
		hcw.getContent("http://www.nasdaq.com/article/weibo-prices-downsized-ipo-at-17-at-the-low-end-of-the-range-cm345076");
	}
}
