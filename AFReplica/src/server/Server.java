package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.xml.ws.Endpoint;

import org.customer.webservice.CustomerBankObj;
import org.manager.webservice.ManagerBankObj;

public class Server {

	public static void main(String[] args) {
		
		final int UDPPortBase = 6500;
		final int WSPortBase = 8080;

		HashMap<Character, ArrayList<Account>> records = new HashMap<Character, ArrayList<Account>>();
		
		String branch = args[0];
		int UDPserverPortNum = UDPPortBase + Branches.valueOf(branch).getValue();
		int WSserverPortNum = WSPortBase + Branches.valueOf(branch).getValue();

		// Database Initialization steps
		populateDefaultRecords(records, branch);

		try {

			// Publish endpoints
			Endpoint.publish("http://localhost:"+WSserverPortNum+"/WS/CustomerBank",new CustomerBankObj(records, branch));
			Endpoint.publish("http://localhost:"+WSserverPortNum+"/WS/ManagerBank",new ManagerBankObj(records, branch));

			System.out.println(branch + " server ready ...");

			// Socket UDP/IP
			DatagramSocket aSocket = null;
			
			System.out.println(branch + " UDP server started ...");

			while (true) {
				try {
					aSocket = new DatagramSocket(UDPserverPortNum);
					final DatagramSocket socket = aSocket;
					byte[] buffer = new byte[1000];
					while (true) {
						DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
						socket.receive(packet);
						System.out.println("Recieved message.");
						byte[] in = new byte[packet.getLength()];
						System.arraycopy(packet.getData(), packet.getOffset(), in, 0, packet.getLength());
						//parse String into arguments
						ArrayList<String> arguments = parse(new String(in));
						//Count Accounts
						if ((new String(in)).equals("accountCount")) {
							// find number of records
							int numRecords = findNumOfRecords(records);
							// package numRecords and branch name
							String pair = branch + "," + numRecords;
							byte[] message = pair.getBytes();
							DatagramPacket reply = new DatagramPacket(message, pair.length(), packet.getAddress(),packet.getPort());
							socket.send(reply);
							System.out.println("Response sent.");
						}
						//Transfer Funds
						else if(arguments.get(0).equals("transferFund")){
							//Make deposit
							CustomerBankObj c = new CustomerBankObj(records, branch);
							double amount = (new Double(arguments.get(2)).doubleValue());
							String depositMessage = c.deposit(arguments.get(1), amount);
							//send reply
							byte[] message = depositMessage.getBytes();
							DatagramPacket reply = new DatagramPacket(message, depositMessage.length(), packet.getAddress(),packet.getPort());
							socket.send(reply);
							System.out.println("Response sent.");
						}
						else{
							//Do nothing
						}
					}
				} catch (SocketException e) {
					System.out.println("Socket: " + e.getMessage());
				} catch (IOException e) {
					System.out.println("IO: " + e.getMessage());
				} finally {
					if (aSocket != null)
						aSocket.close();
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

	// --------------------------------------------
	// find the number of records in the hashmap
	// ---------------------------------------------
	private static int findNumOfRecords(HashMap<Character, ArrayList<Account>> records) {
		int size = 0;
		// add size of each arraylist together
		for (int i = 0; i < 26; i++) {
			size += records.get((char) ('A' + i)).size();
		}
		return size;
	}

}
