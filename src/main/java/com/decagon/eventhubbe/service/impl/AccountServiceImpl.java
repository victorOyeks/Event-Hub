package com.decagon.eventhubbe.service.impl;import com.decagon.eventhubbe.domain.entities.Account;import com.decagon.eventhubbe.domain.entities.AppUser;import com.decagon.eventhubbe.domain.entities.Banks;import com.decagon.eventhubbe.domain.repository.AccountRepository;import com.decagon.eventhubbe.domain.repository.BankRepository;import com.decagon.eventhubbe.dto.request.BankData;import com.decagon.eventhubbe.dto.request.RequestAccountDTO;import com.decagon.eventhubbe.dto.request.SubAccountRequest;import com.decagon.eventhubbe.dto.response.BanksRepo;import com.decagon.eventhubbe.service.AccountService;import com.decagon.eventhubbe.utils.PaymentUtils;import com.decagon.eventhubbe.utils.UserUtils;import com.fasterxml.jackson.core.type.TypeReference;import com.fasterxml.jackson.databind.ObjectMapper;import lombok.*;import lombok.extern.slf4j.Slf4j;import org.apache.tomcat.util.json.JSONParser;import org.modelmapper.ModelMapper;import org.springframework.boot.ApplicationArguments;import org.springframework.boot.ApplicationRunner;import org.springframework.http.*;import org.springframework.stereotype.Service;import org.springframework.util.LinkedMultiValueMap;import org.springframework.util.MultiValueMap;import org.springframework.web.client.RestTemplate;import org.springframework.web.util.UriComponentsBuilder;import java.net.URI;import java.util.*;import static com.decagon.eventhubbe.utils.PaymentUtils.getSecretKey;@Slf4j@Service@RequiredArgsConstructorpublic class AccountServiceImpl implements AccountService, ApplicationRunner {    private final BankRepository bankRepository;    private final AppUserServiceImpl appUserService;    private final AccountRepository accountRepository;    private final RestTemplate restTemplate;    private final HttpHeaders headers;    private final ModelMapper modelMapper;    @Override    public void run(ApplicationArguments args) throws Exception {        PaymentUtils paymentUtils = new PaymentUtils();        HttpHeaders headers = new HttpHeaders();        headers.setBearerAuth(getSecretKey());        headers.set("Cache-Control", "no-cache");        headers.setContentType(MediaType.APPLICATION_JSON);        RequestEntity<?> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, URI.create(paymentUtils.getBANK_URL()));        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);        List<BankData> banksResponseList = new ObjectMapper().convertValue(new JSONParser(Objects.requireNonNull(responseEntity.getBody())).object().get("data"), new TypeReference<List<BankData>>() {});        bankRepository.deleteAll();        log.info("BankApi {}", banksResponseList);        if (bankRepository.findAll().isEmpty()) {            banksResponseList.forEach(i -> {                bankRepository.save(new Banks(i.getCode(), i.getName()));            });        }    }    @Override    public RequestAccountDTO saveAccount(RequestAccountDTO requestAccountDTO) {       AppUser user = appUserService.getUserByEmail(UserUtils.getUserEmailFromContext());         Banks banks = bankRepository.findAllByCode(requestAccountDTO.getBankName());        Account account = Account.builder()                .accountName(requestAccountDTO.getAccountName())                .bankName(requestAccountDTO.getBankName())                .accountNumber(requestAccountDTO.getAccountNumber())                .appUser(user)                .build();                SubAccountRequest request = new SubAccountRequest(                "EVENT COMPANY",                banks.getBankCode(),                requestAccountDTO.getAccountNumber(),                "1");        String subaccount_code = subAccount(headers,request).getData().getSubaccount_code();        account.setSubaccount_code(subaccount_code);        return modelMapper.map(accountRepository.save(account),RequestAccountDTO.class);    }    @Override    public List<?> getBankCodeAndSend(String bankName, String accountNumber) {        Banks getCode = bankRepository.findAllByCode(bankName);        log.info("code {}", getCode);        PaymentUtils paymentUtils = new PaymentUtils();        headers.setBearerAuth(getSecretKey());        UriComponentsBuilder builder = UriComponentsBuilder                .fromUriString(paymentUtils.getPAY_STACK_URL_RESOLVE())                .queryParam("account_number", accountNumber)                .queryParam("bank_code", getCode.getBankCode())                .queryParam("currency", "NGN");        String url = builder.toUriString();        headers.setContentType(MediaType.APPLICATION_JSON);        HttpEntity<?> requestEntity = new HttpEntity<>(headers);        ResponseEntity<Object> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Object.class);        Object responseBody = responseEntity.getBody();        List<?> users = Collections.singletonList(responseBody);        return users;    }    @Override    public  BanksRepo subAccount(HttpHeaders headers, SubAccountRequest request) {        String url = "https://api.paystack.co/subaccount";        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();        body.add("business_name", request.getBusiness_name());        body.add("bank_code", request.getBank_code());        body.add("account_number", request.getAccount_number());        body.add("percentage_charge", request.getPercentage_charge());        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);        headers.setBearerAuth(getSecretKey());        headers.setCacheControl("no-cache");        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, headers);        ResponseEntity<BanksRepo> response = restTemplate.postForEntity(url, httpEntity, BanksRepo.class);        System.out.println(Objects.requireNonNull(response.getBody()).getMessage());        return response.getBody();    }}