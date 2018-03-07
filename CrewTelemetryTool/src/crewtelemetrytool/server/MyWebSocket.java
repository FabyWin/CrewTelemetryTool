package test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

public class MyWebSocket {
	public MyWebSocket() throws IOException, NoSuchAlgorithmException {

		System.out.println("Starting Websocket");
		ServerSocket socketTest = new ServerSocket(80);
		System.out.println("Websocket Started");
		System.out.println("Waiting for Client");
		Socket client = socketTest.accept();
		System.out.println("Client connected");
		// Starting Handshake
		String dataRequest = new Scanner(client.getInputStream(), "UTF-8").useDelimiter("\\r\\n\\r\\n").next();

		Matcher get = Pattern.compile("^GET").matcher(dataRequest);

		Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(dataRequest);
		match.find();
		byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n" + "Connection: Upgrade\r\n" + "Upgrade: websocket\r\n"
				+ "Sec-WebSocket-Accept: "
				+ DatatypeConverter.printBase64Binary(MessageDigest.getInstance("SHA-1")
						.digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8")))
				+ "\r\n\r\n").getBytes("UTF-8");

		client.getOutputStream().write(response, 0, response.length); // Send response
		// END Handshake

		// INPUT STREAM RECEIVE

		Thread send = new Thread() {
			public void run() {

				boolean manualControl = true;
				while (manualControl) {
					Scanner scanner = new Scanner(System.in);

					while (true) {
						System.out.print("X: ");
						String posX = scanner.nextLine();
						System.out.print("Y: ");
						String posY = scanner.nextLine();
						try {
							byte[] responseToClient = encodeMessage(new String("X:" + posX + "Y:" + posY));
							client.getOutputStream().write(responseToClient, 0, responseToClient.length);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} // Send response
					}
				}

				while (true) {
					try {
						client.getInputStream().read();
						int payloadLength = client.getInputStream().read() - 128; // calc payloadLength

						int maskingLength = 4;

						byte[] maskingKey;
						byte[] payloadData;
						byte[] decodedPayloadData = null;

						if (payloadLength >= 0 && payloadLength <= 125) {
							System.out.println("Length: " + payloadLength);
							maskingLength = 4; // in byte
							maskingKey = new byte[maskingLength];
							payloadData = new byte[payloadLength];
							client.getInputStream().read(maskingKey, 0, maskingLength);
							client.getInputStream().read(payloadData, 0, payloadLength);
							decodedPayloadData = decodeMessage(maskingKey, payloadData);
						} else if (payloadLength == 126) {
							// see: https://tools.ietf.org/html/rfc6455#section-5.3
							// see also:
							// https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_a_WebSocket_server_in_Java

						} else if (payloadLength == 127) {
							// see: https://tools.ietf.org/html/rfc6455#section-5.3
							// see also:
							// https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_a_WebSocket_server_in_Java
						}

						byte[] responseToClient = encodeMessage(new String(decodedPayloadData));
						client.getOutputStream().write(responseToClient, 0, responseToClient.length); // Send response
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} // jump first byte
				}
			}
		};
		send.start();

		Thread receive = new Thread() {
			public void run() {
				while (true) {
					try {
						client.getInputStream().read();
						int payloadLength = client.getInputStream().read() - 128; // calc payloadLength

						int maskingLength = 4;

						byte[] maskingKey;
						byte[] payloadData;
						byte[] decodedPayloadData = null;

						if (payloadLength >= 0 && payloadLength <= 125) {
							System.out.println("Length: " + payloadLength);
							maskingLength = 4; // in byte
							maskingKey = new byte[maskingLength];
							payloadData = new byte[payloadLength];
							client.getInputStream().read(maskingKey, 0, maskingLength);
							client.getInputStream().read(payloadData, 0, payloadLength);
							decodedPayloadData = decodeMessage(maskingKey, payloadData);
							System.out.println(new String(decodedPayloadData));//Write into console
						} else if (payloadLength == 126) {
							// see: https://tools.ietf.org/html/rfc6455#section-5.3
							// see also:
							// https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_a_WebSocket_server_in_Java

						} else if (payloadLength == 127) {
							// see: https://tools.ietf.org/html/rfc6455#section-5.3
							// see also:
							// https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_a_WebSocket_server_in_Java
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		receive.start();
	}// End of Constructor

	/**
	 * TODO
	 * 
	 * @param payloadData
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public byte[] encodeMessage(String message) throws UnsupportedEncodingException {
		byte[] messageBytes = message.getBytes("UTF-8");
		byte[] fragment = new byte[messageBytes.length + 2];
		fragment[0] = (byte) 129;
		fragment[1] = (byte) messageBytes.length;
		for (int i = 2; i < fragment.length; i++) {
			fragment[i] = messageBytes[i - 2];
		}
		return fragment;
	}

	/**
	 * TODO
	 * 
	 * @param maskingKey
	 * @param payloadData
	 * @return
	 */
	public byte[] decodeMessage(byte[] maskingKey, byte[] payloadData) {
		byte[] decoded = new byte[payloadData.length];
		for (int i = 0; i < payloadData.length; i++) {
			decoded[i] = (byte) (payloadData[i] ^ maskingKey[i & 0x3]);
		}
		return decoded;
	}

}
