package antifraud.controller;

import antifraud.dto.*;
import antifraud.service.AntifraudService;
import antifraud.validations.CardNumberConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/antifraud")
@Validated
public class AntifraudController {
    private final AntifraudService service;

    @Autowired
    public AntifraudController(AntifraudService service) {
        this.service = service;
    }

    @PostMapping("/transaction")
    public PostTransactionOutDto postTransaction(@Valid @RequestBody PostTransactionInDto postTransactionInDto) {
        return service.postTransaction(postTransactionInDto);
    }

    @PutMapping("/transaction")
    public TransactionOutDto uploadTransactionFeedback(@Valid @RequestBody UpdateTransactionFeedback feedback) {
        return service.updateTransactionFeedback(feedback);
    }

    @GetMapping("/history")
    public TransactionOutDto[] getTransactionHistory() {
        return service.getTransactionHistory();
    }

    @GetMapping("/history/{number}")
    public TransactionOutDto[] getTransactionHistoryByNumber(@PathVariable("number") @CardNumberConstraint String number) {
        //@LuhnCheck  (For demo purposes, use a custom constraint.)
        return service.getTransactionHistoryByNumber(number);
    }

    @PostMapping("/suspicious-ip")
    public SuspiciousIpOutDto postSuspiciousIp(@Valid @RequestBody SuspiciousIpInDto suspiciousIPInDto) {
        return service.postSuspiciousIp(suspiciousIPInDto);
    }

    @DeleteMapping("/suspicious-ip/{ip}")
    public Map<String, String> deleteSuspiciousIp(
            @PathVariable("ip") @Pattern(regexp = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$") String ip) {
        service.deleteSuspiciousIp(ip);
        return Collections.singletonMap("status", "IP %s successfully removed!".formatted(ip));
    }

    @GetMapping("/suspicious-ip")
    public SuspiciousIpOutDto[] getSuscipiousIps() {
        return service.getSuspiciousIps();
    }

    @PostMapping("/stolencard")
    public StolenCardOutDto postStolenCard(@Valid @RequestBody StolenCardInDto stolenCardInDto) {
        return service.postStolenCard(stolenCardInDto);
    }

    @DeleteMapping("/stolencard/{number}")
    public Map<String, String> deleteStolenCard(@PathVariable("number") @CardNumberConstraint String number) {
        service.deleteStolenCard(number);
        return Collections.singletonMap("status", "Card %s successfully removed!".formatted(number));
    }

    @GetMapping("/stolencard")
    public StolenCardOutDto[] getStolenCards() {
        return service.getStolenCards();
    }
}
