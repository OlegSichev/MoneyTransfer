package oleg.sichev.moneytransfer.repository.impl;

import oleg.sichev.moneytransfer.model.Card;
import oleg.sichev.moneytransfer.repository.TransferRepository;
import oleg.sichev.moneytransfer.web.request.TransferRequest;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class TransferRepositoryImpl implements TransferRepository {

    private final Map <String, TransferRequest> transfers = new ConcurrentHashMap<>();
    private final Map<String, Card> cards = new ConcurrentHashMap<>();
    private final Map<String, String> codes = new ConcurrentHashMap<>();
    private final AtomicInteger operationId = new AtomicInteger();

    @Override
    public void saveTransfer(String operationId, TransferRequest request) {
        transfers.put(operationId, request);
    }

    @Override
    public TransferRequest getTransferRequest(String operationId) {
        return transfers.get(operationId);
    }


    @Override
    public int getId() {
        return operationId.incrementAndGet();
    }


    @Override
    public void saveCode(String operationId, String confirmationCode) {
        codes.put(operationId, confirmationCode);
    }

    @Override
    public String getCode(String operationId) {
        return   codes.get(operationId);
    }

    @Override
    public void saveCard(Card currentTestingCardFrom, Card currentTestingCardTo) {
        cards.put(currentTestingCardFrom.getCardNumber(),currentTestingCardFrom);
        cards.put(currentTestingCardTo.getCardNumber(), currentTestingCardTo);
    }

    @Override
    public Card getCard(String cardNumber) {
        return cards.get(cardNumber);
    }
}