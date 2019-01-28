package nicelee.http.resource;

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class HttpResource {
	public final static Pattern patternHost = Pattern.compile("^ *([0-9|a-z|A-Z|.]+) *: *([0-9]*) *$");
	public final static Pattern patternFileRange = Pattern.compile("^bytes=([0-9]+)-([0-9]*)$");
	public final static Pattern patternURL = Pattern.compile("^/([^?^#]*)#?[^#]*\\??[^?^#]*$");
	public final static Pattern patternParent = Pattern.compile("^(/.*)/[^/]+/$|^(/)[^/]+/$");
	public final static Pattern patternSessionid = Pattern
			.compile("jsessionid *[=|:]{1} *YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXo");
	public final static SimpleDateFormat aDateFormat = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss");
	
	public final static byte[] BREAK_LINE = "\r\n".getBytes();
	public final static byte[] PAGE_401 = "<html><head><title>Error</title></head><body><center><h1>401 Authorization Required</h1></center><hr><center>Copyright @Nicelee.top</center></body></html>"
			.getBytes();
	public final static byte[] PAGE_403 = "<html><head><title>Error</title></head><body><center><h1>403 Forbidden</h1></center><hr><center>Copyright @Nicelee.top</center></body></html>"
			.getBytes();
	public final static byte[] PAGE_404 = "<html><head><title>Error</title></head><body><center><h1>404 Not Found</h1></center><hr><center>Copyright @Nicelee.top</center></body></html>"
			.getBytes();
	public final static char[] WHITE_SPACES = "                                                ".toCharArray();
}
