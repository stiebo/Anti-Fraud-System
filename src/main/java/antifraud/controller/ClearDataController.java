package antifraud.controller;

import antifraud.exception.ClearDataErrorException;
import antifraud.exception.ErrorResponse;
import antifraud.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
public class ClearDataController {
    private final StolenCardRepository stolenCardRepository;
    private final SuspiciousIpRepository suspiciousIpRepository;
    private final TransactionLimitRepository transactionLimitRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Autowired
    public ClearDataController(StolenCardRepository stolenCardRepository,
                               SuspiciousIpRepository suspiciousIpRepository,
                               TransactionLimitRepository transactionLimitRepository,
                               TransactionRepository transactionRepository,
                               UserRepository userRepository) {
        this.stolenCardRepository = stolenCardRepository;
        this.suspiciousIpRepository = suspiciousIpRepository;
        this.transactionLimitRepository = transactionLimitRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Operation(
            summary = "Clear all data",
            description = "Delete the entire database and reset the API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully deleted all data",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"status\":\"All data has been deleted from server.\"}")
                    )),
            @ApiResponse(responseCode = "500", description = "Error deleting data",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))
    })
    // obviously this is for demo purposes only
    @DeleteMapping("/api/clear-data")
    public Map<String, String> clearData() {
        try {
            stolenCardRepository.deleteAll();
            suspiciousIpRepository.deleteAll();
            transactionLimitRepository.deleteAll();
            transactionRepository.deleteAll();
            userRepository.deleteAll();
        } catch (Exception e) {
            throw new ClearDataErrorException();
        }
        return Collections.singletonMap("status", "All data has been deleted from server.");
    }
}
