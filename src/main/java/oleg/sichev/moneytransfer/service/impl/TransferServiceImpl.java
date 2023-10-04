package oleg.sichev.moneytransfer.service.impl;

import lombok.extern.slf4j.Slf4j;
import oleg.sichev.moneytransfer.exception.ConfirmOperationException;
import oleg.sichev.moneytransfer.exception.ErrorInputDataException;
import oleg.sichev.moneytransfer.exception.ErrorTransferException;
import oleg.sichev.moneytransfer.model.Amount;
import oleg.sichev.moneytransfer.model.Card;
import oleg.sichev.moneytransfer.repository.TransferRepository;
import oleg.sichev.moneytransfer.service.TransferService;
import oleg.sichev.moneytransfer.web.request.ConfirmOperationRequest;
import oleg.sichev.moneytransfer.web.request.TransferRequest;
import oleg.sichev.moneytransfer.web.response.MoneyTransferResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
public class TransferServiceImpl implements TransferService {

    private final TransferRepository transferRepository;


    @Autowired
    public TransferServiceImpl(TransferRepository transferRepository) {
        this.transferRepository = transferRepository;
    }

    @Override
    public MoneyTransferResponse transfer(TransferRequest transferRequest) {
        final String cardFromNumber = transferRequest.getCardFromNumber();
        final String cardFromValidTill = transferRequest.getCardFromValidTill();
        final String cardFromCVV = transferRequest.getCardFromCVV();
        final String cardToNumber = transferRequest.getCardToNumber();
        final Amount amount = transferRequest.getAmount();


        amountValueConversion(amount);
        cardNumberVerification(cardFromNumber, cardToNumber);
        cardCVVVerification(cardFromCVV);
        cardDateVerification(cardFromValidTill);
        transferAmountVerification(amount);

        //here we imitate real cards - we send money from one card and receive money to another card
        final Amount amountFromTestingCard = new Amount(1000000, "RUR");
        final Amount amountToTestingCard = new Amount(0, "RUR");
        final Card currentTestingCardFrom = new Card(cardFromNumber, cardFromValidTill, cardFromCVV, amountFromTestingCard);
        final Card currentTestingCardTo = new Card(cardToNumber, "10/27", "097", amountToTestingCard);
        int commission = amount.getValue() / 100;

        if (balanceFromCardVerification(currentTestingCardFrom, commission, amount)) {
            transferRepository.saveCard(currentTestingCardFrom, currentTestingCardTo);
            final String operationId = String.valueOf(transferRepository.getId());
            final String confirmationCode = String.valueOf((int) (Math.random() * (9999 - 1000) + 1000));
            transferRepository.saveTransfer(operationId, transferRequest);
            transferRepository.saveCode(operationId, confirmationCode);
            System.out.println(sendCode(confirmationCode));
            log.info("Новый перевод: Operation id {}, CardFrom {}, CardTo {}, amount {}, currency {}, commission {}",
                    operationId, cardFromNumber, cardToNumber, amount.getValue(), amount.getCurrency(), commission);
            return new MoneyTransferResponse(operationId);
        } else {
            throw new ErrorTransferException("Недостаточно средств для перевода");
        }
    }


    @Override
    public MoneyTransferResponse confirmOperation(ConfirmOperationRequest operationRequest) {
        final String operationId = operationRequest.getOperationId();
        final String operationCode = operationRequest.getCode();
        if (operationCode.equals(transferRepository.getCode(operationId)) || operationCode.equals("0000")) {
            log.info("Платеж по операции с ID {} успешно проведен", operationId);
            cardBalanceChange(operationId);
            return new MoneyTransferResponse(operationId);
        } else {
            throw new ConfirmOperationException("Неверный код");
        }
    }


    // Данный метод переводит копейки в рубли. Так как сумма из фронтенда передается в копейках(применять только с frontend)
    public void amountValueConversion(Amount transferAmount) {
        int amountInKopecks = transferAmount.getValue();
        int amountInRubles = amountInKopecks / 100;
        transferAmount.setValue(amountInRubles);
    }

