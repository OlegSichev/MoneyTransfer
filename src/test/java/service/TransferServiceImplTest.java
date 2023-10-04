package service;

import oleg.sichev.moneytransfer.model.Amount;
import oleg.sichev.moneytransfer.model.Card;
import oleg.sichev.moneytransfer.repository.impl.TransferRepositoryImpl;
import oleg.sichev.moneytransfer.service.impl.TransferServiceImpl;
import oleg.sichev.moneytransfer.web.request.ConfirmOperationRequest;
import oleg.sichev.moneytransfer.web.request.TransferRequest;
import oleg.sichev.moneytransfer.web.response.MoneyTransferResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@DisplayName("Testing transfer service functionality.")
public class TransferServiceImplTest {


    @InjectMocks
    TransferServiceImpl transferServiceImpl;

    @Mock
    TransferRepositoryImpl transferRepository;

    public static final String OPERATION_ID = "1";
    public static final String CONFIRMATION_CODE = "1234";
    public static final TransferRequest TRANSFER_REQUEST = new TransferRequest("1111222233334444", "01/29", "345",
            "5555666677778888", new Amount(15000, "RUR"));
    public static final Card TEST_CARD_FROM = new Card("1111222233334444", "01/25", "345", new Amount(1000000, "RUR"));
    public static final Card TEST_CARD_TO = new Card("5555666677778888", "10/27", "097", new Amount(0, "RUR"));
    public static final String CARD_NUMBER_FROM = "1111222233334444";
    public static final String CARD_NUMBER_TO = "5555666677778888";


    @Test
    @DisplayName("Трансфер данных перевода. Должно пройти успешно.")
    void transfer_Test() {
        //given
        TransferRequest transferRequest = TRANSFER_REQUEST;

        Card currentTestingCardFrom = TEST_CARD_FROM;
        Card currentTestingCardTo = TEST_CARD_TO;

        String operationId = "1";
        String confirmationCode = String.valueOf((int) (Math.random() * (9999 - 1000) + 1000));
        MoneyTransferResponse expected = new MoneyTransferResponse(operationId);

        //when
        when(transferRepository.getId()).thenReturn(Integer.valueOf(operationId));
        doNothing().when(transferRepository).saveCard(currentTestingCardFrom, currentTestingCardTo);
        doNothing().when(transferRepository).saveTransfer(operationId, TRANSFER_REQUEST);
        doNothing().when(transferRepository).saveCode(operationId, confirmationCode);


        //then
        MoneyTransferResponse transferResponseActual = transferServiceImpl.transfer(transferRequest);
        assertEquals(expected.getOperationId(), transferResponseActual.getOperationId());

    }

    @Test
    @DisplayName("Подтверждение операции. Должно пройти успешно.")
    void confirmOperation_Test() {
        //given
        ConfirmOperationRequest operationRequest = new ConfirmOperationRequest(OPERATION_ID, CONFIRMATION_CODE);
        String operationId = operationRequest.getOperationId();

        String operationCodeFromRepository = CONFIRMATION_CODE;
        MoneyTransferResponse responseExpected = new MoneyTransferResponse(operationId);
        TransferServiceImpl spy = Mockito.spy(transferServiceImpl);

        //when
        when(transferRepository.getCode(operationId)).thenReturn(operationCodeFromRepository);
        Mockito.doNothing().when(spy).cardBalanceChange(operationId);

        //then
        MoneyTransferResponse responseActual = spy.confirmOperation(operationRequest);
        assertEquals(responseExpected.getOperationId(), responseActual.getOperationId());


    }

    @Test
    @DisplayName("Преобразование копеек в рубли. Должно пройти успешно.")
    void amountValueConversion_Test() {
        Amount amount = new Amount(300000, "RUR");
        Amount amountExpected = new Amount(3000, "RUR");
        transferServiceImpl.amountValueConversion(amount);
        assertEquals(amount, amountExpected);
    }


    @ParameterizedTest
    @CsvSource({
            "1111222233330000,1111222233334444",
            "5555555555555555,6666666666666667",
            "5555666677778888,9999888877776666"
    })
    @DisplayName("Проверка номера карты отправителя и получателя. Должно пройти успешно.")
    void cardNumberVerification_Test(String cardFromNumber, String cardToNumber) {

        assertNotNull(cardFromNumber);
        assertNotNull(cardToNumber);
        assertThat(cardFromNumber).matches("[0-9]{16}");
        assertThat(cardToNumber).matches("[0-9]{16}");
        assertThat(cardFromNumber).isNotEqualTo(cardToNumber);
    }


