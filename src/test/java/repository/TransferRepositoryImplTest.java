package repository;


import oleg.sichev.moneytransfer.model.Amount;
import oleg.sichev.moneytransfer.model.Card;
import oleg.sichev.moneytransfer.repository.impl.TransferRepositoryImpl;
import oleg.sichev.moneytransfer.web.request.TransferRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Testing transfer repository functionality.")
public class TransferRepositoryImplTest {

    @Autowired
    TransferRepositoryImpl transferRepository;

    public static final String OPERATION_ID = "1";
    public static final int ID = 1;
    public static final String CONFIRMATION_CODE = "4580";
    public static final TransferRequest TRANSFER_REQUEST = new TransferRequest("1111222233334444", "01/29", "345",
            "5555666677778888", new Amount(15000, "RUR"));
    public static final Card TEST_CARD_FROM = new Card("5555222200005555", "01/25", "345", new Amount(1000000, "RUR"));
    public static final Card TEST_CARD_TO = new Card("0000111100002222", "10/27", "097", new Amount(0, "RUR"));
    public static final String CARD_NUMBER_FROM = "5555222200005555";
    public static final String CARD_NUMBER_TO = "0000111100002222";

    @Test
    @DisplayName("Сохранение TransferRequest. Должно пройти успешно.")
    void saveTransfer_Test() {
        String operationId = OPERATION_ID;
        TransferRequest requestExp = TRANSFER_REQUEST;
        transferRepository.saveTransfer(operationId, requestExp);

        TransferRequest requestActual = transferRepository.getTransferRequest(operationId);
        assertEquals(requestExp, requestActual);

    }

    @Test
    @DisplayName("Получение TransferRequest. Должно пройти успешно.")
    void getTransferRequest_Test() {

        String operationId = OPERATION_ID;
        TransferRequest requestExp = TRANSFER_REQUEST;
        transferRepository.saveTransfer(operationId, requestExp);

        TransferRequest requestActual = transferRepository.getTransferRequest(operationId);
        assertEquals(requestExp, requestActual);

    }

    @Test
    @DisplayName("Получение ID операции. Должно пройти успешно.")
    void getId_Test() {
        int operationIdExpected = ID;
        int operationIdActual = transferRepository.getId();
        assertEquals(operationIdExpected, operationIdActual);


    }

    @Test
    @DisplayName("Сохранение кода подтверждения. Должно пройти успешно.")
    void saveCode_Test() {

        String operationId = OPERATION_ID;
        String codeExpected = CONFIRMATION_CODE;
        transferRepository.saveCode(operationId, codeExpected);

        String codeActual = transferRepository.getCode(operationId);
        assertEquals(codeExpected, codeActual);

    }

    @Test
    @DisplayName("Получение кода подтверждения. Должно пройти успешно.")
    void getCode_Test() {
        String operationId = OPERATION_ID;
        String codeExpected = CONFIRMATION_CODE;
        transferRepository.saveCode(operationId, codeExpected);

        String codeActual = transferRepository.getCode(operationId);
        assertEquals(codeExpected, codeActual);

    }

    @Test
    @DisplayName("Сохранение карты. Должно пройти успешно.")
    void saveCard_Test() {
        Card testCardFrom = TEST_CARD_FROM;
        Card testCardTo = TEST_CARD_TO;

        transferRepository.saveCard(testCardFrom, testCardTo);

        Card cardFromActual = transferRepository.getCard(CARD_NUMBER_FROM);
        Card cardToActual = transferRepository.getCard(CARD_NUMBER_TO);

        assertEquals(testCardFrom, cardFromActual);
        assertEquals(testCardTo, cardToActual);

    }

    @Test
    @DisplayName("Получение карты. Должно пройти успешно.")
    void getCard_Test() {

        Card testCardFrom = TEST_CARD_FROM;
        Card testCardTo = TEST_CARD_TO;

        transferRepository.saveCard(testCardFrom, testCardTo);

        Card cardFromActual = transferRepository.getCard(CARD_NUMBER_FROM);
        Card cardToActual = transferRepository.getCard(CARD_NUMBER_TO);

        assertEquals(testCardFrom, cardFromActual);
        assertEquals(testCardTo, cardToActual);

    }
}