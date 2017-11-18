package remoteObj;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public interface CustomerBank {
	@WebMethod
	String deposit(String customerID, double amt);

	@WebMethod
	String withdraw(String customerID, double amt);

	@WebMethod
	String getBalance(String customerID);

	@WebMethod
	String transferFund(String sourceCustomerID, double amount, String destinationCustomerID);
}
