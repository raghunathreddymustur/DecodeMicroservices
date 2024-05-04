package com.eazybytes.accounts.service.impl;

import com.eazybytes.accounts.constants.AccountsConstants;
import com.eazybytes.accounts.dto.AccountsDto;
import com.eazybytes.accounts.dto.CustomerDto;
import com.eazybytes.accounts.entity.Accounts;
import com.eazybytes.accounts.entity.Customer;
import com.eazybytes.accounts.exception.CustomerAlreadyExistsException;
import com.eazybytes.accounts.exception.ResourceNotFoundException;
import com.eazybytes.accounts.mapper.AccountMapper;
import com.eazybytes.accounts.mapper.CustomerMapper;
import com.eazybytes.accounts.repository.AccountRepository;
import com.eazybytes.accounts.repository.CustomerRepository;
import com.eazybytes.accounts.service.IAccountService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class AccountServiceImpl implements IAccountService {

    private CustomerRepository customerRepository;
    private AccountRepository accountRepository;
    @Override
    public void createAccount(CustomerDto customerDto) {
        Customer customer= CustomerMapper.mapToCustomer(customerDto,new Customer());
        Optional<Customer> optionalCustomer=customerRepository.findByMobileNumber(customer.getMobileNumber());
        if(optionalCustomer.isPresent())
        {
            throw new CustomerAlreadyExistsException("Customer Already Exists with this mobile number "+ customer.getMobileNumber());
        }

        Customer savedCustomer=customerRepository.save(customer);
        accountRepository.save(createNewAccount(savedCustomer));
    }

    @Override
    public CustomerDto fetchAccount(String mobileNumber) {
        Customer customer=customerRepository.findByMobileNumber(mobileNumber).orElseThrow(()-> new ResourceNotFoundException("Customer", "Mobile Number", mobileNumber));
        Accounts accounts=accountRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(()-> new ResourceNotFoundException("Account", "mobile Number", mobileNumber));

        CustomerDto customerDto=CustomerMapper.mapToCustomerDto(customer,new CustomerDto());
        customerDto.setAccounts(AccountMapper.mapToAccountsDto(accounts,new AccountsDto()));
        return customerDto;
    }

    private Accounts createNewAccount(Customer customer) {
        Accounts newAccount = new Accounts();
        newAccount.setCustomerId(customer.getCustomerId());
        long randomAccNumber = 1000000000L + new Random().nextInt(900000000);

        newAccount.setAccountNumber(randomAccNumber);
        newAccount.setAccountType(AccountsConstants.SAVINGS);
        newAccount.setBranchAddress(AccountsConstants.ADDRESS);
        return newAccount;
    }

    public boolean updateAccount(CustomerDto customerDto)
    {
        boolean isUpdated=false;
        AccountsDto accountsDto=customerDto.getAccounts();
        if(accountsDto!=null)
        {
            Accounts accounts=accountRepository.findById(accountsDto.getAccountNumber()).orElseThrow(()->new ResourceNotFoundException(
                    "Account","Account Number",accountsDto.getAccountNumber().toString()
            ));

            AccountMapper.mapToAccounts(accountsDto,accounts);
            accounts=accountRepository.save(accounts);

            Long customerId=accounts.getCustomerId();

            Customer customer=customerRepository.findById(customerId).orElseThrow(()->

                new ResourceNotFoundException("Customer","Customer Profile",customerId.toString()));;
            CustomerMapper.mapToCustomer(customerDto,customer);
            customer=customerRepository.save(customer);
            isUpdated=true;

        }
        return  isUpdated;
    }

    @Override
    public boolean deleteAccount(String mobileNumber) {
        boolean isSuccessful=false;
        Customer customer=customerRepository.findByMobileNumber(mobileNumber).orElseThrow(()->
            new ResourceNotFoundException("Customer","Mobile Number",mobileNumber)
        );

        accountRepository.deleteByCustomerId(customer.getCustomerId());
        customerRepository.deleteById(customer.getCustomerId());
        isSuccessful=true;

        return isSuccessful;
    }

}
