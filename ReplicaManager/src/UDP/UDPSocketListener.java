package UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class UDPSocketListener extends Thread {

	DatagramSocket socket;
	ArrayList<DatagramPacket> packetBuffer;
	boolean stop;

	public UDPSocketListener(DatagramSocket socket, ArrayList<DatagramPacket> buffer) {
		this.socket = socket;
		this.packetBuffer = buffer;
		this.stop = false;
	}

	public void run() {
		byte[] buffer = new byte[1000];
		DatagramPacket response = new DatagramPacket(buffer, buffer.length);
		while (!stop) {
			// receive response
			try {
				socket.setSoTimeout(1000);
				socket.receive(response);
				
				this.packetBuffer.add(response);
				buffer = new byte[1000];
				response = new DatagramPacket(buffer, buffer.length);
			} catch (SocketTimeoutException e) {
				continue;
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void exit(){
		this.stop = true;
	}
}
