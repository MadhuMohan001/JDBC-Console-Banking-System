CREATE DATABASE IF NOT EXISTS bankdb;
USE bankdb;

CREATE TABLE IF NOT EXISTS accounts (
  acc_no INT PRIMARY KEY AUTO_INCREMENT,
  holder VARCHAR(50) NOT NULL,
  email VARCHAR(80) UNIQUE NOT NULL,
  balance DOUBLE NOT NULL DEFAULT 0 CHECK (balance >= 0),
  status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transactions (
  txn_id INT PRIMARY KEY AUTO_INCREMENT,
  acc_no INT NOT NULL,
  type VARCHAR(12) NOT NULL,          -- DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT
  amount DOUBLE NOT NULL,
  balance_after DOUBLE NOT NULL,
  related_acc INT NULL,               -- the other account in a transfer
  txn_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (acc_no) REFERENCES accounts(acc_no)
);

INSERT INTO accounts (holder, email, balance) VALUES
('Madhu', 'madhu@bank.com', 50000),
('Ravi',  'ravi@bank.com',  25000),
('Sneha', 'sneha@bank.com', 10000);
