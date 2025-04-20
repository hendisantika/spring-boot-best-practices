package dev.nano.bank.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nano.bank.domain.Account;
import dev.nano.bank.domain.Transfer;
import dev.nano.bank.domain.User;
import dev.nano.bank.domain.enumration.EventType;
import dev.nano.bank.domain.enumration.Role;
import dev.nano.bank.dto.TransferDto;
import dev.nano.bank.repository.AccountRepository;
import dev.nano.bank.repository.TransferRepository;
import dev.nano.bank.repository.UserRepository;
import dev.nano.bank.service.AuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({SpringExtension.class})
@SpringBootTest
@AutoConfigureMockMvc
public class TransferIT {
    @Autowired
    private TransferRepository transferRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserRepository userRepository;
    @MockBean
    private AuditService auditService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ObjectMapper objectMapper;

    private final String senderAccountNumber = "010000B025001001";
    private final String receiverAccountNumber = "010000B025001002";
    private final String reason = "salary";
    private final BigDecimal amount = BigDecimal.valueOf(5000L);

    @Test
    void itShouldSendTransferWithSuccess() throws Exception {
        // Given
        TransferDto transferDto = new TransferDto(senderAccountNumber, receiverAccountNumber, reason, amount, new Date());

        String role_super_admin = Role.ROLE_SUPER_ADMIN.toString();
        User user1 = new User(
                1L,
                "nano1",
                passwordEncoder.encode("password123"),
                "MALE",
                "na",
                "no",
                new Date(),
                role_super_admin,
                getRoleEnumName(role_super_admin).getAuthorities(),
                true
        );
        userRepository.save(user1);

        Account sender = new Account(1L, "010000B025001001", "RIB1", BigDecimal.valueOf(20000L), user1);
        accountRepository.save(sender);
        Account receiver = new Account(2L, "010000B025001002", "RIB2", BigDecimal.valueOf(15000L), user1);
        accountRepository.save(receiver);

        // When
        ResultActions resultActions =  mockMvc.perform(post("/api/v1/transfers/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDto)));

        // Then
        resultActions.andExpect(status().isOk());

        List<Transfer> transfers = transferRepository.findAll();

        assertThat(transfers).isNotEmpty();
        Transfer transfer = transfers.get(transfers.size() - 1);
        System.out.println("[DEBUG_LOG] Transfer: " + transfer);
        System.out.println("[DEBUG_LOG] Transfer amount: " + transfer.getAmount());
        System.out.println("[DEBUG_LOG] Transfer amount class: " + transfer.getAmount().getClass().getName());
        System.out.println("[DEBUG_LOG] Expected amount: " + amount);
        System.out.println("[DEBUG_LOG] Expected amount class: " + amount.getClass().getName());
        System.out.println("[DEBUG_LOG] Transfer amount toString: " + transfer.getAmount().toString());
        System.out.println("[DEBUG_LOG] Expected amount toString: " + amount);
        assertThat(transfer.getSenderAccount().getAccountNumber()).isEqualTo(senderAccountNumber);
        assertThat(transfer.getReceiverAccount().getAccountNumber()).isEqualTo(receiverAccountNumber);
        assertThat(transfer.getReason()).isEqualTo(reason);
        // Compare the numeric values instead of string representations
        assertThat(transfer.getAmount().compareTo(amount)).isEqualTo(0);

        Account senderAfterTransfer = accountRepository.findAccountByAccountNumber("010000B025001001").get();
        BigDecimal expectedSenderBalance = BigDecimal.valueOf(15000L);
        System.out.println("[DEBUG_LOG] Sender balance after transfer: " + senderAfterTransfer.getBalance());
        System.out.println("[DEBUG_LOG] Expected sender balance: " + expectedSenderBalance);
        // Compare the numeric values instead of direct equality
        assertThat(senderAfterTransfer.getBalance().compareTo(expectedSenderBalance)).isEqualTo(0);

        Account receiverAfterTransfer = accountRepository.findAccountByAccountNumber("010000B025001002").get();
        BigDecimal expectedReceiverBalance = BigDecimal.valueOf(20000L);
        System.out.println("[DEBUG_LOG] Receiver balance after transfer: " + receiverAfterTransfer.getBalance());
        System.out.println("[DEBUG_LOG] Expected receiver balance: " + expectedReceiverBalance);
        // Compare the numeric values instead of direct equality
        assertThat(receiverAfterTransfer.getBalance().compareTo(expectedReceiverBalance)).isEqualTo(0);

        verify(auditService).audit(
                eq(EventType.TRANSFER),
                eq("""
                        Transfer from 010000B025001001 To 010000B025001002
                        Amount 5000
                        Reason salary
                        """)
        );
    }

    private Role getRoleEnumName(String role) {
        return Role.valueOf(role.toUpperCase());
    }
}
