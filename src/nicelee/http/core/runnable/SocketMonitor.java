package nicelee.http.core.runnable;

import java.io.IOException;
import java.net.Socket;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class SocketMonitor implements Runnable{

	static ConcurrentHashMap<Socket, Long> socketMap = new ConcurrentHashMap<>();
	
	long socketTimeout = 60000;
	
	public SocketMonitor(long socketTimeout) {
		this.socketTimeout = socketTimeout;
	}
	
	public void put(Socket socket) {
		//System.out.println("socketMap放入/更新了一个socket...");
		socketMap.put(socket, System.currentTimeMillis());
	}
	
	public void remove(Socket socket) {
		//System.out.println("socketMap取消了一个socket的队列监控...");
		socketMap.remove(socket);
	}
	
	@Override
	public void run() {
		System.out.println();
		while(true) {
			for(Entry<Socket, Long> entry: socketMap.entrySet()) {
				//System.out.println("监控进程判断中...");
				if(entry.getKey().isClosed()) {
					socketMap.remove(entry.getKey());
				}else if( System.currentTimeMillis() - entry.getValue() >= socketTimeout ) {
					try {
						entry.getKey().close();
						//System.out.println("关闭Socket成功...");
					} catch (IOException e) {
						//System.out.println("关闭Socket失败...");
						e.printStackTrace();
					}
					socketMap.remove(entry.getKey());
				}
			}
			try {
				//System.out.println("监控进程运行中...");
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				break;
			}
		}
		
		
	}

}
