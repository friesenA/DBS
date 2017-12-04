package AFReplica.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import AFReplica.udp.PackageProcessor;

public class Server {

	public static void main(String[] args) {

		int UDPPort = Integer.parseInt(args[1]);

		HashMap<Character, ArrayList<Account>> records = new HashMap<Character, ArrayList<Account>>();

		String branch = args[0];

		// Database Initialization steps
		populateDefaultRecords(records, branch);

		
		DatagramSocket aSocket = null;
		try {
			// Socket UDP/IP
			byte[] buffer = new byte[1000];

			System.out.println(branch + " UDP server started ...");

			while (true) {
				try {
					
					aSocket = new DatagramSocket(UDPPort);
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					aSocket.receive(packet);
					System.out.println("Recieved message.");
					PackageProcessor pp = new PackageProcessor(packet, records, branch, UDPPort);
					pp.run();
					
				} catch (SocketException e) {
					System.out.println("Socket: " + e.getMessage());
				} catch (IOException e) {
					System.out.println("IO: " + e.getMessage());
				}
			}
		} catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		} finally {
			if (aSocket != null) {
				aSocket.close();

			}
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
			fr = new FileReader("src/AFReplica/server/initialRecords.txt");
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
