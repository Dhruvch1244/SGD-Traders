# Capstone Backend API Documentation

This document provides a comprehensive overview of the backend API for the Capstone Trading Application. It is intended for developers working on the frontend or anyone who needs to interact with the backend services.

## Project Overview

This project is a Spring Boot application that provides a complete set of backend services for a modern trading platform. It is designed to be a flexible and scalable foundation, currently using a JSON-file-based data source for rapid development and easy testing. The API is designed to be intuitive and provides clear, consistent responses for both successful operations and errors.

## API Endpoints

---

### Authentication

Handles user registration, sign-in, and password management.

#### 1. Register a New Client

- **Endpoint:** `POST /api/auth/register`
- **Description:** Creates a new client account, initializes a wallet with a zero balance, and assigns a unique ID to their first identification document.
- **Request Body:**
  ```json
  {
    "name": "Diana Prince",
    "email": "diana.p@example.com",
    "dateOfBirth": "03-22-1985",
    "country": "USA",
    "postalCode": "20500",
    "identification": {
      "type": "SSN",
      "value": "555-00-1234"
    },
    "password": "WonderPass123!"
  }
  ```
- **Success Response (201 Created):**
  ```json
  {
      "success": true,
      "message": "Client registration successful",
      "data": { ...Client Object... }
  }
  ```
- **Error Response (400 Bad Request):**
  ```json
  {
    "email": "Please enter a valid email address.",
    "password": "PasswordCriteria of 8 char, 1 num, 1 spl char not met"
  }
  ```

#### 2. Sign In

- **Endpoint:** `POST /api/auth/signin`
- **Description:** Authenticates a user and returns their client data.
- **Request Body:**
  ```json
  {
    "email": "diana.p@example.com",
    "password": "WonderPass123!"
  }
  ```
- **Success Response (200 OK):**
  ```json
  {
      "success": true,
      "message": "Sign-in successful",
      "data": { ...Client Object... }
  }
  ```
- **Special Case (Preferences Not Set):**
  ```json
  {
      "success": false,
      "message": "Investment preferences not set",
      "data": { ...Client Object... }
  }
  ```
- **Error Response (401 Unauthorized):**
  ```json
  {
    "success": false,
    "message": "Invalid email or password"
  }
  ```

#### 3. Forgot Password (Verification)

- **Endpoint:** `POST /api/auth/forgot-password`
- **Description:** Verifies a user's identity before allowing a password change.
- **Request Body:**
  ```json
  {
    "email": "diana.p@example.com",
    "dateOfBirth": "03-22-1985"
  }
  ```
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Verification successful. You can now change your password."
  }
  ```

#### 4. Change Password

- **Endpoint:** `POST /api/auth/change-password`
- **Description:** Updates the password for a verified user. The new password must meet the same complexity requirements as during registration.
- **Request Body:**
  ```json
  {
    "email": "diana.p@example.com",
    "newPassword": "NewPassword123!"
  }
  ```
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Password changed successfully."
  }
  ```
- **Error Response (400 Bad Request):**
  ```json
  {
    "newPassword": "Password must be at least 8 characters long and contain at least one number, one letter, and one special character."
  }
  ```

---

### Profile Management

Handles fetching and updating user profile information.

#### 1. Get Profile

- **Endpoint:** `GET /api/profile/{clientId}`
- **Description:** Retrieves the public profile information for a given client.
- **Success Response (200 OK):**
  ```json
  { ...ProfileDto Object... }
  ```

#### 2. Update Profile

- **Endpoint:** `PUT /api/profile/{clientId}`
- **Description:** Updates a client's editable profile information (name, country, postal code). Only non-null fields will be updated.
- **Request Body:**
  ```json
  {
    "name": "Diana Prince Wayne",
    "postalCode": "20501"
  }
  ```
- **Success Response (200 OK):**
  ```json
  {
      "success": true,
      "message": "Profile updated successfully",
      "data": { ...ProfileDto Object... }
  }
  ```

#### 3. Update Identifications

