package nicelee.http.resource;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Pattern;

public class HttpResource {
	public final static long HOUR_MILLIS = 60*60*1000;
	public final static int DAY_MILLIS = 24*60*60*1000;
	public final static int HTTP_REQUEST_FIRST = 0;
	public final static int HTTP_REQUEST_AFTER_GET = 0x01;
	public final static int HTTP_REQUEST_AFTER_CONNECT = 0x02;
	public final static Pattern patternHeaders = Pattern.compile("^([^:]+):(.*)$");
	public final static Pattern patternHost = Pattern.compile("^ *([0-9|a-z|A-Z|.]+) *: *([0-9]*) *$");
	public final static Pattern patternFileRange = Pattern.compile("^bytes=([0-9]+)-([0-9]*)$");
	public final static Pattern patternURL = Pattern.compile("^/([^?^#]*)#?[^#]*\\??[^?^#]*$");
	public final static Pattern patternParent = Pattern.compile("^(/.*)/[^/]+/$|^(/)[^/]+/$");
	public final static Pattern patternSessionid = Pattern
			.compile("jsessionid *[=|:]{1} *YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXo");
	public final static SimpleDateFormat aDateFormat = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss");
																				// Thu, 31 Jan 2019 08:01:48 GMT || Thu Jan 2410:23:01 GMT 2019
	public final static SimpleDateFormat GMTDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'",Locale.US);
	public final static byte[] BREAK_LINE = "\r\n".getBytes();
	public final static byte[] PAGE_401 = "<html><head><title>Error</title></head><body><center><h1>401 Authorization Required</h1></center><hr><center>Copyright @Nicelee.top</center></body></html>"
			.getBytes();
	public final static byte[] PAGE_403 = "<html><head><title>Error</title></head><body><center><h1>403 Forbidden</h1></center><hr><center>Copyright @Nicelee.top</center></body></html>"
			.getBytes();
	public final static byte[] PAGE_404 = "<html><head><title>Error</title></head><body><center><h1>404 Not Found</h1></center><hr><center>Copyright @Nicelee.top</center></body></html>"
			.getBytes();
	public final static char[] WHITE_SPACES = "                                                ".toCharArray();
}
