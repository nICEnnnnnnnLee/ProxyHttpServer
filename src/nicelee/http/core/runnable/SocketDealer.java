package nicelee.http.core.runnable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import nicelee.config.model.Config;
import nicelee.http.core.SocketServer;
import nicelee.http.core.file.transfer.common.CommonResponse;
import nicelee.http.model.HttpRequest;
import nicelee.http.resource.HttpResource;
import nicelee.http.util.StreamReader;

public class SocketDealer implements Runnable {

	Socket socketClient;
	Socket socketServer;

	// 与客户端之间的联系
	StreamReader in;
	BufferedOutputStream out;
	// 与服务器之间的联系
	StreamReader inFromSever;
	BufferedOutputStream outToServer;
	// Socket监视器
	SocketMonitor monitor;

	File srcFolder;
	int mode;
	int status = HttpResource.HTTP_REQUEST_FIRST;

	public SocketDealer(Socket socketClient, SocketMonitor monitor) {
		this.socketClient = socketClient;
		this.monitor = monitor;
	}

	public SocketDealer(Socket socketClient, SocketMonitor monitor, String source, int mode) {
		this.socketClient = socketClient;
		this.srcFolder = new File(source);
		this.monitor = monitor;
		this.mode = mode;
	}

	@Override
	public void run() {
		String url = "";
		try {
//			in = new BufferedInputStream(socketClient.getInputStream());
			in = new StreamReader(monitor, socketClient, new BufferedInputStream(socketClient.getInputStream()));
			out = new BufferedOutputStream(socketClient.getOutputStream());
			// writer = new BufferedWriter(new
			// OutputStreamWriter(socketClient.getOutputStream()));
			HttpRequest httpRequest;
			while ((httpRequest = in.readHttpRequestStructrue()) != null) {
				url = httpRequest.url;
				httpRequest.print();
				
				if(mode == Config.MODE_FILE_HTTP_SERVER) {
					CommonResponse.doResponseCommon(srcFolder, httpRequest, in, out);
					
				}else {
					if (httpRequest.method.toLowerCase().equals("connect")) {
						// System.out.println("调用的是connect 方法");
						doProxyConnect(httpRequest);
						break;
					} else {
						// System.out.println("调用的是普通GET/POST 方法");
						doProxyNormal(httpRequest);
					}
				}
			}
			
			if(mode == Config.MODE_PROXY_HTTP_SERVER) {
				
				// 直接转发TCP包, 不做任何处理
				// System.out.println("当前开始转发TCP包: ");
				while (true) {
					int length = in.read(in.readBuffer);
					// System.out.println("当前收到客户端包大小: " + length);
					outToServer.write(in.readBuffer, 0, length);
					outToServer.flush();
				}
			}

		} catch (SocketException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			// e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println(url + " -线程结束...");
			try {
				in.close();
			} catch (Exception e) {
			}
			try {
				out.close();
			} catch (Exception e) {
			}
			try {
				socketClient.close();
			} catch (Exception e) {
			}
			try {
				inFromSever.close();
			} catch (Exception e) {
			}
			try {
				outToServer.close();
			} catch (Exception e) {
			}
			try {
				socketServer.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * @param httpRequest
	 * @throws IOException
	 */
	private void doProxyConnect(HttpRequest httpRequest) throws IOException {
		// System.out.println("调用的是connect 方法");
		connecToServer(httpRequest);
		out.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
		out.flush();
	}

	/**
	 * @param httpRequest
	 * @throws IOException
	 */
	private void doProxyNormal(HttpRequest httpRequest) throws IOException {
		// TODO do something with the httpRequest. Put 'X-forward...', for example.
		httpRequest.headers.put("X-Forwarded-For", "123.123.123.123");
		// TODO do URLFilter

		// TODO give the client a response, proxy or http
		// CommonResponse.doResponseCommon(srcFolder, httpRequest, in, out);

		connecToServer(httpRequest);
		// 向服务器发送Http请求
		// System.out.println("发送Http请求... ");
		outToServer.write(
				String.format("%s %s %s\r\n", httpRequest.method, httpRequest.url, httpRequest.version).getBytes());
		outToServer.flush();
		outToServer.write(String.format("Host: %s\r\n", httpRequest.host).getBytes());
		for (Entry<String, String> entry : httpRequest.headers.entrySet()) {
			// TODO do some filter or change
			if (!entry.getKey().toLowerCase().contains("proxy") && !entry.getKey().toLowerCase().contains("forward")
					&& !entry.getKey().toLowerCase().contains("authorization")) {
				outToServer.write((entry.getKey() + ": " + entry.getValue()).getBytes());
				outToServer.write(HttpResource.BREAK_LINE);
			}
		}
		httpRequest.print();
		if (httpRequest.dataLength > 0) {

			outToServer.write(String.format("Content-Length: %d\r\n", httpRequest.dataLength).getBytes());
		}
		outToServer.write(HttpResource.BREAK_LINE);
		outToServer.flush();
		if (httpRequest.dataLength > 0) {
			int count = 0;
			int rSize = in.read(in.readBuffer);
			System.out.println(new String(in.readBuffer, 0, rSize));
			while (rSize < httpRequest.dataLength - count) {
				outToServer.write(in.readBuffer, 0, rSize);
				rSize = in.read(in.readBuffer);
				count += rSize;
			}
			outToServer.write(in.readBuffer, 0, httpRequest.dataLength - count);
		}
		outToServer.flush();
		// System.out.println("数据发送完毕...");
	}

	/**
	 * @param httpRequest
	 * @throws IOException
	 */
	private void connecToServer(HttpRequest httpRequest) throws IOException {
		String dstIp = httpRequest.host;
		int dstPort = 80;
		// connect方法, 从首行url获取参数
		if (httpRequest.method.toLowerCase().equals("connect")) {
			dstPort = 443;
			dstIp = httpRequest.url;
		}
		// 获取目的Host
		Matcher matcher = HttpResource.patternHost.matcher(dstIp);
		if (matcher.find()) {
			dstIp = matcher.group(1);
			dstPort = Integer.parseInt(matcher.group(2));
		}
		System.out.print("Host为:");
		System.out.print(dstIp);
		System.out.print(" ;ip为:");
		System.out.println(dstPort);
		if (socketServer == null) {
			socketServer = new Socket();
			socketServer.connect(new InetSocketAddress(dstIp, dstPort));
//					 socketServer.connect(new InetSocketAddress("127.0.0.1", 7778));

			// 获取服务器之间的输入输出流
			inFromSever = new StreamReader(monitor, socketServer,
					new BufferedInputStream(socketServer.getInputStream()));
			outToServer = new BufferedOutputStream(socketServer.getOutputStream());

			// 打开面向服务器的监听线程,专用于转发数据给客户端
			ProxyDealer proxyDealer = new ProxyDealer(this);
			SocketServer.httpProxyThreadPool.execute(proxyDealer);
		}
	}

	public Socket getSocketClient() {
		return socketClient;
	}

	public Socket getSocketServer() {
		return socketServer;
	}

	public StreamReader getIn() {
		return in;
	}

	public BufferedOutputStream getOut() {
		return out;
	}

	public StreamReader getInFromSever() {
		return inFromSever;
	}

	public BufferedOutputStream getOutToServer() {
		return outToServer;
	}

	public SocketMonitor getMonitor() {
		return monitor;
	}

}
