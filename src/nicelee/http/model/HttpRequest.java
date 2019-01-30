package nicelee.http.model;

import java.util.HashMap;

public class HttpRequest {
	
	/**
	 * GET/POST/PUT/DELETE ...
	 */
	public String method;
	/**
	 * HTTP/1.1
	 */
	public String version;
	/**
	 * e.g. nicele.top:8080
	 */
	public String host;
	/**
	 * e.g. hello?say=nihao
	 */
	public String url;
	public HashMap<String, String> headers = new HashMap<>();
	
	public int dataLength = 0;
	public byte[] data = null;
	
	public void print() {
//		System.out.println(method + " "+ url + " " + version);
//		for(Entry<String, String> entry : headers.entrySet()) {
//			System.out.println("------>" +entry.getKey() + ": " + entry.getValue());
//		}
//		if(data != null)
//			System.out.println(data);
	}
}
