package nicelee.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import nicelee.http.config.ConfigReader;
import nicelee.http.core.SocketServer;

public class MainApplication {

	public static void main(String[] args) throws IOException {
		figlet();
		// 1. 从配置文件app.config读取配置
		ConfigReader.initConfigs();
		int portServerListening = ConfigReader.getInt("nicelee.server.port");
		int threadPoolSize = ConfigReader.getInt("nicelee.server.fixedPoolSize");
		//String sourceLocation = ConfigReader.getString("nicelee.server.source");
		long socketTimeout = ConfigReader.getLong("nicelee.server.socketTimeout");

		// 2. 启动服务端
		SocketServer ss = new SocketServer(portServerListening, threadPoolSize, socketTimeout);
		ss.startServer();
		
	}

	static void figlet() {
		BufferedReader bu = null;
		try {
			InputStream in = MainApplication.class.getResourceAsStream("/resources/console.display");
			bu = new BufferedReader(new InputStreamReader(in));
			String gg;
			while ((gg = bu.readLine()) != null) {
				System.out.println(gg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bu.close();
			} catch (Exception e) {
			}
		}

	}
}
