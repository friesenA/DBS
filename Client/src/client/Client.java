package client;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.omg.CORBA.ORB;


public abstract class Client {

	protected String customerID;
	protected PrintWriter log;
	protected ORB orb;

//-----------------------------------------------------CONSTRUCTOR----------------------------------------------------------------------------//
	public Client(String customerID, PrintWriter log, ORB orb) {
		this.customerID = customerID;
		this.log = log;
		this.orb = orb;
	}

//----------------------------------------------------PUBLIC METHODS--------------------------------------------------------------------------//
	//returns the number of options available
	protected abstract void getCommandOptions();
	
	//enacts the method of the chosen action 
	//returns false if exit from session requested
	public abstract boolean enactCommand(String command);

//----------------------------------------------------PRIVATE METHODS--------------------------------------------------------------------------//

	
	//parse command string into arguments
	protected ArrayList<String> parse(String command){
		ArrayList<String> arguments = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(command,",");
		String argument ="";
		while (tokenizer.hasMoreTokens())
		    {
			argument = tokenizer.nextToken();
			argument = argument.trim();
			arguments.add(argument);
		    }
		return arguments;
	}
	
	//simplifies command for switch statement (optional?)
	protected int getChoice(String command){
		if(command.equalsIgnoreCase("exit")){
			return 0;
		}
		else if(command.equalsIgnoreCase("help")){
			return 1;
		}
		else if(command.equalsIgnoreCase("deposit")){
			return 2;
		}
		else if(command.equalsIgnoreCase("withdraw")){
			return 3;
		}
		else if(command.equalsIgnoreCase("balance")){
			return 4;
		}
		else if(command.equalsIgnoreCase("newAccount")){
			return 5;
		}
		else if(command.equalsIgnoreCase("editRecord")){
			return 6;
		}
		else if(command.equalsIgnoreCase("accountCount")){
			return 7;
		}
		else if(command.equalsIgnoreCase("transfer")){
			return 8;
		}
		else{
			return -1;
		}
	}
}
