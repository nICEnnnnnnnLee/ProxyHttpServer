package nicelee.http.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nicelee.config.model.Config;
import nicelee.http.core.runnable.SocketDealer;
import nicelee.http.core.runnable.SocketMonitor;

public class SocketServer {
	
	//Configs
	int mode;
	int portServerListening;
	String sourceLocation;
	long socketTimeout;
	
	boolean isRun = true;
	public static ExecutorService httpThreadPool;
	public static ExecutorService httpProxyThreadPool;
	ServerSocket serverSocket;
	
	public SocketServer(int portServerListening, int threadPoolSize, long socketTimeout, String sourceLocation) {
		this.portServerListening = portServerListening;
		httpThreadPool = Executors.newFixedThreadPool(threadPoolSize);
		httpProxyThreadPool = Executors.newFixedThreadPool(threadPoolSize);
		this.sourceLocation = sourceLocation;
		this.socketTimeout = socketTimeout;
	}
	public SocketServer(Config config) {
		this.portServerListening = config.portServerListening;
		httpThreadPool = Executors.newFixedThreadPool(config.threadPoolSize);
		httpProxyThreadPool = Executors.newFixedThreadPool(config.threadPoolSize);
		this.sourceLocation = config.sourceLocation;
		this.socketTimeout = config.socketTimeout;
		this.mode = config.mode;
	}
	
	/**
	 *  关闭服务器
	 */
	public void stopServer() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("正在关闭 SocketServer: 服务器... ");
	}
	
	/**
	 *  打开服务器
	 */
	public void startServer() {
		Socket socket = null;
		System.out.println("SocketServer: 服务器监听开始... ");
		try {
			serverSocket = new ServerSocket(portServerListening);
			//serverSocket.setSoTimeout(300000);
			
			//开启Socket监控进程, 时间长于socketTimeout的将会被打断
			SocketMonitor monitor = new SocketMonitor(socketTimeout);
			Thread th = new Thread(monitor);
			th.start();
			while (isRun) {
				try {
					socket = serverSocket.accept();
				}catch (SocketTimeoutException e) {
					continue;
				}catch (SocketException e) {
					break;
				}
				
				//System.out.println("收到新连接: " + socket.getInetAddress() + ":" + socket.getPort());
				SocketDealer dealer = new SocketDealer(socket, monitor, sourceLocation, mode);
				httpThreadPool.execute(dealer);
				//monitor.put(socket); 改为在线程里面刷新时间(每次Http Request刷新一遍)
			}
			httpThreadPool.shutdownNow();
			th.interrupt();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
				}
			}
		}
		System.out.println("SocketServer: 服务器已经关闭... ");
	}
}
