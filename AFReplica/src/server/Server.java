package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;

import udp.PackageProcessor;

public class Server {

	public static void main(String[] args) {
		
		final int UDPPortBase = 6500;

		HashMap<Character, ArrayList<Account>> records = new HashMap<Character, ArrayList<Account>>();
		
		String branch = args[0];
		//int UDPserverPortNum = UDPPortBase + Branches.valueOf(branch).getValue();

		// Database Initialization steps
		populateDefaultRecords(records, branch);

		try {

			// Socket UDP/IP
			DatagramSocket aSocket = null;
			byte[] buffer = new byte[1000];
			
			System.out.println(branch + " UDP server started ...");
			
			MulticastSocket socket = new MulticastSocket(4000);
			InetAddress group = InetAddress.getByName("230.0.0.0");
			socket.joinGroup(group);

			while (true) {
				try {
					/*aSocket = new DatagramSocket(UDPserverPortNum);
					final DatagramSocket socket = aSocket;
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					socket.receive(packet);
					System.out.println("Recieved message.");
					PackageProcessor pp = new PackageProcessor(packet, records, branch);
					pp.run();*/
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					socket.receive(packet);
					PackageProcessor pp = new PackageProcessor(packet, records, branch);
					pp.run();
				} catch (SocketException e) {
					System.out.println("Socket: " + e.getMessage());
				} catch (IOException e) {
					System.out.println("IO: " + e.getMessage());
				} finally {
					if (socket != null)
						socket.leaveGroup(group);
						socket.close();
				}
			}
		}
		catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}

		System.out.println(branch + " Server Exiting ...");

	}

	// -----------------------------------------------------------HELPER-METHODS--------------------------------------------------------------------//

	// ---------------------------------------------------------
	// read from file to populate hash map with starting values
	// ---------------------------------------------------------
	private static void populateDefaultRecords(HashMap<Character, ArrayList<Account>> records, String branch) {
		for (int i = 0; i < 26; i++) {
			records.put((char) ('A' + i), new ArrayList<Account>());
		}

		// create stream
		FileReader fr = null;
		BufferedReader in = null;
		try {
			fr = new FileReader("src/server/initialRecords.txt");
			in = new BufferedReader(fr);

			// read line by line and add to records
			String line;
			while ((line = in.readLine()) != null) {
				ArrayList<String> arguments = parse(line);
				// Create account and place it in the correct list.
				records.get(arguments.get(1).toUpperCase().charAt(0)).add(new Account(arguments.get(0),
						arguments.get(1), arguments.get(2), arguments.get(3), 5000, branch, arguments.get(4)));
			}
			fr.close();
			in.close();
		} catch (Exception e) {
			System.out.println("Error reading file.");
			e.printStackTrace();
		}
	}

	// ---------------------------------------
	// parse input string into arguments
	// ----------------------------------------
	private static ArrayList<String> parse(String command) {
		ArrayList<String> arguments = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(command, ",");
		String argument = "";
		while (tokenizer.hasMoreTokens()) {
			argument = tokenizer.nextToken();
			argument = argument.trim();
			arguments.add(argument);
		}
		return arguments;
	}

}
