package nicelee.config.model;


public class Config {
	public static final int MODE_FILE_HTTP_SERVER = 0;
	public static final int MODE_PROXY_HTTP_SERVER = 1;
	public int mode;
	public int portServerListening;
	public int threadPoolSize;
	public String sourceLocation;
	public long socketTimeout;
}
