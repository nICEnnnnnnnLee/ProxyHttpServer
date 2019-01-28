package nicelee.http.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
//import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nicelee.http.MainApplication;

public class ConfigReader {
	final static Pattern patternConfig = Pattern.compile("^[ ]*([0-9|a-z|A-Z|.|_]+)[ ]*=[ ]*([^ ]+.*$)");

	/**
	 * 返回key 所对应的value; 如无设置,返回0
	 * 
	 * @param property
	 * @return
	 */
	public static int getInt(String property) {
		try {
			return Integer.parseInt(System.getProperty(property));
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * 返回key 所对应的value; 如无设置,返回0
	 * 
	 * @param property
	 * @return
	 */
	public static long getLong(String property) {
		try {
			return Long.parseLong(System.getProperty(property));
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * 返回key 所对应的value; 如无默认设置,返回null
	 * 
	 * @param property
	 * @return
	 */
	public static String getString(String property) {
		return System.getProperty(property);
	}

	public static void initConfigs() {
		// 先初始化默认值
		BufferedReader buReader = null;
		try {
			InputStream in = MainApplication.class.getResourceAsStream("/resources/app.config");
			buReader = new BufferedReader(new InputStreamReader(in));
			String config;
			while ((config = buReader.readLine()) != null) {
				Matcher matcher = patternConfig.matcher(config);
				if (matcher.find()) {
					System.setProperty(matcher.group(1), matcher.group(2).trim());
					//System.out.printf("  key-->value:  %s --> %s\r\n", matcher.group(1), matcher.group(2));
				}
			}
			buReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		//从配置文件读取
		buReader = null;
		System.out.println("----Config init begin...----");
		try {
			buReader = new BufferedReader(new FileReader("app.config"));
			String config;
			while ((config = buReader.readLine()) != null) {
				Matcher matcher = patternConfig.matcher(config);
				if (matcher.find()) {
					System.setProperty(matcher.group(1), matcher.group(2).trim());
					System.out.printf("  key-->value:  %s --> %s\r\n", matcher.group(1), matcher.group(2));
				}
			}
		} catch (IOException e) {
			// e.printStackTrace();
		} finally {
			try {
				buReader.close();
			} catch (Exception e) {
			}
		}
		System.out.println("----Config ini end...----");
	}
}
