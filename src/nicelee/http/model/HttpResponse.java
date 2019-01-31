package nicelee.http.model;

import java.util.HashMap;

public class HttpResponse {
	public final static String HTTP_VERSION_1_1 = "HTTP/1.1";
	public final static int HTTP_STATUS_200 = 200;
	public final static int HTTP_STATUS_206 = 206;
	public final static int HTTP_STATUS_304 = 304;
	public final static int HTTP_STATUS_404 = 404;
	public final static int HTTP_STATUS_403 = 403;
	public final static int HTTP_STATUS_401 = 401;
	public final static String HTTP_STATUS_Desc_200_OK = "OK";
	public final static String HTTP_STATUS_Desc_206_Part = "Partial Content";
	public final static String HTTP_STATUS_Desc_304_NOT_MODIFIED = "Not Modified";
	public final static String HTTP_STATUS_Desc_401_NOT_AUTH = "Authorization Required";
	public final static String HTTP_STATUS_Desc_404_NOT_FOUND = "Not Found";
	public final static String HTTP_STATUS_Desc_403_FORBBIDEN = "Forbidden";
	public final static String HTTP_CONTENT_TYPE_HTML = "text/html";
	public final static String HTTP_CONTENT_TYPE_JS = "application/x-javascript";
	public final static String HTTP_CONTENT_TYPE_JSON = "application/json";
	public final static String HTTP_CONTENT_TYPE_CSS = "text/css";
	public final static String HTTP_CONTENT_TYPE_JPG = "image/jpeg";
	public final static String HTTP_CONTENT_TYPE_ICO = "image/x-icon";
	public final static String HTTP_CONTENT_TYPE_FONT = "application/x-font-woff";
	public final static String HTTP_CONTENT_TYPE_MP4 = "video/mp4";
	public final static String HTTP_CONTENT_TYPE_MP3 = "audio/mp3";
	public final static String HTTP_CONTENT_TYPE_FLV = "video/x-flv";
	public final static String HTTP_CONTENT_TYPE_FILE = "application/octet-stream";
	
	/**
	 * HTTP/1.1
	 */
	public String version = HTTP_VERSION_1_1;
	/**
	 * e.g. 200, 404
	 */
	public int status = HTTP_STATUS_200;
	/**
	 * e.g. OK, Not Found, Forbidden
	 */
	public String statusDescript = HTTP_STATUS_Desc_200_OK;
	/**
	 * GMT String
	 */
	public String date;
	/**
	 * text/html, application/x-javascript, application/json, image/webp, application/x-font-woff, video/mp4
	 */
	public String contentType = HTTP_CONTENT_TYPE_HTML;//; charset=utf-8
	public HashMap<String, String> headers = new HashMap<String, String>();
	
	public int dataLength = 0;
	
	public HttpResponse do200() {
		return this;
	}
	
	public HttpResponse do206() {
		status = HTTP_STATUS_206;
		statusDescript = HTTP_STATUS_Desc_206_Part;
		return this;
	}
	public HttpResponse do304() {
		status = HTTP_STATUS_304;
		statusDescript = HTTP_STATUS_Desc_304_NOT_MODIFIED;
		return this;
	}
	public HttpResponse do404() {
		status = HTTP_STATUS_404;
		statusDescript = HTTP_STATUS_Desc_404_NOT_FOUND;
		return this;
	}
	
	public HttpResponse do403() {
		status = HTTP_STATUS_403;
		statusDescript = HTTP_STATUS_Desc_403_FORBBIDEN;
		return this;
	}

	public HttpResponse do401() {
		status = HTTP_STATUS_401;
		statusDescript = HTTP_STATUS_Desc_401_NOT_AUTH;
		return this;
	}
	
	public void print() {
		
//		System.out.println("返回Response结果: ");
//		System.out.println(version + " "+ status + " " + statusDescript);
//		System.out.println("Date: "+ date);
//		System.out.println("Content-Type: "+ contentType);
//		System.out.println("Content-Length: "+ dataLength);
//		for(Entry<String, String> entry : headers.entrySet()) {
//			System.out.println(entry.getKey() + ": " + entry.getValue());
//		}
//		if(data != null)
//			System.out.println(data);
	}
}
