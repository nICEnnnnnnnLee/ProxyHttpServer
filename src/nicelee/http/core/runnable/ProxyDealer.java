package nicelee.http.core.runnable;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

import nicelee.http.util.StreamReader;

/**
 * 将服务器的数据转发给客户端
 * 
 * @author LiJia
 *
 */
public class ProxyDealer implements Runnable {
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

	public ProxyDealer(SocketDealer socketDealer) {
		socketClient = socketDealer.getSocketClient();
		socketServer = socketDealer.getSocketServer();
		in = socketDealer.getIn();
		out = socketDealer.getOut();
		inFromSever = socketDealer.getInFromSever();
		outToServer = socketDealer.getOutToServer();
		monitor = socketDealer.getMonitor();
		monitor.put(socketServer);
	}
	
	@Override
	public void run() {
		try {
			
			int len = inFromSever.read(inFromSever.readBuffer);
			while (len > -1) {
				//放入监控队列, 等待timeout时间超时后关闭Socket, 此时会抛出异常, 进而结束整个线程
				//Socket关闭后, 对应的两个Dealer线程均会抛出异常关闭
				monitor.put(socketServer);
				//System.out.println("从服务器收到请求: ");
				//System.out.println(new String(inFromSever.readBuffer, 0, len));
				out.write(inFromSever.readBuffer, 0, len);
				out.flush();
				len = inFromSever.read(inFromSever.readBuffer);
			}
		} catch (IOException e) {
			//e.printStackTrace();
		} finally {
			System.out.println(socketServer.getInetAddress() + " Proxy-线程结束...");
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

}
