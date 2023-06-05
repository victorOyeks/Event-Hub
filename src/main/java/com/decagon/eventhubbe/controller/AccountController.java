package com.decagon.eventhubbe.controller;

import com.decagon.eventhubbe.dto.request.RequestAccountDTO;
import com.decagon.eventhubbe.dto.response.APIResponse;
import com.decagon.eventhubbe.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/bank/")
@RequiredArgsConstructor

public class AccountController {

    private final AccountService accountService;

    @GetMapping("/getName")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<?> getAccountName(@RequestParam String bankName,
                                            @RequestParam String accountNumber) {
        return ResponseEntity.ok().body(
                accountService.getBankCodeAndSend(bankName, accountNumber));
    }
    @PostMapping("/create-account")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<APIResponse<RequestAccountDTO>> saveAccount(@RequestBody RequestAccountDTO requestAccountDTO) {
        APIResponse<RequestAccountDTO> apiResponse = new APIResponse<>(accountService.saveAccount(requestAccountDTO));
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

}
