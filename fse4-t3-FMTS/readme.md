 # FMTS API Documentation

This document provides details on the API endpoints for the Fidelity Main Trading System (FMTS).

---

## 1. Client Verification Endpoints

Base Path: `/fmts/client`

### POST /

Verifies a client's identity and generates a token for future trades.

**Request Body:**

```json
{
  "email": "user@example.com",
  "clientId": 123456789 // Optional
}
```

**Responses:**

*   **`200 OK`**: Successful verification. Returns the client object with a generated token.

    ```json
    {
      "email": "user@example.com",
      "clientId": 987654321,
      "token": 1234567890
    }
    ```

*   **`406 Not Acceptable`**: The provided email is invalid.
*   **`404 Not Found`**: An unexpected error occurred.

---

## 2. Trade Endpoints

Base Path: `/fmts/trades`

### POST /trade

Executes a trade order.

**Request Body:**

```json
{
  "instrumentId": "INST001",
  "quantity": 100,
  "targetPrice": 150.50,
  "direction": "B", // 'B' for Buy, 'S' for Sell
  "clientId": 987654321,
  "email": "user@example.com",
  "token": 1234567890
}
```

**Responses:**

*   **`200 OK`**: The trade was successfully executed. Returns the trade object.

    ```json
    {
        "instrumentId": "INST001",
        "quantity": 100,
        "executionPrice": 150.50,
        "direction": "B",
        "clientId": 987654321,
        "order": { ... },
        "tradeId": "xyz-abc-123",
        "cashValue": 15200.50
    }
    ```

*   **`400 Bad Request`**: The trade was not executed (e.g., target price not within tolerance).

    ```json
    {
      "message": "Target price not within tolerance range of +/- $5."
    }
    ```

*   **`401 Unauthorized`**: Invalid token, or `clientId`/`token` mismatch.

    ```json
    {
      "message": "Unauthorized: Invalid token"
    }
    ```

*   **`406 Not Acceptable`**: Invalid email format.

    ```json
    {
      "message": "Not Acceptable: Invalid email"
    }
    ```

### GET /instruments

Retrieves a list of all available instruments.

**Responses:**

*   **`200 OK`**: Returns an array of instrument objects.

    ```json
    [
      {
        "instrumentId": "INST001",
        "name": "Instrument Name",
        "categoryId": "STOCKS"
      }
    ]
    ```

*   **`204 No Content`**: No instruments found.

### GET /instruments/:category

Retrieves a list of instruments by category.

**URL Parameters:**

*   `category`: The category of instruments to retrieve.

**Responses:**

*   **`200 OK`**: Returns an array of instrument objects.
*   **`204 No Content`**: No instruments found for the given category.

### GET /prices

Retrieves a list of all current prices.

**Responses:**

*   **`200 OK`**: Returns an array of price objects.

    ```json
    [
      {
        "askPrice": 150.75,
        "bidPrice": 150.25,
        "instrument": {
          "instrumentId": "INST001",
          "name": "Instrument Name",
          "categoryId": "STOCKS"
        }
      }
    ]
    ```

*   **`204 No Content`**: No prices found.

### GET /prices/:category

Retrieves a list of prices by category.

**URL Parameters:**

*   `category`: The category of prices to retrieve.

**Responses:**

*   **`200 OK`**: Returns an array of price objects.
*   **`204 No Content`**: No prices found for the given category.

### POST /prices

Retrieves prices for a given category, requiring a valid token.

**Request Body:**

```json
{
  "token": 1234567890
}
```

**Query Parameters:**

*   `category`: (Optional) The category of prices to retrieve.

**Responses:**

*   **`200 OK`**: Returns an array of price objects.
*   **`204 No Content`**: No prices found.
*   **`401 Unauthorized`**: Invalid token.

---

## 3. General Endpoints

### GET /

Displays the home page.

**Responses:**

*   **`200 OK`**: Renders the main application page.

### GET /users

Responds with a placeholder message.

**Responses:**

*   **`200 OK`**: Returns the string `"respond with a resource"`.

---

## 4. Error Responses

*   **`404 Not Found`**: Returned for any path that does not match a defined endpoint.

    ```
    Uuuups wrong path!
    ```
