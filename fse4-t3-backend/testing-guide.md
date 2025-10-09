## Testing Authentication in Insomnia

Follow these steps to test if authentication is working correctly:

### Step 1: Sign In to Get a Token

**Request Type:** POST  
**URL:** `http://a9e59e70f722c4b5db3d700a757ba4b6-580270519.ap-south-1.elb.amazonaws.com:3002/api/signin`  
**Headers:**
- Content-Type: application/json

**Request Body (JSON):**
```json
{
  "email": "alice.j@example.com",
  "password": "Password123!"
}
```

The response should include user information and you should see a cookie being set in the Cookies tab. You can also check your server logs to see if the token was created successfully.

### Step 2: Access a Protected Resource

**Request Type:** GET  
**URL:** `http://a9e59e70f722c4b5db3d700a757ba4b6-580270519.ap-south-1.elb.amazonaws.com:3002/api/profile` (or any other protected endpoint)  
**Headers:**
- Content-Type: application/json
- Authorization: Bearer YOUR_TOKEN_HERE

You can get your token from:
1. The Cookies tab in Insomnia after signing in
2. Server logs after a successful login
3. Browser cookies if testing through a browser

### Step 3: Verify Authentication Logic

1. If you try to access a protected endpoint without a token, you should get a 401 error
2. If you provide an invalid token, you should get a 403 error
3. If you provide a valid token, you should get a successful response

### Step 4: Test Token in Cookie vs. Authorization Header

The middleware accepts tokens from either:
1. The Authorization header in the format: `Bearer YOUR_TOKEN_HERE`
2. A cookie named `token`

Try both methods to ensure they work correctly.

### How to Extract the Token for Testing

When a successful sign-in occurs, the token is set as an HTTP-only cookie and also logged in the server console. You can:

1. Use the Chrome DevTools > Application > Cookies to see the token
2. Check your server logs for the "Token verified successfully" message which shows the decoded token
3. Use a cookie manager extension to extract the token value
