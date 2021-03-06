package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;


public class Server2 {
	
	private static List <BankImpl> serverNames = new ArrayList();
	private static BankImpl bankBranch = null;
	private static String[] serverInitial = {"qc", "bc", "mb", "nb"};
	
	public static void main(String args[]) throws IOException
	{
		
		
		
		try{
			// Creating server for each branch
			BankImpl serverQc = new BankImpl("qc", 9876);
			BankImpl serverBc = new BankImpl("bc", 9877);
			BankImpl serverMb = new BankImpl("mb", 9878);
			BankImpl serverNb = new BankImpl("nb", 9879);
			
			// Add the servers in an arraylist
			serverNames.add(serverQc);
			serverNames.add(serverBc);
			serverNames.add(serverMb);
			serverNames.add(serverNb);
			
			
			String name = "bankObj";
			
			
			for(int i = 0; i < serverNames.size(); i++)
			{		
				System.out.println( serverInitial[i].toString() + " server ready and waiting");
			}
			
			// UDP server initialization
			DatagramSocket serverSocket = new DatagramSocket(9876);
			byte[] receiveData = new byte[1024];
			byte[] sendData = new byte[1024];
			
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			String sentence = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
			System.out.println("Received command");
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
			
			// replyMsg will be the response message send back to the client
			String replyMsg = "";
			String[] transferMsg = sentence.split(",");
			
			// Validate to see if the client request is a getCount() or a transfer fund
			if(sentence.equalsIgnoreCase("count") == true)
			{
				String size = "";
				replyMsg = "Success, ";
				// Get all users from every branch and combine them into one huge string response message
				for(int i = 0; i < serverNames.size(); i++)
				{
					size = serverNames.get(i).getCount();
					replyMsg += serverInitial[i].toUpperCase() + " " + size + ", ";
				}
			}
			else if(transferMsg[0].equalsIgnoreCase("transfer") == true)
			{
				boolean status = true;
				String initialSource = transferMsg[1].substring(0, 2).toLowerCase();
				String initialDestination = transferMsg[3].substring(0, 2).toLowerCase();
				
				for(int i = 0; i < serverNames.size(); i++)
				{
				    // This handles the withdrawal, withdrawal always has to go first, that is where status variable comes to play
					if(initialSource.equalsIgnoreCase(serverInitial[i]) && status == true)
					{
						if(Double.parseDouble(transferMsg[2]) <= Double.parseDouble(serverNames.get(i).getBalance(transferMsg[1])) && status == true)
						{
							serverNames.get(i).withdrawal(transferMsg[1], Double.parseDouble(transferMsg[2]));
							i = -1;
							status = false;  // Status false means withdrawal is made, proceed to deposit
							System.out.println("withdrawal good");
						}
						else
						{
							replyMsg = "Failure";
						}
					}
					// This handles the deposit once the withdrawal is made
					else if(initialDestination.equalsIgnoreCase(serverInitial[i]) && status == false)
					{
						serverNames.get(i).deposit(transferMsg[3], Double.parseDouble(transferMsg[2]));
						replyMsg = "Success";
						i = 10;  // Skip for loop once deposit is completed
						System.out.println("deposit good");
					}
					else
					{
						replyMsg = "Failure";
					}
				}
			}
			else if(transferMsg[0].equalsIgnoreCase("deposit") == true)
			{
				bankBranch = getBranch(transferMsg[1]);  // must fill it with the branch initial
				replyMsg = bankBranch.deposit(transferMsg[2], Double.parseDouble(transferMsg[3]));
			}
			else if(transferMsg[0].equalsIgnoreCase("withdrawal"))
			{
				bankBranch = getBranch(transferMsg[1]);  // must fill it with the branch initial
				replyMsg = bankBranch.withdrawal(transferMsg[2], Double.parseDouble(transferMsg[3]));
			}
			else if(transferMsg[0].equalsIgnoreCase("balance"))
			{
				bankBranch = getBranch(transferMsg[1]);  // must fill it with the branch initial
				replyMsg = bankBranch.getBalance(transferMsg[2]);
			}
			else if(transferMsg[0].equalsIgnoreCase("create"))
			{
				bankBranch = getBranch(transferMsg[1]);  // must fill it with the branch initial
				//replyMsg = bankBranch.createAccountRecord(custId, fname, lname, custAddress, custPhone, custBranch, custBalance)
			}
			else if(transferMsg[0].equalsIgnoreCase("edit"))
			{
				bankBranch = getBranch(transferMsg[1]);  // must fill it with the branch initial
				//replyMsg = bankBranch.editRecord(custId, fieldName, newValue);
			}
			
			sendData = replyMsg.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			serverSocket.send(sendPacket);
			
			
		}
		catch(Exception e){
			System.err.println("Error:" + e);
			e.printStackTrace(System.out);
		}
		
		System.out.println("Server Exiting");
	}
	
	
	
	public static BankImpl getBranch(String initial)
	{
		BankImpl server = null;
		
		for(int i = 0; i < serverInitial.length; i++)
		{
			if(initial.equalsIgnoreCase(serverInitial[i]))
			{
				server = serverNames.get(i);
			}
		}
		
		return server;
	}
	
	
	// IGNORE IT TEMPORARY LEFT IT HERE JUST TO MAKE IT WORK
	public static String getCount() throws IOException
	{
		String message = "";
		int count = 0;
		String[] locations = {"QC", "NB", "MB", "BC"};
		String[] accountFiles = {"../Corba_Bank/Quebec_Log/QcAccountsList.txt",
				                "../Corba_Bank/Nb_Log/NbAccountsList.txt",
				                "../Corba_Bank/Manitoba_Log/MbAccountsList.txt",
				                "../Corba_Bank/Bc_Log/BcAccountsList.txt"};
		
		for(int i = 0; i < 4; i++)
		{
			count = 0;
			BufferedReader fr = new BufferedReader(new FileReader(accountFiles[i].toString()));
			
			while(fr.readLine() != null)
			{
				count++;
			}
			
			message += locations[i] + ": " + count + " users, ";
			fr.close();
		}
		
		return message;
	}

}
