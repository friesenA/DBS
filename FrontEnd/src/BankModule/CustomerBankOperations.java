package BankModule;


/**
* BankModule/CustomerBankOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from Bank.idl
* Tuesday, October 24, 2017 12:13:01 o'clock AM EDT
*/

public interface CustomerBankOperations 
{
  String deposit (String customerID, double amt);
  String withdraw (String customerID, double amt);
  String getBalance (String customerID);
  String transferFund (String sourceCustomerID, double amount, String destinationCustomerID);
} // interface CustomerBankOperations