- **Endpoint:** `PUT /api/profile/{clientId}/identification`
- **Description:** Replaces a client's entire list of identifications. To add a new ID, omit the `id` field. To update an existing one, include its `id`.
- **Request Body:**
  ```json
  [
    {
      "id": "some-existing-uuid",
      "type": "SSN",
      "value": "555-00-1235"
    },
    {
      "type": "Driver License",
      "value": "D1234567"
    }
  ]
  ```
- **Success Response (200 OK):**
  ```json
  {
      "success": true,
      "message": "Identifications updated successfully",
      "data": { ...ProfileDto Object... }
  }
  ```

---

### Wallet Management

Handles all wallet-related operations.

#### 1. Get Wallet Balance

- **Endpoint:** `GET /api/wallets/{clientId}`
- **Description:** Retrieves the current wallet details for a client.
- **Success Response (200 OK):**
  ```json
  {
    "clientId": "C001",
    "balance": 150000.0
  }
  ```

#### 2. Add Money to Wallet

- **Endpoint:** `POST /api/wallets/{clientId}/add`
- **Description:** Adds a specified amount to a client's wallet.
- **Request Body:**
  ```json
  {
    "amount": 5000
  }
  ```
- **Success Response (200 OK):**
  ```json
  {
      "success": true,
      "message": "Funds added successfully",
      "data": { ...Wallet Object... }
  }
  ```

---

### Trading

Handles the execution of buy and sell trades.

#### 1. Execute a Buy Trade

- **Endpoint:** `POST /api/trade/buy`
- **Description:** Executes a buy order, validates against wallet balance, and updates the client's portfolio.
- **Request Body:**
  ```json
  {
    "clientId": "C001",
    "instrumentId": "IN002",
    "quantity": 10
  }
  ```
- **Success Response (200 OK):**
  ```json
  {
      "success": true,
      "message": "Buy trade successful",
      "data": { ...Trade Object... }
  }
  ```
- **Error Response (400 Bad Request):**
  ```json
  {
    "success": false,
    "message": "Insufficient funds"
  }
  ```

#### 2. Execute a Sell Trade

- **Endpoint:** `POST /api/trade/sell`
- **Description:** Executes a sell order, validates against portfolio holdings, and updates the client's wallet and portfolio.
- **Request Body:**
  ```json
  {
    "clientId": "C001",
    "instrumentId": "IN001",
    "quantity": 10
  }
  ```
- **Success Response (200 OK):**
  ```json
  {
      "success": true,
      "message": "Sell trade successful",
      "data": { ...Trade Object... }
  }
  ```
- **Error Response (400 Bad Request):**
  ```json
  {
    "success": false,
    "message": "Insufficient holdings"
  }
  ```

---

### Market Data

Provides market-wide data for the trading page.

- **Get All Instruments for Trade Page:** `GET /api/market/instruments`
- **Get Top 5 Gainers:** `GET /api/market/top-gainers`
- **Get Top 5 Losers:** `GET /api/market/top-losers`
- **Get 5 Most Active Stocks:** `GET /api/market/most-active`

---

### Portfolio & Reports

Provides detailed portfolio information and generates reports.

#### 1. Get Portfolio Details

- **Endpoint:** `GET /api/portfolio/{clientId}/details`
- **Description:** Retrieves all data needed for the portfolio page, including grid data and chart data.
- **Success Response (200 OK):**
  ```json
  { ...PortfolioPageDto Object... }
  ```

#### 2. Get Trade History

- **Endpoint:** `GET /api/trades/{clientId}`
- **Description:** Retrieves the complete trade history for a client.
- **Success Response (200 OK):**
  ```json
  [ ...Array of Trade Objects... ]
  ```

#### 3. Generate a Report

- **Endpoint:** `GET /api/reports/{clientId}`
- **Description:** Gathers all necessary data for a client report, with an optional time filter for the trade history.
- **Query Parameters:** `timeScale` (optional) - `ONE_MONTH`, `SIX_MONTHS`, `ONE_YEAR`, `ALL_TIME` (default).
- **Example:** `GET /api/reports/C001?timeScale=ONE_MONTH`
- **Success Response (200 OK):**
  ```json
  { ...ReportDto Object... }
  ```

---

This documentation should serve as a complete guide for your frontend development. The API is now fully documented and ready for integration.
