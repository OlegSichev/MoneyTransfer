package com.example.moneytransferservice.controller;

import oleg.sichev.moneytransfer.service.TransferService;
import oleg.sichev.moneytransfer.web.request.ConfirmOperationRequest;
import oleg.sichev.moneytransfer.web.request.TransferRequest;
import oleg.sichev.moneytransfer.web.response.MoneyTransferResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class is a rest controller that accepts requests from the client and passes data to the service.
 */


@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class MoneyTransferController {

    private final TransferService transferService;

    @Autowired
    public MoneyTransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/transfer")
    public MoneyTransferResponse transfer (@RequestBody TransferRequest transferRequest) {
        MoneyTransferResponse response = transferService.transfer(transferRequest);
        return response;
    }

    @PostMapping("/confirmOperation")
    public MoneyTransferResponse confirmOperation (@RequestBody ConfirmOperationRequest operationRequest) {
        MoneyTransferResponse response = transferService.confirmOperation(operationRequest);
        return response;
    }
}