package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class UDPSocketListener extends Thread {

	DatagramSocket socket;
	DatagramPacket request;
	ArrayList<DatagramPacket> packetBuffer;
	boolean stop;
	boolean resend;

	public UDPSocketListener(DatagramSocket socket, ArrayList<DatagramPacket> buffer, DatagramPacket request) {
		this.socket = socket;
		this.packetBuffer = buffer;
		this.request = request;
		this.stop = false;
		this.resend = false;
	}

	public void run() {
		byte[] buffer = new byte[1000];
		DatagramPacket response = new DatagramPacket(buffer, buffer.length);
		while (!stop) {
			// receive response
			try {
				if(resend){
					socket.send(request);
					resend = false;
				}
				socket.setSoTimeout(1000);
				socket.receive(response);
				
				synchronized(this){
				this.packetBuffer.add(response);
				}
				
				buffer = new byte[1000];
				response = new DatagramPacket(buffer, buffer.length);
			} catch (SocketTimeoutException e) {
				continue;
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void resend(){
		this.resend = true;
	}
	
	public void exit(){
		this.stop = true;
	}
}
