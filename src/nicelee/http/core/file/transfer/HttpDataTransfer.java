package nicelee.http.core.file.transfer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class HttpDataTransfer {

	final static byte[] BREAK_LINE = "\r\n".getBytes();

	/**
	 * 缓冲区
	 */
	byte[] data = new byte[1024 * 8];

	/**
	 * 使用断点续传
	 * 
	 * @param begin 文件起始位置
	 * @param end   文件结束位置
	 * @param out   面向客户端的输出流
	 * @param file  文件
	 * @throws IOException
	 */
	public void transferFileWithRange(long begin, long end, BufferedOutputStream out, File file) throws IOException {

		RandomAccessFile raf = new RandomAccessFile(file, "r");
		try {
			long cnt = 0, fSize = end - begin + 1;

			out.write("Content-Length: ".getBytes());
			out.write(("" + fSize).getBytes());
			out.write(BREAK_LINE);

			out.write("Content-Range:bytes ".getBytes());
			out.write(("" + begin).getBytes());
			out.write(("-").getBytes());
			out.write(("" + end).getBytes());
			out.write(("/").getBytes());
			out.write(("" + file.length()).getBytes());
			out.write(BREAK_LINE);

			out.write(BREAK_LINE);
			raf.seek(begin);
			int sizeRead = raf.read(data);
			while (sizeRead >= 0) {
				if (sizeRead <= fSize - cnt) {
					out.write(data, 0, sizeRead);
					cnt += sizeRead;
					sizeRead = raf.read(data);
				} else {
					out.write(data, 0, (int) (fSize - cnt));
					break;
				}
			}
			out.flush();
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				raf.close();
			} catch (Exception e) {
			}
		}

	}

	/**
	 * 使用常见传输方式传输文件
	 * 
	 * @param out  面向客户端的输出流
	 * @param file 文件
	 * @throws IOException
	 */
	public void transferFileCommon(BufferedOutputStream out, File file) throws IOException {
		// System.out.println("Transfering file...");
		RandomAccessFile raf = new RandomAccessFile(file, "r");
		try {
			long fSize = file.length();
			// System.out.println("File size: "+ fSize);
			out.write("Content-Length: ".getBytes());
			out.write(("" + fSize).getBytes());
			out.write(BREAK_LINE);
			out.write(BREAK_LINE);
			int sizeRead = raf.read(data);

			while (sizeRead > 0) {
				out.write(data, 0, sizeRead);
				sizeRead = raf.read(data);
			}
			out.write(BREAK_LINE);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				raf.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 使用Chunked 方式传输文件
	 * 
	 * @param out  面向客户端的输出流
	 * @param file 文件
	 * @throws IOException
	 */
	public void transferFileChunked(BufferedOutputStream out, File file) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(file, "r");
		try {
			System.out.println("准备传输 + ");
			out.write("Transfer-Encoding: chunked".getBytes());
			out.write(BREAK_LINE);
			out.write(BREAK_LINE);
			int sizeRead = raf.read(data);

			while (sizeRead > 0) {
				System.out.println("准备传输 + " + String.format("%x", sizeRead));
				out.write(String.format("%x", sizeRead).getBytes());
				out.write(BREAK_LINE);
				out.write(data, 0, sizeRead);
				out.write(BREAK_LINE);
				sizeRead = raf.read(data);
				out.flush();
			}
			out.write(48);
			out.write(BREAK_LINE);
			out.write(BREAK_LINE);
			out.write(BREAK_LINE);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				raf.close();
			} catch (Exception e) {
			}
		}

	}
}
