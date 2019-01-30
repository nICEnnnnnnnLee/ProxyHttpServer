package nicelee.http.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;

import nicelee.http.core.runnable.SocketMonitor;
import nicelee.http.model.HttpRequest;
import nicelee.http.resource.HttpResource;


/**
 * 用于从输入字节流中提取换行符前的内容
 * @author LiJia
 *
 */
public class StreamReader {
	public byte[] readBuffer;		//用于Socket数据读取
	ByteBuffer byteBuffer = null;	//用于数据缓冲
	int maxSize = 2048;
	
	BufferedInputStream in;
	
	//用于Socket监控
	SocketMonitor monitor;
	Socket socketClient;
	
	public StreamReader(SocketMonitor monitor, Socket socketClient, BufferedInputStream in) {
		this.monitor = monitor;
		this.socketClient = socketClient;
		this.in = in;
		readBuffer = new byte[1024];
		byteBuffer = ByteBuffer.allocate(2560);
	}
	
	public void close() throws IOException{
		in.close();
	}
	/**
	 *  从输入流中截取换行符\r\n, 读取前面的内容,并以String返回
	 *  maxSize后仍旧未读出换行符,抛出异常,
	 *  多余的内容在byteBuffer
	 * @param byteBuffer
	 * @return
	 * throws IOException
	 */
	public String readLine() throws IOException{
		int cnt = 0, rSize;
		//System.out.println("调用readLine方法");
		while(true) {
			//从byteBuffer中检查内容
			int position = byteBuffer.position();
			for(int i = 0 ; i< position; i++) {
				if( byteBuffer.get(i) == 13 && byteBuffer.get(i+1) == 10) {
					//构造String 
					String line = new String(byteBuffer.array(), 0, i );
					if(i + 2 <= position) {
						//将ByteBuffer多余的内容去掉
						byteBuffer.position(0);
						byteBuffer.put(byteBuffer.array(), i + 2, position - i - 2);
					}else {
						byteBuffer.clear();
					}
					//返回结果
					//System.out.println(Thread.currentThread().getName()+" -byteBuffer剩余数据(从上一次留存的byteBuffer中找到CRLF)" + (byteBuffer.position()));
					//System.out.println(Thread.currentThread().getName()+" 剩余结果" + new String(byteBuffer.array(), 0, byteBuffer.position() ));
					return line;
				}
			}
			if(byteBuffer.get(position) == 13) {
				int nextByte = in.read();
				if(nextByte == 10) {
					String line = new String(byteBuffer.array(), 0, position);
					byteBuffer.clear();
					//System.out.println(Thread.currentThread().getName()+" -byteBuffer剩余数据(从上一次留存的byteBuffer中找到CR没找到LF)" + (byteBuffer.position()));
					return line;
				}else {
					byteBuffer.put((byte)nextByte);
				}
			}
			
			//从流中读取内容
			rSize = in.read(readBuffer);
			if(rSize == -1) {
				throw new IOException("对方停止传输前, 仍旧未找到回车换行符! ");
			}
			for(int i = 0 ; i< rSize - 1; i++) {
				//找到\r\n
				if(readBuffer[i] == 13 && readBuffer[i+1] == 10) {
					//构造String 
					byteBuffer.put(readBuffer, 0, i);
					String line = new String(byteBuffer.array(), 0, byteBuffer.position());
					
					//将从流中读取多余的内容移至ByteBuffer
					byteBuffer.clear();
					if( i+ 2 <= rSize - 1) {
						byteBuffer.put(readBuffer, i+ 2, rSize - 2 - i);
					}
					//返回结果
					//System.out.println(Thread.currentThread().getName()+" -byteBuffer剩余数据(从socket中找到CRLF)" + (byteBuffer.position() + 1));
					return line;
				}
			}
			//未找到换行符
			//字符长度过大,返回null
			cnt += rSize;
			if(cnt > maxSize) {
				//System.out.println("长度过大, 仍旧未找到回车换行符: " + maxSize);
				throw new IOException("长度过大, 仍旧未找到回车换行符! ");
			}
			//System.out.println("大小" +rSize);
			//将内容移至ByteBuffer
			byteBuffer.put(readBuffer, 0, rSize);
		}
	}
	
	
	/**
	 * 返回HttpRequest结构, 不符合协议标准将会抛出异常
	 * 
	 * @param reader return
	 * @throws IOException
	 */
	public HttpRequest readHttpRequestStructrue()
			throws NullPointerException, IOException, IndexOutOfBoundsException {
		// refresh the monitor
		monitor.put(socketClient);
		// System.out.println("Headers 提取中... 可能会阻塞或抛出异常...");
		HttpRequest httpRequest = new HttpRequest();

		// 第一行
		String firstLine = readLine();
		if (firstLine == null) {
			return null;
		}
		//System.out.println("第一行为: " + firstLine);
		String firstLines[] = firstLine.split(" ");
		httpRequest.method = firstLines[0];
		httpRequest.url = firstLines[1];
		httpRequest.version = firstLines[2];
		// 第一行结束

		// 获取其他属性
		String key_value = readLine();
		while (key_value != null && key_value.length() > 0) {
			//System.out.println(key_value);
			// System.out.println("获取数据中...");
			Matcher matcher = HttpResource.patternHeaders.matcher(key_value);
			matcher.find();
			String key = matcher.group(1).trim();
			String value = matcher.group(2).trim();
			if (key.toLowerCase().startsWith("host")) {
				// 获取目的host
				httpRequest.host = value;
			}else if (key.toLowerCase().startsWith("content-length")) {
				// 判断是否有数据
				httpRequest.dataLength = Integer.parseInt(value);
			}else {
				httpRequest.headers.put(key, value);
			}
			key_value = readLine();
			//System.out.println(Thread.currentThread().getName()+"当前key_value..." + key_value);
			//System.out.println(Thread.currentThread().getName()+"当前key_value长度:" + key_value.getBytes().length);
		}
		//System.out.println(Thread.currentThread().getName()+"最后bytebuffer剩余数据..." + byteBuffer.position());
		// System.out.println("获取httpRequest完毕...");

		// 内容传输不计入时间, 则从监控队列删除
		// monitor.remove(socketClient);
		return httpRequest;
	}

	public int read(byte[] b, int off, int len) throws IOException, SocketException {

		if (byteBuffer.position() == 0) {
			//System.out.println("byteBuffer为空");
			return in.read(b, off, len);
		}
		int pos = byteBuffer.position();
		if (len > pos) {
			//System.out.println("部分从byteBuffer中读取");
			System.arraycopy(byteBuffer.array(), 0, b, off, pos);
			int rSize = in.read(b, off + pos, len - pos);
			//System.out.println("部分从流中读取");
			byteBuffer.clear();
			return rSize + pos;
		} else {
			//System.out.println("全部从byteBuffer中读取");
			System.arraycopy(byteBuffer.array(), 0, b, off, len);
			byteBuffer.position(0);
			byteBuffer.put(byteBuffer.array(), len, pos - len);
			return len;
		}
	}
	
	public int read(byte[] b) throws IOException {
		if(b == readBuffer && byteBuffer.position() > 0) {
			return read(b, 0, byteBuffer.position());
		}
		return read(b, 0, b.length);
	}
	
	/**
	 * 效率低,不推荐使用
	 * @return
	 * @throws IOException
	 */
	public int read() throws IOException {
		if (byteBuffer.position()==0) {
			return in.read();
		}
		int b = byteBuffer.get(0);
		int pos = byteBuffer.position();
		byteBuffer.position(0);
		byteBuffer.put(byteBuffer.array(), 1, pos -1);
		return b;
	}
}
