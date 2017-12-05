package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

public class Server3 {
	
	private static List <BankImpl> serverNames = new ArrayList();
	private static BankImpl bankBranch;
	private static String[] serverInitial = {"qc", "bc", "mb", "nb"};
	private static ArrayList<String> arrParameters = new ArrayList<String>();
	
	public static void main(String args[]) throws IOException
	{
		int portNum = Integer.parseInt(args[0]);
		System.out.println(portNum);
		
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
			@SuppressWarnings("resource")
			DatagramSocket sendSocket = new DatagramSocket(portNum);
			
			for(int i = 0; i < serverNames.size(); i++)
			{		
				System.out.println( serverInitial[i].toString() + " server ready and waiting");
			}
			
			// UDP server initialization
			while(true){
				
			MulticastSocket serverSocket = new MulticastSocket(4000);
			InetAddress group = InetAddress.getByName("230.0.0.0");
			serverSocket.joinGroup(group);
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
			System.out.println(sentence);
			
			
			// Validate to see if the client request is a getCount() or a transfer fund
			if(transferMsg[3].equalsIgnoreCase("count") == true)
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
			else if(transferMsg[3].equalsIgnoreCase("transfer") == true)
			{
				boolean status = true;
				String initialSource = transferMsg[4].substring(0, 2).toLowerCase();
				String initialDestination = transferMsg[6].substring(0, 2).toLowerCase();
				
				for(int i = 0; i < serverNames.size(); i++)
				{
				    // This handles the withdrawal, withdrawal always has to go first, that is where status variable comes to play
					if(initialSource.equalsIgnoreCase(serverInitial[i]) && status == true)
					{
						if(Double.parseDouble(transferMsg[5]) <= Double.parseDouble(serverNames.get(i).getBalance(transferMsg[4])) && status == true)
						{
							serverNames.get(i).withdrawal(transferMsg[4], Double.parseDouble(transferMsg[5]));
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
						serverNames.get(i).deposit(transferMsg[6], Double.parseDouble(transferMsg[5]));
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
			else if(transferMsg[3].equalsIgnoreCase("deposit") == true)
			{
				bankBranch = getBranch(transferMsg[4]);  // must fill it with the branch initial
				getParameters(sentence);
				replyMsg = bankBranch.deposit(arrParameters.get(0), Double.parseDouble(arrParameters.get(1)));
			}
			else if(transferMsg[3].equalsIgnoreCase("withdraw"))
			{
				bankBranch = getBranch(transferMsg[4]);  // must fill it with the branch initial
				getParameters(sentence);
				replyMsg = bankBranch.withdrawal(arrParameters.get(0), Double.parseDouble(arrParameters.get(1)));
			}
			else if(transferMsg[3].equalsIgnoreCase("getBalance"))
			{
				bankBranch = getBranch(transferMsg[4]);  // must fill it with the branch initial
				getParameters(sentence);
				replyMsg = bankBranch.getBalance(arrParameters.get(0)).toString();
			}
			else if(transferMsg[3].equalsIgnoreCase("createAccountRecord"))
			{
				bankBranch = getBranch(transferMsg[4]);  // must fill it with the branch initial
				getParameters(sentence);
				replyMsg = bankBranch.createAccountRecord(arrParameters.get(0), arrParameters.get(1), arrParameters.get(2), arrParameters.get(3), arrParameters.get(4), arrParameters.get(5), Double.parseDouble(arrParameters.get(6)));
			}
			else if(transferMsg[3].equalsIgnoreCase("editRecord"))
			{
				bankBranch = getBranch(transferMsg[4]);  // must fill it with the branch initial
				getParameters(sentence);
				replyMsg = bankBranch.editRecord(arrParameters.get(0), arrParameters.get(1), arrParameters.get(2));
			}
			
			sendData = replyMsg.getBytes();
			System.out.println(replyMsg + "  " + transferMsg[2]);
			System.out.println(sendData);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(transferMsg[1]), Integer.parseInt(transferMsg[2]));
			//DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("localhost"), 7000);
			sendSocket.send(sendPacket);
			}
			
			
			
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
		String province = initial.substring(0, 2);
		
		for(int i = 0; i < serverInitial.length; i++)
		{
			
			if(province.equalsIgnoreCase(serverInitial[i]))
			{
				server = serverNames.get(i);
			}
		}
		
		return server;
	}
	
	
	public static void getParameters(String message)
	{
		String[] arrMessage = message.split(",");
		arrParameters.clear();
		
		String idType = arrMessage[4].substring(2, 3);
		int index = 0;
		
		if(idType.equalsIgnoreCase("m"))
		{
			index = 5;
		}
		else
		{
			index = 4;
		}
		
		for(int i = index; i < arrMessage.length; i++)
		{
			arrParameters.add(arrMessage[i]);
			System.out.println(arrMessage[i]);
		}
		
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
