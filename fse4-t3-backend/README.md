# Capstone Backend API Documentation

This document provides a comprehensive overview of the backend API for the Capstone Trading Application. It is intended for developers working on the frontend or anyone who needs to interact with the backend services.

## Project Overview

This project is a Spring Boot application that provides a complete set of backend services for a modern trading platform. It is designed to be a flexible and scalable foundation, using an Oracle database for persistence. The API is designed to be intuitive and provides clear, consistent responses for both successful operations and errors.

## API Endpoints

---

### Authentication (`/api/auth`)

Handles user registration, sign-in, and password management.

- **`POST /signin`**: Authenticates a user.
  - **Request Body**: `SignInRequest` (`email`, `password`)
  - **Success Response**: `ApiResponse` with client data. Returns a special message if investment preferences are not set.
  - **Error Response**: 401 Unauthorized for invalid credentials.

- **`POST /register`**: Creates a new client account.
  - **Request Body**: `RegistrationRequest`
  - **Success Response**: 201 Created with `ApiResponse` containing the new client data.
  - **Error Response**: 400 Bad Request if email/identification exists or data is invalid.

- **`POST /forgot-password`**: Verifies user identity for password reset.
  - **Request Body**: `ForgotPasswordRequestDto` (`email`, `dateOfBirth`)
  - **Success Response**: `ApiResponse` with a success message.
  - **Error Response**: 401 Unauthorized for invalid credentials.

- **`POST /change-password`**: Updates the password for a verified user.
  - **Request Body**: `ChangePasswordRequestDto` (`email`, `newPassword`)
  - **Success Response**: `ApiResponse` with a success message.
  - **Error Response**: 404 Not Found if the user does not exist.

---

### Profile Management (`/api/profile`)

HHandles fetching and updating user profile information.

- **`GET /{clientId}`**: Retrieves the public profile information for a given client.
  - **Success Response**: `ProfileDto` object.
  - **Error Response**: 404 Not Found if the client does not exist.

- **`PUT /{clientId}`**: Updates a client's editable profile information (name, country, postal code).
  - **Request Body**: `UpdateProfileDto`
  - **Success Response**: `ApiResponse` with the updated `ProfileDto`.
  - **Error Response**: 404 Not Found if the client does not exist.

- **`PUT /{clientId}/identification`**: Replaces a client's entire list of identifications.
  - **Request Body**: `List<ClientIdentification>`
  - **Success Response**: `ApiResponse` with the updated `ProfileDto`.
  - **Error Response**: 400 Bad Request for duplicate or already used identification values.

---

### Investment Preferences (`/api/preferences`)

Manages user investment preferences.

- **`GET /{clientId}`**: Retrieves the investment preferences for a given client.
  - **Success Response**: `InvestmentPreferences` object.
  - **Error Response**: 404 Not Found if preferences are not set.

- **`GET /data`**: Retrieves the lookup data for creating the preferences form (risk tolerances, income categories, etc.).
  - **Success Response**: `InvestmentPreferencesDataDto` object.

- **`POST /`**: Saves or updates the investment preferences for a client.
  - **Request Body**: `InvestmentPreferencesDto`
  - **Success Response**: `ApiResponse` with the saved `InvestmentPreferences` object.

---

### Wallet Management (`/api/wallets`)

Handles all wallet-related operations.

- **`GET /{clientId}`**: Retrieves the current wallet details for a client.
  - **Success Response**: `Wallet` object.
  - **Error Response**: 404 Not Found if the wallet does not exist.

- **`POST /{clientId}/add`**: Adds a specified amount to a client's wallet.
  - **Request Body**: `{ "amount": double }`
  - **Success Response**: `ApiResponse` with the updated `Wallet` object.

---

### Market Data (`/api/market`)

Provides general and instrument-specific market data.

- **`GET /instruments`**: Retrieves a list of all tradable instruments.
  - **Query Param**: `clientId` (optional) - If provided, the response will include an `isInPortfolio` flag for each instrument.
  - **Success Response**: `List<TradePageInstrumentDto>`

- **`GET /instruments/{instrumentId}/price`**: Retrieves the latest price for a specific instrument.
  - **Success Response**: `Price` object.
  - **Error Response**: 404 Not Found if the instrument or price does not exist.

- **`GET /top-gainers`**: Retrieves the top 5 performing instruments (gainers).
  - **Success Response**: `List<MarketPerformerDto>`

- **`GET /top-losers`**: Retrieves the bottom 5 performing instruments (losers).
  - **Success Response**: `List<MarketPerformerDto>`

- **`GET /most-active`**: Retrieves the 5 most actively traded instruments by volume.
  - **Success Response**: `List<MostActiveStockDto>`

---

### Instruments (`/api/instruments`)

Provides access to the master list of all instruments.

- **`GET /`**: Retrieves all instruments available in the system.
  - **Success Response**: `List<Instrument>`

---

### Trading (`/api/trade`)

Handles the execution of buy and sell trades.

- **`POST /buy`**: Executes a buy order.
  - **Request Body**: `TradeRequestDto` (`clientId`, `instrumentId`, `quantity`)
  - **Success Response**: `TradeResponseDto` with the executed `Trade` object.
  - **Error Response**: 400 Bad Request for insufficient funds or invalid request.

- **`POST /sell`**: Executes a sell order.
  - **Request Body**: `TradeRequestDto` (`clientId`, `instrumentId`, `quantity`)
  - **Success Response**: `TradeResponseDto` with the executed `Trade` object.
  - **Error Response**: 400 Bad Request for insufficient holdings or invalid request.

---

### Portfolio & Reports

Provides detailed portfolio information and generates reports.

- **`GET /api/portfolios/{clientId}/summary`**: Retrieves a summary of the client's portfolio for the dashboard.
  - **Success Response**: `PortfolioSummary` object.

- **`GET /api/portfolio/{clientId}/details`**: Retrieves detailed data for the portfolio page, including holdings and allocation charts.
  - **Success Response**: `PortfolioPageDto` object.

- **`GET /api/trades/{clientId}`**: Retrieves the complete trade history for a client.
  - **Success Response**: `List<Trade>`

- **`GET /api/reports/{clientId}`**: Generates a comprehensive report for a client.
  - **Query Param**: `timeScale` (optional) - `ONE_MONTH`, `SIX_MONTHS`, `ONE_YEAR`, `ALL_TIME` (default).
  - **Success Response**: `ReportDto` object.
