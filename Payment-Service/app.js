const express = require("express");
const cors = require("cors");
const app = express();
app.use(cors());
const PORT = 8088;
const PaymentController = require("./controllers/PaymentController.js");
const {
  IpnFailChecksum,
  IpnOrderNotFound,
  IpnInvalidAmount,
  InpOrderAlreadyConfirmed,
  IpnUnknownError,
  IpnSuccess,
} = require("vnpay");
const { VNPay, ignoreLogger } = require("vnpay");
// Middleware
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
// Cấu hình EJS
app.set("view engine", "ejs");
app.set("views", "./views");




// In-memory storage cho đơn hàng
const orders = {};
app.post("/api/create-payment",  PaymentController.CreatePaymentUrl);
//  xác thực IPN từ VNPay
// Route xử lý IPN (tối thiểu, không kiểm tra)
app.get('/vnpay-ipn', PaymentController.CheckIPN);

// Route xử lý return URL
app.get('/payment-result', PaymentController.PaymentResult);

// Start the server
app.listen(PORT, () => {
  console.log(`Server is running on http://localhost:${PORT}`);
});
