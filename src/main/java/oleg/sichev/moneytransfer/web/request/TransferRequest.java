package oleg.sichev.moneytransfer.web.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import oleg.sichev.moneytransfer.model.Amount;

@Data
@AllArgsConstructor
public class TransferRequest {

    private String cardFromNumber;

    private String cardFromValidTill;

    private String cardFromCVV;

    private String cardToNumber;

    private Amount amount;

}