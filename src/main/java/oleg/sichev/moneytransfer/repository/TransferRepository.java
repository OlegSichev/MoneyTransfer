package oleg.sichev.moneytransfer.repository;

import oleg.sichev.moneytransfer.model.Card;
import oleg.sichev.moneytransfer.web.request.TransferRequest;

public interface TransferRepository {

    void saveTransfer(String operationId, TransferRequest request);

    TransferRequest getTransferRequest(String operationId);

    int getId();

    void saveCode(String operationId, String confirmationCode);

    String getCode(String operationId);


    void saveCard(Card currentCardFrom , Card currentCardTo);

    Card getCard(String cardNumber);
}