    @ParameterizedTest
    @CsvSource({
            "111",
            "555",
            "345",
            "000",
            "987"
    })
    @DisplayName("Проверка CVC кода. Должно пройти успешно.")
    void cardCVVVerification_Test(String cardCVV) {
        assertNotNull(cardCVV);
        assertThat(cardCVV).containsOnlyDigits();
        assertThat(cardCVV).matches("\\d{3}");
    }


    @ParameterizedTest
    @CsvSource({
            "10/35",
            "12/25",
            "05/28",
            "11/29",
            "01/32"
    })
    @DisplayName("Проверка срока действия карты отправителя. Должно пройти успешно.")
    void cardDateVerificationVersionOne_Test(String cardFromValidTill) {

        assertThat(cardFromValidTill).isNotNull();

        final String[] yearAndMonth = cardFromValidTill.split("/");
        final int enteredMonth = Integer.parseInt(yearAndMonth[0]);
        final int enteredYear = Integer.parseInt(yearAndMonth[1]) + 2000;
        assertTrue(enteredMonth <= 12);
        assertTrue(enteredMonth >= 1);
        assertTrue(enteredYear > LocalDate.now().getYear());

    }

    @ParameterizedTest
    @CsvSource({
            "12/22",
    })
    @DisplayName("Проверка срока действия карты отправителя если год равен текущему году. Должно пройти успешно.")
    void cardDateVerificationVersionTwo_Test(String cardFromValidTill) {

        assertThat(cardFromValidTill).isNotNull();

        final String[] yearAndMonth = cardFromValidTill.split("/");
        final int enteredMonth = Integer.parseInt(yearAndMonth[0]);
        final int enteredYear = Integer.parseInt(yearAndMonth[1]) + 2000;

        assertTrue(enteredYear == LocalDate.now().getYear() && enteredMonth >= LocalDate.now().getMonthValue());
    }


    @ParameterizedTest
    @MethodSource("argsAmountFactory")
    @DisplayName("Проверка суммы и валюты операции. Должно пройти успешно.")
    void transferAmountVerification_Test(Amount amount) {

        assertNotNull(amount);

        int amountValue = amount.getValue();
        String currency = amount.getCurrency();

        assertTrue(amountValue > 0);
        assertThat(currency).isEqualTo("RUR");

    }

    static Stream<Amount> argsAmountFactory() {
        return Stream.of(new Amount(3400, "RUR"),
                new Amount(15000, "RUR"),
                new Amount(89000, "RUR"),
                new Amount(1, "RUR"));
    }


    @ParameterizedTest
    @ArgumentsSource(CardArgumentsProvider.class)
    @DisplayName("Проверка баланса карты отправителя. Должно пройти успешно.")
    void balanceFromCardVerification_Test(Card cardFrom, int commission, Amount amountOperation) {
     int balanceOnCard = cardFrom.getBalanceCard().getValue();
     int amountWithCommission = amountOperation.getValue() + commission;

     assertTrue(balanceOnCard >= amountWithCommission);

    }

    static class CardArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(
                    Arguments.of(new Card("1111222233334444", "11/29", "345", new Amount(2500000, "RUR")),
                       3700, new Amount(370000,"RUR")),
                    Arguments.of(new Card("5000500050005000", "01/28", "001", new Amount(30300, "RUR")),
                            300, new Amount(30000,"RUR")),
                    Arguments.of(new Card("9999999911119999", "05/30", "986", new Amount(38000, "RUR")),
                            62, new Amount(6200,"RUR"))
            );
        }
    }



    @Test
    @DisplayName("Изменение баланса на картах отправителя и получателя. Должно пройти успешно.")
    void cardBalanceChange() {
        //given
        String operationId = OPERATION_ID;
        TransferRequest requestData = new TransferRequest("1111222233334444", "01/29",
                "345", "5555666677778888", new Amount(15000, "RUR"));
        String cardNumberFrom = CARD_NUMBER_FROM;
        String cardNumberTo = CARD_NUMBER_TO;

        Card cardFrom = TEST_CARD_FROM;
        Card cardTo = TEST_CARD_TO;
        int newBalanceCardFromExpected = 984850;
        int newBalanceCardToExpected = 15000;

        //when
        when(transferRepository.getTransferRequest(operationId)).thenReturn(requestData);
        when(transferRepository.getCard(cardNumberFrom)).thenReturn(cardFrom);
        when(transferRepository.getCard(cardNumberTo)).thenReturn(cardTo);

        //then
        transferServiceImpl.cardBalanceChange(operationId);

        assertEquals(newBalanceCardFromExpected, cardFrom.getBalanceCard().getValue());
        assertEquals(newBalanceCardToExpected, cardTo.getBalanceCard().getValue());

    }
}