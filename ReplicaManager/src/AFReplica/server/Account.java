package AFReplica.server;

import java.util.AbstractMap.SimpleEntry;

public class Account {
	
	private static int uniquedigit = 1111;
	
	private String firstName;
	private String lastName;
	private String customerID;
	private String address;
	private String phone;
	private double balance;
	
	public Account(String firstName, String lastName, String address, String phone, double balance, String branch, String position) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.address = address;
		this.phone = phone;
		this.balance = balance;
		this.customerID = branch + position + uniquedigit;
		uniquedigit++;
	}
	
	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getCustomerID() {
		return customerID;
	}
	
	//Atomic operation
	public synchronized void changeBranch(String branch) {
		String tmp = this.customerID.substring(2);
		this.customerID = branch + tmp;
	}
	public String getAddress() {
		return address;
	}
	
	//Atomic operation
	public synchronized void setAddress(String address) {
		this.address = address;
	}
	public String getPhone() {
		return phone;
	}
	
	//Atomic operation
	public synchronized void setPhone(String phone) {
		this.phone = phone;
	}
	public double getBalance() {
		return balance;
	}
	
	//takes in amount to deposit, adjusts balance and returns new balance
	//Atomic operation
	public synchronized double deposit(double amt) {
		this.balance = balance + amt;
		return this.balance;
	}
	
	//takes in the amount to withdraw, returns a pair with a boolean indicating success or failure and the current balance
	//Atomic operation
	public synchronized SimpleEntry<Boolean, Double> withdraw(double amt){
		if(this.balance < amt){
			return new SimpleEntry<Boolean,Double>(false,this.balance);
		}
		this.balance = this.balance - amt;
		return new SimpleEntry<Boolean,Double>(true, this.balance);
	}
}