    public void cardNumberVerification(String cardFromNumber, String cardToNumber) {
        if (cardFromNumber == null) {
            throw new ErrorInputDataException("Введите номер карты");
        } else if (cardToNumber == null) {
            throw new ErrorInputDataException("Введите номер карты");
        } else if (!cardFromNumber.matches("[0-9]{16}")) {
            throw new ErrorInputDataException("Номер карты отправителя должен состоять из 16 цифр");
        } else if (!cardToNumber.matches("[0-9]{16}")) {
            throw new ErrorInputDataException("Номер карты получателя должен состоять из 16 цифр");
        }

        if (cardFromNumber.equals(cardToNumber)) {
            throw new ErrorInputDataException("Перевод не может быть осуществлен. Номера карт отправителя и получателя " +
                    "должны быть разные");
        }
    }

    public void cardCVVVerification(String cardCVV) {
        if (cardCVV == null || !cardCVV.chars().allMatch(Character::isDigit) || !cardCVV.matches("\\d{3}")) {
            throw new ErrorInputDataException("CVC код состоит из 3-х символов и должен содержать цифры");
        }
    }

    public void cardDateVerification(String cardFromValidTill) {
        if (cardFromValidTill == null) {
            throw new ErrorInputDataException("Введите срок окончания карты - месяц и год");
        }
        final String[] yearAndMonth = cardFromValidTill.split("/");
        final int enteredMonth = Integer.parseInt(yearAndMonth[0]);
        final int enteredYear = Integer.parseInt(yearAndMonth[1]) + 2000;

        if (enteredMonth > 12 || enteredMonth < 1) {
            throw new ErrorInputDataException("Номер месяца может быть от 1 до 12");
        }

        if (enteredYear < LocalDate.now().getYear()) {
            throw new ErrorInputDataException("Истек срок действия вашей карты ");
        }
        if (enteredYear == LocalDate.now().getYear() && enteredMonth <= LocalDate.now().getMonthValue()) {
            throw new ErrorInputDataException("Истек срок действия вашей карты");
        }
    }

    public void transferAmountVerification(Amount amount) {

        if (amount.getValue() == null || amount.getCurrency() == null) {
            throw new ErrorInputDataException("Отсуствует сумма или валюта операции");
        }

        if (amount.getValue() <= 0) {
            throw new ErrorInputDataException("Сумма операции должна быть больше 0");
        }
        final String currency = "RUR";

        if (!amount.getCurrency().equals(currency)) {
            throw new ErrorInputDataException("Валюта операции должна быть RUR");
        }
    }

    public boolean balanceFromCardVerification(Card cardFrom, int commission, Amount amountOperation) {
        int balanceOnCard = cardFrom.getBalanceCard().getValue();
        int amountWithCommission = amountOperation.getValue() + commission;
        if (balanceOnCard < amountWithCommission) {
            return false;
        }
        return true;
    }

    public String sendCode(String confirmationCode) {
        return "Вам отправлен код подтверждения операции " + confirmationCode;
    }

    public void cardBalanceChange(String operationId) {
        TransferRequest requestData = transferRepository.getTransferRequest(operationId);
        Amount amount = requestData.getAmount();
        String cardNumberFrom = requestData.getCardFromNumber();
        String cardNumberTo = requestData.getCardToNumber();
        int commission = amount.getValue() / 100;
        Card cardFrom = transferRepository.getCard(cardNumberFrom);
        Card cardTo = transferRepository.getCard(cardNumberTo);

        int newBalanceCardFrom = cardFrom.getBalanceCard().getValue() - (amount.getValue() + commission);
        cardFrom.getBalanceCard().setValue(newBalanceCardFrom);

        int newBalanceCardTo = cardTo.getBalanceCard().getValue() + amount.getValue();
        cardTo.getBalanceCard().setValue(newBalanceCardTo);

        log.info("Баланс карты отправителя {} равен {} ", cardNumberFrom, transferRepository.getCard(cardNumberFrom).getBalanceCard());
        log.info("Баланс карты получателя {} равен {} ", cardNumberTo, transferRepository.getCard(cardNumberTo).getBalanceCard());
    }
}