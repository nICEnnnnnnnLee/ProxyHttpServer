package nicelee.http.core.file.transfer.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;

import nicelee.http.core.file.transfer.HttpDataTransfer;
import nicelee.http.core.file.transfer.HttpHeaderTransfer;
import nicelee.http.model.HttpRequest;
import nicelee.http.model.HttpResponse;
import nicelee.http.resource.HttpResource;
import nicelee.http.util.StreamReader;

public class CommonResponse {
	
	/**
	 * 根据请求返回
	 * 
	 * @param httpRequest
	 * @param reader
	 * @param writer
	 */
	public static void doResponseCommon(File srcFolder, HttpRequest httpRequest, StreamReader in, BufferedOutputStream out)
			throws IOException {
		httpRequest.print();
		HttpResponse httpResponse = new HttpResponse();
		// 盘点是否禁止访问/未授权访问
		/**
		 * 未授权检验 这里以admin:admin为例, 固定生成jsessionid=YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXo
		 * 路径为/source
		 */
		if (httpRequest.url.startsWith("/source")) {
			// System.out.println("访问权限目录...");
			// 如果jsessionid正确, 跳过认证
			// boolean sessionCorrect = false;
			String cookies = httpRequest.headers.get("cookie");
			if (cookies == null || !HttpResource.patternSessionid.matcher(cookies).find()) {
				// System.out.println("未找到匹配session");
				String auth = httpRequest.headers.get("authorization");
				httpRequest.print();
				// 这里用contains不对, 仅作示范用, 表示鉴权通过
				if (auth != null && auth.contains("YWRtaW46YWRtaW4")) {
					// System.out.println("未找到匹配session,但是鉴权通过");
					httpRequest.headers.put("Set-cookie",
							"jsessionid=YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXo;path=/;httponly");
				} else {
					// System.out.println("未找到匹配session,且鉴权未通过");
					CommonResponse.doResponseWithFileNotAuth(httpResponse, out);
					return;
				}
				// System.out.println("存在匹配session");
			}
		}
		/**
		 * 禁止访问检验 路径为*.txt
		 */
		if (httpRequest.url.endsWith(".txt")) {
			CommonResponse.doResponseWithFileForbidden(httpResponse, out);
			return;
		}
		if (isPathExists(srcFolder, httpRequest)) {
			// System.out.println("URL Resouce is at: " + httpRequest.url);
			File file = new File(srcFolder, httpRequest.url);
			if (file.isDirectory()) {
				CommonResponse.doResponseWithFolderOK(file, httpRequest, httpResponse, out);
			} else {
				CommonResponse.doResponseWithFileOK(file, httpRequest, httpResponse, out);
			}
		} else {
			CommonResponse.doResponseWithFileNotFound(httpResponse, out);
		}
	}
	/**
	 * 若返回true, 则httpRequest.url已经做了修改,且已经变成了绝对路径
	 * 
	 * @param httpRequest
	 * @return
	 * @throws IOException
	 */
	public static boolean isPathExists(File srcFolder, HttpRequest httpRequest) throws IOException {
		// 去掉锚# 和参数? , 获取path
		String path = httpRequest.url;
		Matcher matcher = HttpResource.patternURL.matcher(path);
		//System.out.println("path路径" +httpRequest.url);
		if (matcher.find()) {
			path = matcher.group(1);
		} else {
			System.out.println("path路径不匹配");
			return false;
		}

		/**
		 * 优先顺序,
		 * 0文件存在 
		 * 1不做任何修饰, 文件存在 
		 * 2文件夹下, index.html/index.htm存在 
		 * 3path加上.html后缀,文件存在
		 */
		File file = new File(srcFolder, path);
		if (file.exists() && file.isDirectory()) {
			return true;
		}
		if (file.exists()) {
			// 匹配 1.
			if (file.isFile()) {
				return true;
			}
		} else {
			// 匹配 3.
			String[] suffixs = { ".html", ".htm" };
			for (String suffix : suffixs) {
				File filDst = new File(file.getParent(), file.getName() + suffix);
				if (filDst.exists()) {
					httpRequest.url += suffix;
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * 若URL对应的文件不允许访问, 使用该方法返回
	 * 
	 * @param httpResponse
	 * @param writer
	 * @throws IOException
	 */
	public static void doResponseWithFileForbidden(HttpResponse httpResponse, BufferedOutputStream out) throws IOException {
		HttpHeaderTransfer headerTrans = new HttpHeaderTransfer();

		// 403
		httpResponse.do403();
		httpResponse.dataLength = HttpResource.PAGE_403.length;
		headerTrans.transferCommonHeader(httpResponse, out);

		// out date-length & data
		out.write("Content-Length: ".getBytes());
		out.write(("" + httpResponse.dataLength).getBytes());
		out.write(HttpResource.BREAK_LINE);
		out.write(HttpResource.BREAK_LINE);
		out.write(HttpResource.PAGE_403);
		out.flush();
	}

	/**
	 * 若URL对应的文件未经授权认证, 使用该方法返回
	 * 
	 * @param httpResponse
	 * @param writer
	 * @throws IOException
	 */
	public static void doResponseWithFileNotAuth(HttpResponse httpResponse, BufferedOutputStream out) throws IOException {
		HttpHeaderTransfer headerTrans = new HttpHeaderTransfer();

		// 401
		httpResponse.do401();
		httpResponse.dataLength = HttpResource.PAGE_401.length;
		httpResponse.headers.put("WWW-Authenticate", "Basic realm=\"NiceLee's Site\"");
		headerTrans.transferCommonHeader(httpResponse, out);

		// out date-length & data
		out.write("Content-Length: ".getBytes());
		out.write(("" + httpResponse.dataLength).getBytes());
		out.write(HttpResource.BREAK_LINE);
		out.write(HttpResource.BREAK_LINE);
		out.write(HttpResource.PAGE_401);
		out.flush();
	}

	/**
	 * 若URL对应的文件不存在, 使用该方法返回
	 * 
	 * @param httpResponse
	 * @param writer
	 * @throws IOException
	 */
	public static void doResponseWithFileNotFound(HttpResponse httpResponse, BufferedOutputStream out) throws IOException {
		HttpHeaderTransfer headerTrans = new HttpHeaderTransfer();

		// 404
		httpResponse.do404();
		httpResponse.dataLength = HttpResource.PAGE_404.length;
		headerTrans.transferCommonHeader(httpResponse, out);

		// out date-length & data
		out.write("Content-Length: ".getBytes());
		out.write(("" + httpResponse.dataLength).getBytes());
		out.write(HttpResource.BREAK_LINE);
		out.write(HttpResource.BREAK_LINE);
		out.write(HttpResource.PAGE_404);
		out.flush();
	}

	/**
	 * 若文件夹解析成功, 按此返回目录
	 * 
	 * @param fileFolder
	 * @param httpRequest
	 * @param httpResponse
	 * @param out
	 * @throws IOException
	 */
	public static void doResponseWithFolderOK(File fileFolder, HttpRequest httpRequest, HttpResponse httpResponse,
			BufferedOutputStream out) throws IOException {
		if (!httpRequest.url.endsWith("/")) {
			httpRequest.url += "/";
		}
		// 200
		HttpHeaderTransfer headerTrans = new HttpHeaderTransfer();
		httpResponse.do200();
		headerTrans.transferCommonHeader(httpResponse, out);

		// out date-length & data
		out.write("Transfer-Encoding: chunked".getBytes());
		out.write(HttpResource.BREAK_LINE);
		out.write(HttpResource.BREAK_LINE);
		out.flush();

		byte[] head = ("<html><head><meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8;\"/><title>Nicelee.top提供</title></head><body>")
				.getBytes();
		out.write(String.format("%x", head.length).getBytes());
		out.write(HttpResource.BREAK_LINE);
		out.write(head);
		out.write(HttpResource.BREAK_LINE);

		// System.out.println("当前url 为: " +httpRequest.url);
		byte[] title = String.format("<h1>Index Of %s</h1><hr><pre>", httpRequest.url).getBytes();
		out.write(String.format("%x", title.length).getBytes());
		out.write(HttpResource.BREAK_LINE);
		out.write(title);
		out.write(HttpResource.BREAK_LINE);

		StringBuilder sb = new StringBuilder();
		// 列出父级目录
		Matcher matcher = HttpResource.patternParent.matcher(httpRequest.url);
		if (matcher.find()) {
			String parent = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
			sb.append("<a href=\"").append(parent).append("\">../</a><br>");

		}
		// 列出文件夹
		for (File childFolder : fileFolder.listFiles()) {
			if (childFolder.isDirectory()) {
				sb.append("<a href=\"").append(httpRequest.url + childFolder.getName()).append("\">")
						.append(childFolder.getName()).append("/</a><br>");

			}
		}
		// 列出文件
		for (File childFile : fileFolder.listFiles()) {
			if (childFile.isFile()) {
				String fSize = String.valueOf(childFile.length());
				sb.append("<a href=\"").append(httpRequest.url + childFile.getName()).append("\">")
						.append(childFile.getName()).append("</a>")
						.append(HttpResource.WHITE_SPACES, 0, HttpResource.WHITE_SPACES.length - childFile.getName().length())
						.append(HttpResource.aDateFormat.format(childFile.lastModified()))
						.append(HttpResource.WHITE_SPACES, 0, HttpResource.WHITE_SPACES.length - fSize.length() - 30).append(fSize).append("<br>");
			}
		}
		sb.append("</pre><hr></body></html>");
		out.write(String.format("%x", sb.length()).getBytes());
		out.write(HttpResource.BREAK_LINE);
		out.write(sb.toString().getBytes());
		out.write(HttpResource.BREAK_LINE);

		out.write(48);
		out.write(HttpResource.BREAK_LINE);
		out.write(HttpResource.BREAK_LINE);
		out.write(HttpResource.BREAK_LINE);
		out.flush();
	}

	/**
	 * 若URL对应的文件存在, 使用该方法返回
	 * 
	 * @param file
	 * @param httpResponse
	 * @param writer
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public static void doResponseWithFileOK(File file, HttpRequest httpRequest, HttpResponse httpResponse,
			BufferedOutputStream out) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		httpResponse.do200();
		httpResponse.dataLength = (int) file.length();
		HttpHeaderTransfer headerTrans = new HttpHeaderTransfer();

		String fName = file.getName().toLowerCase();
		headerTrans.setContentType(httpResponse, fName);

		// out date-length & data
		HttpDataTransfer dataTrans = new HttpDataTransfer();
		// decide file begin and end if range acquired
		String range;
		if ((range = httpRequest.headers.get("range")) != null) {
			// System.out.println("Range Required: " +range);
			Matcher matcher = HttpResource.patternFileRange.matcher(range);
			if (matcher.find()) {
				long begin = Long.parseLong(matcher.group(1));
				long end = file.length() - 1;
				try {
					end = Long.parseLong(matcher.group(2));
					end = end < (file.length() - 1) ? end : (file.length() - 1);
				} catch (Exception e) {
				}
				if (begin > 0) {
					//System.out.println("206");
					httpResponse.do206();
				}
				headerTrans.transferCommonHeader(httpResponse, out);
				dataTrans.transferFileWithRange(begin, end, out, file);
			} else {
				headerTrans.transferCommonHeader(httpResponse, out);
				dataTrans.transferFileCommon(out, file);
			}
		} else {
			headerTrans.transferCommonHeader(httpResponse, out);
			dataTrans.transferFileCommon(out, file);
		}
		out.flush();
	}
}
