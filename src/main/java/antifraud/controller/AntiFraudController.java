package antifraud.controller;

import antifraud.dto.*;
import antifraud.exception.ErrorResponse;
import antifraud.exception.ValidationErrorResponse;
import antifraud.service.AntiFraudService;
import antifraud.validations.CardNumberConstraint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/antifraud")
@Validated
public class AntiFraudController {
    private final AntiFraudService service;

    @Autowired
    public AntiFraudController(AntiFraudService service) {
        this.service = service;
    }

    @Operation(
            summary = "Post a transaction",
            description = "Submits a transaction for fraud analysis (Role: MERCHANT)",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PostTransactionOutDto.class)
                    )),
            @ApiResponse(responseCode = "400", description = "Validation failed for request parameter",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class)
                    ))
    })
    @PostMapping("/transaction")
    public PostTransactionOutDto postTransaction(@Valid @RequestBody PostTransactionInDto postTransactionInDto) {
        return service.postTransaction(postTransactionInDto);
    }

    @Operation(
            summary = "Send feedback",
            description = "Submits feedback on a transaction's validity (Role: SUPPORT)",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TransactionOutDto.class)
                    )),
            @ApiResponse(responseCode = "400", description = "Validation failed for request parameter",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class)
                    )),
            @ApiResponse(responseCode = "409", description = "Transaction feedback already exists",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )),
            @ApiResponse(responseCode = "422", description = "Wrong feedback",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class)
                    ))
    })
    @PutMapping("/transaction")
    public TransactionOutDto uploadTransactionFeedback(@Valid @RequestBody UpdateTransactionFeedback feedback) {
        return service.updateTransactionFeedback(feedback);
    }

    @Operation(
            summary = "Transaction history",
            description = "Retrieves the history of processed transactions (Role: SUPPORT)",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TransactionOutDto[].class)
                    ))
    })
    @GetMapping("/history")
    public TransactionOutDto[] getTransactionHistory() {
        return service.getTransactionHistory();
    }

    @Operation(
            summary = "Transaction history by card",
            description = "Retrieves transaction history for a specific card number (Role: SUPPORT)",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TransactionOutDto[].class)
                    )),
            @ApiResponse(responseCode = "400", description = "Validation failed for request parameter",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class)
                    )),
            @ApiResponse(responseCode = "404", description = "Card number not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))
    })
    @GetMapping("/history/{number}")
    public TransactionOutDto[] getTransactionHistoryByNumber(@PathVariable("number") @CardNumberConstraint String number) {
        //@LuhnCheck  (For demo purposes, use a custom constraint.)
        return service.getTransactionHistoryByNumber(number);
    }

    @Operation(
            summary = "Add suspicious IP",
            description = "Adds an IP address to the suspicious-IPs list (Role: SUPPORT)",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuspiciousIpOutDto.class)
                    )),
            @ApiResponse(responseCode = "400", description = "Validation failed for request parameter",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class)
                    )),
            @ApiResponse(responseCode = "409", description = "IP address already exists",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))
    })
    @PostMapping("/suspicious-ip")
    public SuspiciousIpOutDto postSuspiciousIp(@Valid @RequestBody SuspiciousIpInDto suspiciousIPInDto) {
        return service.postSuspiciousIp(suspiciousIPInDto);
    }

    @Operation(
            summary = "Remove suspicious IP",
            description = "Removes an IP address from the suspicious-IP list (Role: SUPPORT)",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"status\":\"IP 192.168.0.1 successfully removed!\"}")
                    )),
            @ApiResponse(responseCode = "400", description = "Validation failed for request parameter",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class)
                    )),
            @ApiResponse(responseCode = "404", description = "IP not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))
    })
    @DeleteMapping("/suspicious-ip/{ip}")
    public Map<String, String> deleteSuspiciousIp(
            @PathVariable("ip") @Pattern(regexp = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$") String ip) {
        service.deleteSuspiciousIp(ip);
        return Collections.singletonMap("status", "IP %s successfully removed!".formatted(ip));
    }

    @Operation(
            summary = "List suspicious IPs",
            description = "Retrieves the list of suspicious IP addresses (Role: SUPPORT)",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuspiciousIpOutDto[].class)
                    ))
    })
    @GetMapping("/suspicious-ip")
    public SuspiciousIpOutDto[] getSuscipiousIps() {
        return service.getSuspiciousIps();
    }

    @Operation(
            summary = "Add stolen card",
            description = "Adds a card number to the stolen cards list (Role: SUPPORT)",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = StolenCardOutDto.class)
                    )),
            @ApiResponse(responseCode = "400", description = "Validation failed for request parameter",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class)
                    )),
            @ApiResponse(responseCode = "409", description = "Stolen card already exists",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))
    })
    @PostMapping("/stolencard")
    public StolenCardOutDto postStolenCard(@Valid @RequestBody StolenCardInDto stolenCardInDto) {
        return service.postStolenCard(stolenCardInDto);
    }

    @Operation(
            summary = "Remove stolen card",
            description = "Removes a card number from the stolen cards list (Role: SUPPORT)",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"status\":\"Card 4000008449433403 successfully removed!\"}")
                    )),
            @ApiResponse(responseCode = "400", description = "Validation failed for request parameter",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class)
                    )),
            @ApiResponse(responseCode = "404", description = "Stolen card not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))
    })
    @DeleteMapping("/stolencard/{number}")
    public Map<String, String> deleteStolenCard(@PathVariable("number") @CardNumberConstraint String number) {
        service.deleteStolenCard(number);
        return Collections.singletonMap("status", "Card %s successfully removed!".formatted(number));
    }

    @Operation(
            summary = "List stolen cards",
            description = "Retrieves the list of stolen cards (Role: SUPPORT)",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = StolenCardOutDto[].class)
                    ))
    })
    @GetMapping("/stolencard")
    public StolenCardOutDto[] getStolenCards() {
        return service.getStolenCards();
    }
}
