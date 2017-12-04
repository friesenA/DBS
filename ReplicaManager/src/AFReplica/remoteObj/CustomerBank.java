package AFReplica.remoteObj;

public interface CustomerBank {
	String deposit(String customerID, double amt);

	String withdraw(String customerID, double amt);

	String getBalance(String customerID);

	String transferFund(String sourceCustomerID, double amount, String destinationCustomerID);
}
