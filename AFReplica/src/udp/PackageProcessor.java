package udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import remoteObj.CustomerBankObj;
import remoteObj.ManagerBankObj;
import server.Account;

public class PackageProcessor extends Thread {

	DatagramPacket packet;
	HashMap<Character, ArrayList<Account>> customerRecords;
	String branch;

	public PackageProcessor(DatagramPacket packet, HashMap<Character, ArrayList<Account>> customerRecords,
			String branch) {
		this.packet = packet;
		this.customerRecords = customerRecords;
		this.branch = branch;
	}

	public void run() {
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();

			// Unmarshall
			byte[] in = new byte[packet.getLength()];
			System.arraycopy(packet.getData(), packet.getOffset(), in, 0, packet.getLength());
			// parse String into arguments
			ArrayList<String> arguments = parse(new String(in));

			// ********************************REMOTE*METHODCALL********************************************************//
			// Check branch is accurate
			if (arguments.size() >= 4 && arguments.get(4).substring(0, 2).equals(branch)) {
				// Deposit
				if (arguments.get(3).equals("deposit")) {
					String message = "";
					if (arguments.size() == 6) {
						CustomerBankObj c = new CustomerBankObj(customerRecords, this.branch);
						double amount = (new Double(arguments.get(5)).doubleValue());
						message = c.deposit(arguments.get(4), amount);
					} else if (arguments.size() == 7) {
						ManagerBankObj m = new ManagerBankObj(customerRecords, this.branch);
						double amount = (new Double(arguments.get(6)).doubleValue());
						message = m.deposit(arguments.get(4), arguments.get(5), amount);
					}

					InetAddress a = InetAddress.getByName(arguments.get(1));
					int p = Integer.parseInt(arguments.get(2));
					sendReply(message, a, p);
				}
				// Withdraw
				else if (arguments.get(3).equals("withdraw")) {
					String message = "";
					if (arguments.size() == 6) {
						CustomerBankObj c = new CustomerBankObj(customerRecords, this.branch);
						double amount = (new Double(arguments.get(5)).doubleValue());
						message = c.withdraw(arguments.get(4), amount);
					} else if (arguments.size() == 7) {
						ManagerBankObj m = new ManagerBankObj(customerRecords, this.branch);
						double amount = (new Double(arguments.get(6)).doubleValue());
						message = m.withdraw(arguments.get(4), arguments.get(5), amount);
					}

					InetAddress a = InetAddress.getByName(arguments.get(1));
					int p = Integer.parseInt(arguments.get(2));
					sendReply(message, a, p);
				}
				// GetBalance
				else if (arguments.get(3).equals("getBalance")) {
					String message = "";
					if (arguments.size() == 5) {
						CustomerBankObj c = new CustomerBankObj(customerRecords, this.branch);
						message = c.getBalance(arguments.get(4));
					} else if (arguments.size() == 6) {
						ManagerBankObj m = new ManagerBankObj(customerRecords, this.branch);
						message = m.getBalance(arguments.get(4), arguments.get(5));
					}

					InetAddress a = InetAddress.getByName(arguments.get(1));
					int p = Integer.parseInt(arguments.get(2));
					sendReply(message, a, p);
				}
				// TransferFund
				else if (arguments.get(3).equals("transferFund")) {
					String message = "";
					if (arguments.size() == 7) {
						CustomerBankObj c = new CustomerBankObj(customerRecords, this.branch);
						double amount = (new Double(arguments.get(5)).doubleValue());
						message = c.transferFund(arguments.get(4), amount, arguments.get(6));
					} else if (arguments.size() == 8) {
						ManagerBankObj m = new ManagerBankObj(customerRecords, this.branch);
						double amount = (new Double(arguments.get(6)).doubleValue());
						message = m.transferFund(arguments.get(4), arguments.get(5), amount, arguments.get(7));
					}

					InetAddress a = InetAddress.getByName(arguments.get(1));
					int p = Integer.parseInt(arguments.get(2));
					sendReply(message, a, p);
				}
				// CreateAccountRecord
				else if (arguments.get(3).equals("createAccountRecord")) {
					String message = "";
					ManagerBankObj m = new ManagerBankObj(customerRecords, this.branch);
					message = m.createAccountRecord(arguments.get(4), arguments.get(5), arguments.get(6),
							arguments.get(7), arguments.get(8), arguments.get(9));

					InetAddress a = InetAddress.getByName(arguments.get(1));
					int p = Integer.parseInt(arguments.get(2));
					sendReply(message, a, p);
				}
				// EditRecord
				else if (arguments.get(3).equals("editRecord")) {
					String message = "";
					ManagerBankObj m = new ManagerBankObj(customerRecords, this.branch);
					message = m.editRecord(arguments.get(4), arguments.get(5), arguments.get(6), arguments.get(7));

					InetAddress a = InetAddress.getByName(arguments.get(1));
					int p = Integer.parseInt(arguments.get(2));
					sendReply(message, a, p);
				}
				// AccountCount
				else if (arguments.get(3).equals("getAccountCount")) {
					String message = "";
					ManagerBankObj m = new ManagerBankObj(customerRecords, this.branch);
					message = m.getAccountCount(arguments.get(4));

					InetAddress a = InetAddress.getByName(arguments.get(1));
					int p = Integer.parseInt(arguments.get(2));
					sendReply(message, a, p);
				}
			}
			// *****************************INTERNAL*METHODCALL**********************************************************//
			// Count Accounts internal to Replica
			else if ((new String(in)).equals("accountCountInternal")) {
				// find number of records
				int numRecords = findNumOfRecords(customerRecords);
				// package numRecords and branch name
				String pair = ", " + this.branch + " " + numRecords;
				byte[] message = pair.getBytes();
				DatagramPacket reply = new DatagramPacket(message, pair.length(), packet.getAddress(),
						packet.getPort());
				socket.send(reply);
				System.out.println("Response sent.");
			}
			// Transfer Funds internal to Replica
			else if (arguments.get(0).equals("transferFundInternal")) {
				// Make deposit
				CustomerBankObj c = new CustomerBankObj(customerRecords, this.branch);
				double amount = (new Double(arguments.get(2)).doubleValue());
				String depositMessage = c.deposit(arguments.get(1), amount);
				// send reply
				byte[] message = depositMessage.getBytes();
				DatagramPacket reply = new DatagramPacket(message, depositMessage.length(), packet.getAddress(),
						packet.getPort());
				socket.send(reply);
				System.out.println("Response sent.");
			} else {
				// Do nothing
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (socket != null)
				socket.close();
		}
	}

	// ---------------------------------------
	// Send Reply
	// ---------------------------------------
	private static void sendReply(String message, InetAddress addr, int port) {
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			byte[] m = message.getBytes();
			DatagramPacket reply = new DatagramPacket(m, message.length(), addr, port);
			socket.send(reply);

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (socket != null)
				socket.close();
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
