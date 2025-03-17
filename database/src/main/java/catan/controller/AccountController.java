package catan.controller;

import catan.Account;
import catan.AccountDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountDAO accountDAO;

    @Autowired
    public AccountController(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        Account createdAccount = accountDAO.create(account);
        return ResponseEntity.ok(createdAccount);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable Long id) {
        Account account = accountDAO.findById(id);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(account);
    }

    @PutMapping("/{id}/username")
    public ResponseEntity<Account> updateUsername(@PathVariable Long id, @RequestBody String username) {
        Account account = accountDAO.findById(id);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        account.setUsername(username);
        accountDAO.updateUsername(account);
        return ResponseEntity.ok(account);
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Account> updatePassword(@PathVariable Long id, @RequestBody String password) {
        Account account = accountDAO.findById(id);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        account.setPassword(password);
        accountDAO.updatePassword(account);
        return ResponseEntity.ok(account);
    }

    @PutMapping("/{id}/elo")
    public ResponseEntity<Account> updateElo(@PathVariable Long id, @RequestBody Long elo) {
        Account account = accountDAO.findById(id);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        account.setElo(elo);
        accountDAO.updateElo(account);
        return ResponseEntity.ok(account);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> updateAccount(@PathVariable Long id, @RequestBody Account updatedAccount) {
        Account account = accountDAO.findById(id);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        account.setUsername(updatedAccount.getUsername());
        account.setPassword(updatedAccount.getPassword());
        account.setElo(updatedAccount.getElo());
        accountDAO.update(account);
        return ResponseEntity.ok(account);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        Account account = accountDAO.findById(id);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        accountDAO.delete(account);
        return ResponseEntity.ok().build();
    }
}