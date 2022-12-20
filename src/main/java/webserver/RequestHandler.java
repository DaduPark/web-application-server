package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
 
        	String mappingUrl = getMappingUrl(getHeaderFirstLine(in));
        	byte[] body = getMappingBody(getFile(mappingUrl));
        	
        	// TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
    		DataOutputStream dos = new DataOutputStream(out);
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private String getHeaderFirstLine(InputStream in) {
    	InputStreamReader isr = new InputStreamReader(in);
    	BufferedReader br = new BufferedReader(isr);

    	String firstLine="";
		try {
			firstLine = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	return firstLine;
    }
    
    private String getMappingUrl(String headerFirstLine) {
    	return headerFirstLine.split(" ")[1];
    }
    
    private byte[] getMappingBody(File file) {
    	byte[] body = null;
    	
    	try {
    		body = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return body;
    }
    
    private File getFile(String mappingUrl) {
    	File file = new File("./webapp" + mappingUrl);
    	
        if (file.exists()) {
            if (!file.isDirectory()) {
            	return file;
            }
        }
        return new File("./webapp/index.html");
    }
    
}
