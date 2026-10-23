package com.ashish.banking;

import com.ashish.banking.entity.Customer;
import com.ashish.banking.repository.CustomerRepository;
import com.ashish.banking.repository.TransactionRepository;
import com.ashish.banking.service.CustomerService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;

    private final String accountNumber = "1234567890";
    private final String email = "ashish@example.com";

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setAccountNumber(accountNumber);
        customer.setBalance(new BigDecimal("1000.00"));
    }

    @Test
    void depositShouldIncreaseBalanceAndSaveTransaction() {
        when(customerRepository.findByAccountNumberAndOwnerEmailForUpdate(
                accountNumber, email))
                .thenReturn(Optional.of(customer));

        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Customer result = customerService.depositMoneyForUser(
                accountNumber,
                new BigDecimal("200.00"),
                email);

        assertEquals(new BigDecimal("1200.00"), result.getBalance());

        verify(customerRepository).save(customer);
        verify(transactionRepository).save(any());
    }

    @Test
    void withdrawalShouldDecreaseBalanceAndSaveTransaction() {
        when(customerRepository.findByAccountNumberAndOwnerEmailForUpdate(
                accountNumber, email))
                .thenReturn(Optional.of(customer));

        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Customer result = customerService.withdrawMoneyForUser(
                accountNumber,
                new BigDecimal("300.00"),
                email);

        assertEquals(new BigDecimal("700.00"), result.getBalance());

        verify(customerRepository).save(customer);
        verify(transactionRepository).save(any());
    }

    @Test
    void withdrawalShouldFailWhenBalanceIsInsufficient() {
        when(customerRepository.findByAccountNumberAndOwnerEmailForUpdate(
                accountNumber, email))
                .thenReturn(Optional.of(customer));

        assertThrows(
                IllegalArgumentException.class,
                () -> customerService.withdrawMoneyForUser(
                        accountNumber,
                        new BigDecimal("1500.00"),
                        email));

        verify(customerRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void depositShouldRejectZeroAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> customerService.depositMoneyForUser(
                        accountNumber,
                        BigDecimal.ZERO,
                        email));

        verify(customerRepository, never())
                .findByAccountNumberAndOwnerEmailForUpdate(
                        anyString(), anyString());
    }

    @Test
    void depositShouldRejectAmountWithMoreThanTwoDecimalPlaces() {
        assertThrows(
                IllegalArgumentException.class,
                () -> customerService.depositMoneyForUser(
                        accountNumber,
                        new BigDecimal("100.123"),
                        email));

        verify(customerRepository, never())
                .findByAccountNumberAndOwnerEmailForUpdate(
                        anyString(), anyString());
    }

    @Test
    void depositShouldRejectAccountNotOwnedByUser() {
        when(customerRepository.findByAccountNumberAndOwnerEmailForUpdate(
                accountNumber, email))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> customerService.depositMoneyForUser(
                        accountNumber,
                        new BigDecimal("100.00"),
                        email));

        verify(customerRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transferShouldRejectSameSenderAndReceiverAccount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> customerService.transferMoneyForUser(
                        accountNumber,
                        accountNumber,
                        new BigDecimal("100.00"),
                        email));

        verify(customerRepository, never())
                .findByAccountNumberForUpdate(anyString());
    }

}