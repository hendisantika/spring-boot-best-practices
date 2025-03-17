package dev.nano.bank.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "deposits")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Deposit extends TransactionBase implements Serializable {
    @Column(nullable = false)
    private String senderName;

    public Deposit(BigDecimal amount, Date dateExecution, String reason, Account receiverAccount, String senderName) {
        super(amount, dateExecution, reason, receiverAccount);
        this.senderName = senderName;
    }
}
