package com.example.demo.controller;

import com.example.demo.dto.trade.TradeRequestDto;
import com.example.demo.models.Trade;
import com.example.demo.service.TradeExecutionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/trade")
public class TradeExecutionController {

    private final TradeExecutionService tradeExecutionService;

    public TradeExecutionController(TradeExecutionService tradeExecutionService) {
        this.tradeExecutionService = tradeExecutionService;
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buy(@Valid @RequestBody TradeRequestDto tradeRequest) {
        try {
            Trade trade = tradeExecutionService.executeBuyTrade(tradeRequest);
            return ResponseEntity.ok(new TradeResponse(trade));
        } catch (TradeExecutionService.UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorMessage("Unauthorized: Invalid token"));
        } catch (TradeExecutionService.InvalidEmailException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(new ErrorMessage("Not Acceptable: Invalid email"));
        } catch (TradeExecutionService.PriceToleranceException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorMessage(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorMessage(e.getMessage()));
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<?> sell(@Valid @RequestBody TradeRequestDto tradeRequest) {
        try {
            Trade trade = tradeExecutionService.executeSellTrade(tradeRequest);
            return ResponseEntity.ok(new TradeResponse(trade));
        } catch (TradeExecutionService.UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorMessage("Unauthorized: Invalid token"));
        } catch (TradeExecutionService.InvalidEmailException e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(new ErrorMessage("Not Acceptable: Invalid email"));
        } catch (TradeExecutionService.PriceToleranceException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorMessage(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorMessage(e.getMessage()));
        }
    }

    // Error message wrapper
    static class ErrorMessage {
        public String message;
        public ErrorMessage(String message) { this.message = message; }
    }

    // Trade response wrapper as per spec
    static class TradeResponse {
        public String instrumentId;
        public int quantity;
        public double executionPrice;
        public String direction;
        public String clientId;
        public Object order;
        public String tradeId;
        public double cashValue;
        public TradeResponse(Trade trade) {
            this.instrumentId = trade.getInstrumentId();
            this.quantity = trade.getQuantity();
            this.executionPrice = trade.getExecutionPrice();
            this.direction = trade.getDirection();
            this.clientId = trade.getClientId();
            this.order = trade.getOrder();
            this.tradeId = trade.getTradeId();
            this.cashValue = trade.getCashValue();
        }
    }
}