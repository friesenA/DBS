package UDP;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class MulticastListener extends Thread {

	MulticastSocket socket;
	ArrayList<DatagramPacket> packetList;
	InetAddress group;
	boolean stop;

	public MulticastListener(MulticastSocket socket, InetAddress group, ArrayList<DatagramPacket> packetList) {
		this.socket = socket;
		this.packetList = packetList;
		this.group = group;
		this.stop = false;
	}

	public void run() {
		byte[] buffer = new byte[1000];
		DatagramPacket response = new DatagramPacket(buffer, buffer.length);
		try {
			socket.joinGroup(group);

			while (!stop) {
				// receive response
				try {
					socket.setSoTimeout(1000);
					socket.receive(response);

					this.packetList.add(response);
					buffer = new byte[1000];
					response = new DatagramPacket(buffer, buffer.length);
				} catch (SocketTimeoutException e) {
					continue;
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void exit() {
		this.stop = true;
	}

}
