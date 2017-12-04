import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Test {

	public static void main(String[] args) {
		ReplicaManager rm = new ReplicaManager(5432, 6543, 7654);
		new Thread(rm).start();
		
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			while (true) {
				byte[] sendData = new byte[1024];
				InetAddress aHost = InetAddress.getByName("localhost");
				String request = new String("1,8888,deposit,QCC0001,100");
				sendData = request.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData, request.length(), aHost, 5432);
				socket.send(sendPacket);
				byte[] receiveData = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				socket.receive(receivePacket);
				String message = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
				System.out.println(message);
				}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (socket != null){
				socket.close();
			}
		}
		

	}

}
