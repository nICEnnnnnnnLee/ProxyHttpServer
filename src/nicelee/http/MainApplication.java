package nicelee.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import nicelee.config.ConfigReader;
import nicelee.config.model.Config;
import nicelee.http.core.SocketServer;

public class MainApplication {

	public static void main(String[] args) throws IOException {
		figlet();
		// 1. 从配置文件app.config读取配置
		Config configs = ConfigReader.initConfigs();

		// 2. 启动服务端
		SocketServer ss = new SocketServer(configs);
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
