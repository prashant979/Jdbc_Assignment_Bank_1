package com.tavant.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * PreparedStatement CRUD Operation
 */
public class App {

	private Scanner sc;

	private Connection dbCon;

	private String query = "";

	private PreparedStatement preparedStatement;

	private ResultSet theResultSet;

	App() {

		sc = new Scanner(System.in);

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			ConnectionProvider.getConnection();

			dbCon = DriverManager.getConnection(ConnectionProvider.URLCONNECTION + ConnectionProvider.DATABASE,
					ConnectionProvider.USERNAME, ConnectionProvider.USERPASSWORD);

			System.out.println("==>Welcome to Indian Bank<==");

		} catch (ClassNotFoundException e) {
			System.out.println("Driver Problem: " + e.getMessage());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		App theApp = new App();
		theApp.homePage();

	}

	void homePage() {
		System.out.println("\n1: Create Account");
		System.out.println("2: Login");
//		System.out.println("3: View Account");
		int option = Integer.parseInt(sc.nextLine());

		switch (option) {
		case 1:
			createAccount();
			break;
		case 2:
			loginIntoAccount();
			break;
		default:
			System.out.println("\nInvalid Option");
			homePage();
		}
	}

	void createAccount() {
		System.out.println("\n->Creating Account...");
		System.out.println("Enter your Name");
		String name = sc.nextLine();
		System.out.println("Enter your Password");
		String password = sc.nextLine();

		query = "insert into accounts (name, password, balance) values (?, ?, 0)";

		try {

			preparedStatement = dbCon.prepareStatement(query);
			preparedStatement.setString(1, name);
			preparedStatement.setString(2, password);

			if (preparedStatement.executeUpdate() > 0) {
				System.out.println("\nAccount Created Successfully");
			}
		} catch (SQLException e) {
			System.out.println("\nCreating Account Problem: " + e.getMessage());
		}

		homePage();
	}

	void loginIntoAccount() {
		System.out.println("\n->Login into Account");
		System.out.println("Enter your Name: ");
		String name = sc.nextLine();
		System.out.println("Enter your Password");
		String password = sc.nextLine();

		query = "select * from accounts where name = ?";
		try {
			preparedStatement = dbCon.prepareStatement(query);
			preparedStatement.setString(1, name);
			theResultSet = preparedStatement.executeQuery();
			while (theResultSet.next()) {
				if (theResultSet.getString("password").equals(password)) {
					System.out.println("Login Successfull");
					userConsole(name);
				} else {
					System.out.println("Password Wrong");
					homePage();
				}
			}

			// Problem here not getting further executed if password or user wrong
		} catch (SQLException e) {
			System.out.println("\nInvalid Credentials");
			homePage();
			// System.out.println("Login Problem: " + e.getMessage());
		}
	}

	void userConsole(String userName) {
		System.out.println("\n1: Withdraw");
		System.out.println("2: Deposit");
		System.out.println("3: Check Balance");
		System.out.println("4: Transfer Amount");
		System.out.println("5: Print transaction");
		System.out.println("6: Logout");

		int option = Integer.parseInt(sc.nextLine());

		switch (option) {
		case 1:
			withDraw(userName);
			break;
		case 2:
			deposit(userName);
			break;
		case 3:
			checkBalance(userName);
			break;
		case 4:
			transferAmount(userName);
			break;
		case 5:
			query = "select id from accounts where name ='" + userName + "'";
			try {
				Statement stmt = dbCon.createStatement();
				theResultSet = stmt.executeQuery(query);
				while (theResultSet.next()) {
					int id = theResultSet.getInt("id");
					printTransaction(id, userName);
				}
			} catch (SQLException e) {
				System.out.println("\nProblem Switch Print Transaction: " + e.getMessage());
			}
			break;
		case 6:
			homePage();
			break;
		default:
			System.out.println("\nInvalid Option");
			userConsole(userName);
		}
	}

	void withDraw(String userName) {
		query = "select * from accounts where name = ?";
		try {
			preparedStatement = dbCon.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			preparedStatement.setString(1, userName);
			theResultSet = preparedStatement.executeQuery();
			while (theResultSet.next()) {
				int id = theResultSet.getInt("id");
				int balance = theResultSet.getInt("balance");
				System.out.println("Enter Amount to Withdraw");
				int amount = Integer.parseInt(sc.nextLine());

				if (amount < balance) {
					theResultSet.updateInt("balance", balance - amount);
					theResultSet.updateRow();
					System.out.println("Amount Withdraw Succesfull.");

					// transaction need to update
					query = "insert into transactions (balance, status, whome, transactionTime, accountId) values (?,?,?,CURRENT_TIMESTAMP,?)";
					preparedStatement = dbCon.prepareStatement(query);
					preparedStatement.setInt(1, amount);
					preparedStatement.setString(2, "WITHDRAW");
					preparedStatement.setString(3, "SELF");
					preparedStatement.setInt(4, id);

					if (preparedStatement.executeUpdate() > 0) {
						System.out.println("Transaction Updated");
					}
				} else {
					System.out.println("\nAmount cannot exceed maximum balance.");

				}
				userConsole(userName);
			}

		} catch (SQLException e) {
			System.out.println("\nInvalid Problem in Withdraw: " + e.getMessage());
			userConsole(userName);
		}
	}

