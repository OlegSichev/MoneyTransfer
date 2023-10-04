package oleg.sichev.moneytransfer.service;

import oleg.sichev.moneytransfer.web.request.ConfirmOperationRequest;
import oleg.sichev.moneytransfer.web.request.TransferRequest;
import oleg.sichev.moneytransfer.web.response.MoneyTransferResponse;
import org.springframework.stereotype.Service;

@Service
public interface TransferService {

    MoneyTransferResponse transfer(TransferRequest transferRequest);

    MoneyTransferResponse confirmOperation(ConfirmOperationRequest operationRequest);
}