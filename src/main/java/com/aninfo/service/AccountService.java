package com.aninfo.service;

import com.aninfo.exceptions.DepositNegativeSumException;
import com.aninfo.exceptions.InsufficientFundsException;
import com.aninfo.exceptions.InvalidTransactionTypeException;
import com.aninfo.model.Account;
import com.aninfo.model.Transaction;
import com.aninfo.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionService transactionService;

    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

    public Collection<Account> getAccounts() {
        return accountRepository.findAll();
    }

    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id);
    }

    public void save(Account account) {
        accountRepository.save(account);
    }

    public void deleteById(Long cbu) {
        accountRepository.deleteById(cbu);
    }

    @Transactional
    public Account withdraw(Long cbu, Double sum) {
        Account account = accountRepository.findAccountByCbu(cbu);

        if (account.getBalance() < sum) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        account.setBalance(account.getBalance() - sum);
        accountRepository.save(account);

        return account;
    }

    @Transactional
    public Account deposit(Long cbu, Double sum) {

        if (sum <= 0) {
            throw new DepositNegativeSumException("Cannot deposit negative sums");
        }

        Account account = accountRepository.findAccountByCbu(cbu);
        sum = transactionService.applyPromo(sum);
        account.setBalance(account.getBalance() + sum);
        accountRepository.save(account);

        return account;
    }

    @Transactional
    public Transaction generateWithdraw(Transaction transaction) {
        Optional<Account> optionalAccount = accountRepository.findById(transaction.getAccountCbu());

        if(!optionalAccount.isPresent()){
            throw new InvalidTransactionTypeException("Invalid Transaction");
        }
        Double amount = transaction.getAmount();
        Transaction withdraw = transactionService.createWithdraw(transaction);
        withdraw(optionalAccount.get().getCbu(),amount);

        return withdraw;
    }
    
    @Transactional
    public Transaction generateDeposit(Transaction transaction) {
        Optional<Account> optionalAccount = accountRepository.findById(transaction.getAccountCbu());

        if(!optionalAccount.isPresent()){
            throw new InvalidTransactionTypeException("Invalid Transaction");
        }
        Double amount = transaction.getAmount();
        Transaction deposit = transactionService.createDeposit(transaction);
        deposit(optionalAccount.get().getCbu(),amount);

        return deposit;
    }

    public Collection<Transaction> getTransactionsByCbu(Long cbu){
        return transactionService.getTransactionsByCbu(cbu);
    }

    public Optional<Transaction> getTransactionsById(Long id){
        return transactionService.getTransactionsById(id);
    }
    
    public void deleteTransaction(Long id){
        transactionService.deleteTransaction(id);
    }
}
