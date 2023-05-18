package com.aninfo.service;

import com.aninfo.exceptions.DepositNegativeSumException;
import com.aninfo.exceptions.InvalidTransactionTypeException;
import com.aninfo.exceptions.WithdrawNegativeSumException;
import com.aninfo.model.Transaction;
import com.aninfo.model.TransactionType;
import com.aninfo.repository.TransactionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionService{

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountService accountService;

    public Double applyPromo(Double sum){
        if(sum >= 2000){
            Double promotion = sum * 0.1;
            if(promotion > 500){
                promotion = 500.0;
            }
            sum += promotion;
        }
        return sum;
    }

    public Transaction saveTransaction(Transaction transaction){
        return transactionRepository.save(transaction);
    }

    public Transaction createDeposit(Transaction transaction){
        transaction.setAmount(applyPromo(transaction.getAmount()));
        if(transaction.getAmount() < 0){
            throw new DepositNegativeSumException("It is not possible to deposit negative amounts");
        }
        return saveTransaction(transaction);
    }

    public Transaction createWithdraw(Transaction transaction){
        if(transaction.getAmount() <= 0){
            throw new WithdrawNegativeSumException("It is not possible to withdraw negative amounts");
        }
        transaction.negativeAmount();
        return saveTransaction(transaction);
    }

    public Transaction createTransaction(Transaction transaction){
        if (transaction.getType() == TransactionType.DEPOSIT){
            return accountService.generateDeposit(transaction);
        }else if (transaction.getType() == TransactionType.WITHDRAW){
            return accountService.generateWithdraw(transaction);
        }
        else {
            throw new InvalidTransactionTypeException("Invalid transaction type");
        }
    }

    public List<Transaction> getTransactions(){
        return transactionRepository.findAll();
    }

    public List<Transaction> getTransactionsByCbu(Long cbu){
        return transactionRepository.findAllByAccountCbu(cbu);
    }

    public Optional<Transaction>  getTransactionsById(Long id){
        return transactionRepository.findById(id);
    }

    public void deleteTransaction(Long id){
        transactionRepository.deleteById(id);
    }

}