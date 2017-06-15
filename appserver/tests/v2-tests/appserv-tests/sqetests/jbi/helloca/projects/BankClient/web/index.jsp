<%@page contentType="text/text"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="bankws.BankService"%>
<%@page import="bankws.Bank"%>
<%
  BankService service = new BankService();
  Bank bank = service.getBankPort();
  double interestRate = bank.getCheckingAccountInterestRate();
  String status = "FAIL";
  if (interestRate == 5.25)
     status = "PASS";
%>
Bank.getCheckingAccountInterestRate=<%=status%>
