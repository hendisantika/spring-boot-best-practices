package dev.nano.bank.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
@Data @AllArgsConstructor @NoArgsConstructor
public class Account implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(nullable = false, updatable = false)
  private Long id;

  @Column(length = 16, unique = true)
  private String accountNumber;

  private String rib;

  @Column(precision = 16, scale = 2)
  private BigDecimal balance;

  @ManyToOne()
  @JoinColumn(name = "user_id")
  private User user;

  public Account(String accountNumber, String rib, BigDecimal balance, User user) {
      this.accountNumber = accountNumber;
      this.rib = rib;
      this.balance = balance;
      this.user = user;
  }
}