	void deposit(String userName) {

		query = "select * from accounts where name = ?";
		try {
			preparedStatement = dbCon.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			preparedStatement.setString(1, userName);
			theResultSet = preparedStatement.executeQuery();
			while (theResultSet.next()) {
				int id = theResultSet.getInt("id");
				int balance = theResultSet.getInt("balance");
				System.out.println("Enter Amount to Deposit");
				int amount = Integer.parseInt(sc.nextLine());

				if (amount > 0) {
					theResultSet.updateInt("balance", balance + amount);
					theResultSet.updateRow();
					System.out.println("Amount Deposited Succesfull.");

					// transaction need to update
					query = "insert into transactions (balance, status, whome, transactionTime, accountId) values (?,?,?,CURRENT_TIMESTAMP,?)";
					preparedStatement = dbCon.prepareStatement(query);
					preparedStatement.setInt(1, amount);
					preparedStatement.setString(2, "DEPOSITED");
					preparedStatement.setString(3, "SELF");
					preparedStatement.setInt(4, id);

					if (preparedStatement.executeUpdate() > 0) {
						System.out.println("Transaction Updated");
					}
				} else {
					System.out.println("\nInvalid Amount.");

				}
				userConsole(userName);
			}

		} catch (SQLException e) {
			System.out.println("\nInvalid Problem in Deposit: " + e.getMessage());
			userConsole(userName);
		}

	}

	int checkBalance(String userName) {
		int balance = 0;
		query = "select * from accounts where name = ?";
		try {
			preparedStatement = dbCon.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			preparedStatement.setString(1, userName);
			theResultSet = preparedStatement.executeQuery();
			while (theResultSet.next()) {
				balance = theResultSet.getInt("balance");
				System.out.println("\nTotal Balance: " + balance);
			}
			userConsole(userName);
		} catch (SQLException e) {
			System.out.println("\nInvalid Problem in Deposit: " + e.getMessage());
			userConsole(userName);
		}
		return balance;

	}

	void transferAmount(String userName) {
		query = "select * from accounts";
		System.out.println("\nEnter account name to transfer");
		String recieverName = sc.nextLine();

		System.out.println("\nEnter amount to transfer");
		int amountToTransfer = Integer.parseInt(sc.nextLine());

		try {
			preparedStatement = dbCon.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			theResultSet = preparedStatement.executeQuery();

//			if(theResultSet.getInt("balance") < amountToTransfer) {
//				System.out.println("\nAmount cannot exceed maxmimum balance.");
//				userConsole(userName);
//			}

			while (theResultSet.next()) {
				if (theResultSet.getString("name").equals(recieverName)) {
					theResultSet.updateInt("balance", theResultSet.getInt("balance") + amountToTransfer);
					int id = theResultSet.getInt("id");

					// transaction need to update
					query = "insert into transactions (balance, status, whome, transactionTime, accountId) values (?,?,?,CURRENT_TIMESTAMP,?)";
					preparedStatement = dbCon.prepareStatement(query);
					preparedStatement.setInt(1, amountToTransfer);
					preparedStatement.setString(2, "RECIEVED");
					preparedStatement.setString(3, userName);
					preparedStatement.setInt(4, id);

					if (preparedStatement.executeUpdate() > 0) {
						System.out.println("Transaction Updated");
					}
				}

				if (theResultSet.getString("name").equals(userName)) {
					theResultSet.updateInt("balance", theResultSet.getInt("balance") - amountToTransfer);
					int id = theResultSet.getInt("id");

					// transaction need to update
					query = "insert into transactions (balance, status, whome, transactionTime, accountId) values (?,?,?,CURRENT_TIMESTAMP,?)";
					preparedStatement = dbCon.prepareStatement(query);
					preparedStatement.setInt(1, amountToTransfer);
					preparedStatement.setString(2, "TRANSFERED");
					preparedStatement.setString(3, recieverName);
					preparedStatement.setInt(4, id);

					if (preparedStatement.executeUpdate() > 0) {
						System.out.println("Transaction Updated");
					}

				}
				theResultSet.updateRow();
			}

			userConsole(userName);
		} catch (SQLException e) {
			System.out.println("\nInvalid Problem in TrasferAmount: " + e.getMessage());
			userConsole(userName);
		}
	}

	void printTransaction(int id, String userName) {
		query = "select * from transactions";
		System.out.println("\n->Transaction History");

		try {
			preparedStatement = dbCon.prepareStatement(query);
			theResultSet = preparedStatement.executeQuery();

			while (theResultSet.next()) {
				if (theResultSet.getInt("accountId") == id) {
					System.out.println(theResultSet.getInt("balance") + " | " + theResultSet.getString("status") + " | "
							+ theResultSet.getString("whome") + " | " + theResultSet.getTimestamp("transactionTime"));
				}
			}

		} catch (SQLException e) {
			System.out.println("\nProblem in PrintTransaction: " + e.getMessage());
		}
		userConsole(userName);

	}

}