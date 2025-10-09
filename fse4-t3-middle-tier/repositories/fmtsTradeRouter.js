// ...existing code...
router.post("/", (request, response) => {
  console.log("POST /fmts/trades called - Route matched!");
  const order = readOrder(request);
  console.log("Received FMTS trade payload:", order);

  // Accept all trades for testing if token is 'admin_test123'
  if (order.token === "admin_test123") {
    console.log("Test admin token detected, bypassing all authentication.");
    // Simulate a successful trade response
    return response.status(200).json({
      tradeId: "test_trade_" + Date.now(),
      executionPrice: order.targetPrice,
      cashValue: order.targetPrice * order.quantity,
      instrumentId: order.instrumentId,
      quantity: order.quantity,
      direction: order.direction,
      clientId: order.clientId,
      email: order.email,
      status: "EXECUTED"
    });
  }

  // ...existing code for validation and authentication...
});
// ...existing code...
