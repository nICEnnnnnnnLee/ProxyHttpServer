package nicelee.http.core.runnable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import nicelee.http.core.SocketServer;
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
	BufferedWriter outToServer;
	// Socket监视器
	SocketMonitor monitor;

	//File srcFolder;

	public SocketDealer(Socket socketClient, SocketMonitor monitor) {
		this.socketClient = socketClient;
		this.monitor = monitor;
	}
	
	public SocketDealer(Socket socketClient, SocketMonitor monitor, String source) {
		this.socketClient = socketClient;
		//this.srcFolder = new File(source);
		this.monitor = monitor;
	}

	@Override
	public void run() {
		// BufferedInputStream in = null;
//		BufferedOutputStream out = null;
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
				// TODO do something with the httpRequest. Put 'X-forward...', for example.
				httpRequest.headers.put("X-Forwarded-For", "123.123.123.123");
				// TODO do URLFilter

				// TODO give the client a response, proxy or http
				// CommonResponse.doResponseCommon(srcFolder, httpRequest, in, out);

				// 获取目的Host
				Matcher matcher = HttpResource.patternHost.matcher(httpRequest.host);
				String dstIp = null;
				int dstPort = 80;
				if (matcher.find()) {
					dstIp = matcher.group(1);
					dstPort = Integer.parseInt(matcher.group(2));
				} else {
					dstIp = httpRequest.host;
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
					outToServer = new BufferedWriter(new OutputStreamWriter(socketServer.getOutputStream()));

					// 打开面向服务器的监听线程,专用于转发数据给客户端
					ProxyDealer proxyDealer = new ProxyDealer(this);
					SocketServer.httpProxyThreadPool.execute(proxyDealer);
				}
				// 向服务器发送Http请求
				//System.out.println("发送Http请求... ");
				outToServer
						.write(String.format("%s %s %s\r\n", httpRequest.method, httpRequest.url, httpRequest.version));
				outToServer.flush();
				outToServer.write(String.format("Host: %s\r\n", httpRequest.host));
				for (Entry<String, String> entry : httpRequest.headers.entrySet()) {
					outToServer.write(entry.getKey() + ": " + entry.getValue());
					outToServer.write("\r\n");
				}
				httpRequest.print();
				if (httpRequest.dataLength > 0) {

					outToServer.write(String.format("Content-Length: %d\r\n", httpRequest.dataLength));
				}
				outToServer.write("\r\n");
				outToServer.flush();
				if (httpRequest.dataLength > 0) {
					int count = 0;
					int rSize = in.read(in.readBuffer);
					while (rSize < httpRequest.dataLength - count) {
						outToServer.write(new String(in.readBuffer, 0, rSize));
						rSize = in.read(in.readBuffer);
						count += rSize;
					}
					outToServer.write(new String(in.readBuffer, 0, httpRequest.dataLength - count));
				}
				outToServer.flush();
				//System.out.println("数据发送完毕...");

			}

		} catch (SocketException e) {
		} catch (IOException e) {
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

	public Socket getSocketClient() {
		return socketClient;
	}

	public void setSocketClient(Socket socketClient) {
		this.socketClient = socketClient;
	}

	public Socket getSocketServer() {
		return socketServer;
	}

	public void setSocketServer(Socket socketServer) {
		this.socketServer = socketServer;
	}

	public StreamReader getIn() {
		return in;
	}

	public void setIn(StreamReader in) {
		this.in = in;
	}

	public BufferedOutputStream getOut() {
		return out;
	}

	public void setOut(BufferedOutputStream out) {
		this.out = out;
	}

	public StreamReader getInFromSever() {
		return inFromSever;
	}

	public void setInFromSever(StreamReader inFromSever) {
		this.inFromSever = inFromSever;
	}

	public BufferedWriter getOutToServer() {
		return outToServer;
	}

	public void setOutToServer(BufferedWriter outToServer) {
		this.outToServer = outToServer;
	}

	public SocketMonitor getMonitor() {
		return monitor;
	}

	public void setMonitor(SocketMonitor monitor) {
		this.monitor = monitor;
	}

}
