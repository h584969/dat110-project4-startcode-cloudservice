package no.hvl.dat110.rest.counters;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import no.hvl.dat110.ac.restservice.AccessCode;
import no.hvl.dat110.ac.restservice.AccessEntry;
import no.hvl.dat110.ac.restservice.AccessMessage;
import no.hvl.dat110.ac.restservice.Endpoints;

/**
 * Unit test for simple App.
 */
@TestMethodOrder(OrderAnnotation.class)
public class AppTest {
    
	private static final String HOST = "127.0.0.1";
	private static final int PORT = 8080;
	
	private static final String MESSAGE = "A very important message";
	private static final int ID = 1;
	
	private static final int[] ACCESS_CODE = new int [] {
			2,1
	};
	
	@Test
	@Order(1)
	public void getAccessDeviceHello_ReturnsHello() {
		String expected = "IoT Access Control Device";
		String actual = send("GET",Endpoints.HELLO.toString(),String.class);
		
		assertEquals(expected, actual);
	}
	
	
	@Test
	@Order(2)
	public void postAccessMessage_ReturnsCreatedMessage() {
		
		//This test expects the server to have just been started
		AccessMessage message = new AccessMessage(MESSAGE);
		
		AccessEntry received = send("POST",Endpoints.POST_LOG.toString(),AccessEntry.class,message);
		
		assertEquals(MESSAGE, received.getMessage());
		assertEquals(ID, received.getId().intValue());
	}
	
	@Test
	@Order(3)
	public void getAccessMessage_ReturnsMessage() {
		AccessEntry received = send("GET",Endpoints.GET_LOG.toString().replace(":id", Integer.toString(ID)),AccessEntry.class);
		
		assertEquals(MESSAGE, received.getMessage());
		assertEquals(ID, received.getId().intValue());
	}
	
	@Test
	@Order(4)
	public void getAllAccessMessages_ReturnsMessageList() {
		
		
		TypeToken<List<AccessEntry>> type = new TypeToken<List<AccessEntry>>() {};
		
		List<AccessEntry> messages = send("GET", Endpoints.GET_LOGS.toString(), type.getType());
		
		assertEquals(1, messages.size());
		assertEquals(MESSAGE, messages.get(0).getMessage());
	}
	
	@Test
	@Order(5)
	public void putAccessCode_ReturnsNewAccessCode() {
		AccessCode message = new AccessCode();
		message.setAccesscode(ACCESS_CODE);
		
		AccessCode received = send("PUT",Endpoints.PUT_CODE.toString(), AccessCode.class, message);
		
		assertArrayEquals(ACCESS_CODE, received.getAccesscode());
	}
	
	@Test
	@Order(6)
	public void getAccessCode_ReturnsAccessCode() {
		AccessCode received = send("GET",Endpoints.GET_CODE.toString(),AccessCode.class);
		assertArrayEquals(ACCESS_CODE, received.getAccesscode());
	}
	
	@Test
	@Order(7)
	public void deleteAccessLogs_DeletesTheEntireLog() {
		TypeToken<List<AccessEntry>> type = new TypeToken<List<AccessEntry>>() {};
		
		List<AccessMessage> messages = send("DELETE", Endpoints.DELETE_LOGS.toString(), type.getType());
		
		
		assertEquals(0, messages.size());
		
		messages = send("GET", Endpoints.GET_LOGS.toString(), type.getType());
		
		assertEquals(0, messages.size());
	}
	
	private static <T> T send(String method, String endpoint, Type responseType) {
		String response = perform_call(method, endpoint,null);
		return new Gson().fromJson(response, responseType);
	}
	
	private static <T> T send(String method, String endpoint, Class<T> clazz) {
		return send(method, endpoint, clazz, null);
	}
	
	private static <T> T send(String method, String endpoint, Class<T> clazz, Object body) {
		String response = perform_call(method, endpoint, body);
		return new Gson().fromJson(response, clazz);
	}
	
	private static String perform_call(String method, String endpoint, Object body){
		try(Socket s = new Socket(HOST,PORT)){
			String request = 
					method + " "
				   +endpoint+" HTTP/1.1\r\n"
			   	  + "Accept: application/json\r\n"
			   	  + "Host: localhost\r\n"
			   	  + "Connection: close\r\n";
			if (body != null) {
				
				Gson gson = new Gson();
				String requestBody = gson.toJson(body);
				request += "Content-type: application/json\r\n"
						+  "Content-length: " + requestBody.length() + "\r\n"
						+ "\r\n"
						+ requestBody;
			}
			request += "\r\n";
			
			OutputStream stream = s.getOutputStream();
			
			PrintWriter pw = new PrintWriter(stream);
			pw.print(request);
			
			if (body != null) {
				
			}
			
			pw.flush();
			
			InputStream in = s.getInputStream();
			
			Scanner sc = new Scanner(in);
			
			StringBuilder sb = new StringBuilder();
			
			//We retrieve the status line to check the status code
			String status = sc.nextLine();
			
			//General syntax is [HTTP VERSION][STATUS CODE][REASON PHRASE]
			String[] tokens = status.split(" ");
			
			boolean header = true;
			while(sc.hasNext()) {
				String line = sc.nextLine();
				if (header) {
					System.out.println(line);
				}
				else {
					sb.append(line);
				}
				
				if (line.isEmpty()) {
					header = false;
				}
			}
			
			sc.close();
			
			if (Integer.parseInt(tokens[1]) / 100 != 2) {
				throw new RuntimeException("Recieved " + tokens[1] + " " + tokens[2] + ": " + sb.toString());
			}
			
			return sb.toString();
			
		}catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
