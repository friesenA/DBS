package server;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import BankModule.CustomerBank;
import BankModule.ManagerBank;
import BankModule.CustomerBankHelper;
import BankModule.ManagerBankHelper;

public class FrontEnd {

	public static void main(String[] args) {

		try {

			// create and initialize the ORB //// get reference to rootpoa &amp;
			// activate the POAManager
			ORB orb = ORB.init(args, null);
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			// create servant and register it with the ORB
			CustomerBankObj CBobj = new CustomerBankObj();
			ManagerBankObj MBobj = new ManagerBankObj();
			CBobj.setORB(orb);
			MBobj.setORB(orb);

			// get object reference from the servant
			org.omg.CORBA.Object CBref = rootpoa.servant_to_reference(CBobj);
			org.omg.CORBA.Object MBref = rootpoa.servant_to_reference(MBobj);
			CustomerBank javaCBref = CustomerBankHelper.narrow(CBref);
			ManagerBank javaMBref = ManagerBankHelper.narrow(MBref);
			
			//get name service reference
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			
			//bind objects to naming service
			NameComponent CBpath[] = ncRef.to_name("cb");
			NameComponent MBpath[] = ncRef.to_name("mb");
			ncRef.rebind(CBpath, javaCBref);
			ncRef.rebind(MBpath, javaMBref);

			System.out.println("Front End CORBA server ready ...");
			
			// Start the ORB in a new thread
			new Thread(new Runnable() {
				public void run() {
					while (true) {
						orb.run();
					}
				}
			}).start();
		}
		catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}

		System.out.println("Front End Server Exiting ...");

	}
}
