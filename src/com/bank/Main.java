package com.bank;

import com.bank.exception.BankException;
import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.service.BankService;

import java.util.List;
import java.util.Scanner;

/** Menu loop and console input/output only. No SQL here. */
public class Main {

    private static final BankService bank = new BankService();
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            printMenu();
            String choice = prompt("Choose an option: ");
            try {
                switch (choice) {
                    case "1": createAccount();   break;
                    case "2": viewAccount();     break;
                    case "3": deposit();         break;
                    case "4": withdraw();        break;
                    case "5": transfer();        break;
                    case "6": miniStatement();   break;
                    case "7": listAccounts();    break;
                    case "8": closeAccount();    break;
                    case "0":
                        System.out.println("Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid option, try again.");
                }
            } catch (BankException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Error: please enter a valid number.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n===== CONSOLE BANK =====");
        System.out.println("1. Create account");
        System.out.println("2. View account");
        System.out.println("3. Deposit");
        System.out.println("4. Withdraw");
        System.out.println("5. Transfer");
        System.out.println("6. Mini statement (last " + BankService.STATEMENT_SIZE + ")");
        System.out.println("7. List all accounts");
        System.out.println("8. Close account");
        System.out.println("0. Exit");
    }

    private static void createAccount() {
        String name = prompt("Holder name: ");
        String email = prompt("Email: ");
        double deposit = Double.parseDouble(prompt("Opening deposit: "));
        int accNo = bank.openAccount(name, email, deposit);
        System.out.println("Account created. Account number: " + accNo);
    }

    private static void viewAccount() {
        int accNo = Integer.parseInt(prompt("Account number: "));
        System.out.println(bank.getAccount(accNo));
    }

    private static void deposit() {
        int accNo = Integer.parseInt(prompt("Account number: "));
        double amount = Double.parseDouble(prompt("Amount: "));
        double balance = bank.deposit(accNo, amount);
        System.out.printf("Deposited. New balance: %.2f%n", balance);
    }

    private static void withdraw() {
        int accNo = Integer.parseInt(prompt("Account number: "));
        double amount = Double.parseDouble(prompt("Amount: "));
        double balance = bank.withdraw(accNo, amount);
        System.out.printf("Withdrawn. New balance: %.2f%n", balance);
    }

    private static void transfer() {
        int from = Integer.parseInt(prompt("From account: "));
        int to = Integer.parseInt(prompt("To account: "));
        double amount = Double.parseDouble(prompt("Amount: "));
        bank.transfer(from, to, amount);
        System.out.println("Transfer successful.");
    }

    private static void miniStatement() {
        int accNo = Integer.parseInt(prompt("Account number: "));
        List<Transaction> list = bank.miniStatement(accNo);
        if (list.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        System.out.printf("%-6s %-13s %10s %12s  %-6s %s%n",
                "ID", "TYPE", "AMOUNT", "BALANCE", "OTHER", "TIME");
        list.forEach(System.out::println);
    }

    private static void listAccounts() {
        int page = 1;
        while (true) {
            List<Account> list = bank.listAccounts(page);
            if (list.isEmpty()) {
                System.out.println(page == 1 ? "No accounts found." : "No more accounts.");
                if (page == 1) return;
                page--;                              // step back to the last real page
            } else {
                System.out.println("--- Page " + page + " ---");
                list.forEach(System.out::println);
            }
            String cmd = prompt("[n]ext, [p]revious, [q]uit: ").toLowerCase();
            if (cmd.equals("n")) page++;
            else if (cmd.equals("p") && page > 1) page--;
            else if (cmd.equals("q")) return;
        }
    }

    private static void closeAccount() {
        int accNo = Integer.parseInt(prompt("Account number: "));
        String confirm = prompt("Close account " + accNo + "? (y/n): ");
        if (confirm.equalsIgnoreCase("y")) {
            bank.closeAccount(accNo);
            System.out.println("Account closed.");
        } else {
            System.out.println("Cancelled.");
        }
    }

    private static String prompt(String message) {
        System.out.print(message);
        return sc.nextLine().trim();
    }
}
