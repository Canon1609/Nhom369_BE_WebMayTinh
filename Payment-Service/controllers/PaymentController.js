const { ProductCode, VnpLocale, dateFormat } = require("vnpay");
const { VNPay, ignoreLogger } = require("vnpay");
const vnpay = new VNPay({
  tmnCode: "BZEIVBIA",
  secureSecret: "BODD15T3WAGJFO5YLNNF1TG11BR2X8O4",
  vnpayHost: "https://sandbox.vnpayment.vn",
  testMode: true, // tùy chọn, ghi đè vnpayHost thành sandbox nếu là true
  hashAlgorithm: "SHA512", // tùy chọn
  /**
   * Bật/tắt ghi log
   * Nếu enableLog là false, loggerFn sẽ không được sử dụng trong bất kỳ phương thức nào
   */
  enableLog: true, // tùy chọn

  /**
   * Hàm `loggerFn` sẽ được gọi để ghi log khi enableLog là true
   * Mặc định, loggerFn sẽ ghi log ra console
   * Bạn có thể cung cấp một hàm khác nếu muốn ghi log vào nơi khác
   *
   * `ignoreLogger` là một hàm không làm gì cả
   */
  loggerFn: ignoreLogger, // tùy chọn

  /**
   * Tùy chỉnh các đường dẫn API của VNPay
   * Thường không cần thay đổi trừ khi:
   * - VNPay cập nhật đường dẫn của họ
   * - Có sự khác biệt giữa môi trường sandbox và production
   */
  endpoints: {
    paymentEndpoint: "paymentv2/vpcpay.html",
    queryDrRefundEndpoint: "merchant_webapi/api/transaction",
    getBankListEndpoint: "qrpayauth/api/merchant/get_bank_list",
  }, // tùy chọn
});
const orders = {};
const PaymentController = {
  CreatePaymentUrl: async (req, res) => {
    try {
      // tạo đơn hàng bằng cách gọi đến API của order-service
      const order = {
        id: req.body.id,
        amount: req.body.amount,
        status: req.body.status,
        products: req.body.products,
      };

      // Lưu đơn hàng vào in-memory
      orders[order.id] = {
        id: order.id,
        amount: order.amount,
        status: "pending",
      };

      if (!order) {
        return res.status(500).json({ message: "Lỗi khi tạo đơn hàng" });
      }
      // tạo URL thanh toán
      const vnpay = new VNPay({
        tmnCode: "BZEIVBIA",
        secureSecret: "BODD15T3WAGJFO5YLNNF1TG11BR2X8O4",
        vnpayHost: "https://sandbox.vnpayment.vn",
        testMode: true, // tùy chọn, ghi đè vnpayHost thành sandbox nếu là true
        hashAlgorithm: "SHA512", // tùy chọn,
        loggerFn: ignoreLogger, // tùy chọn
      });
      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1); // Ngày hết hạn là 24h sau
      // tạo url thanh toán
      const paymentUrl = vnpay.buildPaymentUrl({
        vnp_Amount: order.amount, // số tiền thanh toán (đơn vị là đồng)
        vnp_IpAddr: "127.0.0.1",
        vnp_TxnRef: `${order.id}`, // mã đơn hàng, có thể là id của đơn hàng trong hệ thống của bạn
        vnp_OrderInfo: `Thanh toán đơn hàng ${order.id}`,
        vnp_OrderType: ProductCode.Other,
        vnp_ReturnUrl: "http://localhost:5173/payment-return", // URL trả về sau khi thanh toán
        vnp_Locale: VnpLocale.VN, // 'vn' hoặc 'en'
        vnp_CreateDate: dateFormat(new Date()), // tùy chọn, mặc định là thời gian hiện tại
        vnp_ExpireDate: dateFormat(tomorrow), // tùy chọn
      });
      if (!paymentUrl) {
        return res.status(500).json({ message: "Lỗi khi tạo URL thanh toán" });
      }
      // trả về URL thanh toán cho client
      return res
        .status(200)
        .json({ message: "tạo trang thành công ", url: paymentUrl });
    } catch (error) {}
  },
  // xử lý result từ VNPay
  PaymentResult: async (req, res) => {
    try {
      // Xác thực return URL
      const verify = vnpay.verifyReturnUrl(
        { ...req.query },
        {
          logger: {
            type: "pick",
            fields: ["createdAt", "method", "isVerified", "message"],
            loggerFn: (data) => console.log(data),
          },
        }
      );

      if (!verify.isVerified) {
        return res.status(200).json({
          message: verify?.message ?? "Payment failed!",
          status: verify.isSuccess,
        });
      }
      const orderId = verify.vnp_TxnRef;
      const ipnAmount = verify.vnp_Amount;

      // Lấy đơn hàng từ in-memory
      const foundOrder = orders[orderId];
      console.log(`Found order: ${JSON.stringify(foundOrder)}`);

      if (!foundOrder) {
        return res.status(400).json({
          message: "Không tìm thấy đơn hàng",
          status: false,
        });
      }

      // Kiểm tra số tiền
      if (ipnAmount !== foundOrder.amount) {
        return res.status(400).json({
          message: "Số tiền không hợp lệ",
          status: false,
        });
      }

      // Cập nhật trạng thái đơn hàng
      foundOrder.status = "completed";
      orders[orderId] = foundOrder;
      console.log(`Order ${orderId} updated to completed`);

      // Tạo dữ liệu trả về cho client
      const paymentData = {
        orderId: orderId,
        paymentId: verify.vnp_TxnRef,
        amount: verify.vnp_Amount,
        paymentStatus: "success",
        transactionNo: verify.vnp_TransactionNo,
        payDate: verify.vnp_TransactionDate,
        bankCode: verify.vnp_BankCode,
        orderInfo: verify.vnp_OrderInfo,
      };

      return res.status(200).json({
        vnp_TransactionNo: verify.vnp_TransactionNo,
        vnp_TransactionStatus: verify.vnp_TransactionStatus,
        vnp_TransactionDate: verify.vnp_TransactionDate,
        vnp_TxnRef: verify.vnp_TxnRef,
        vnp_SecureHash: verify.vnp_SecureHash,
        vnp_SecureHashType: verify.vnp_SecureHashType,
        message: verify?.message ?? "Payment successful!",
        status: verify.isSuccess,
        paymentData: paymentData,
      });
    } catch (error) {
      console.error(`Return URL error: ${error}`);
      return res.status(400).json({ message: "Verify error", status: false });
    }
  },
  // xử lý IPN từ VNPAY
  CheckIPN: async (req, res) => {
    try {
      // Xác thực checksum (vẫn cần để VNPay không gửi lại IPN)
      const verify = vnpay.verifyIpnCall(
        { ...req.query },
        {
          logger: {
            type: "all",
            loggerFn: console.log,
          },
        }
      );

      if (!verify.isVerified) {
        return res.json(IpnFailChecksum);
      }

      // Không kiểm tra đơn hàng, trả về thành công để VNPay dừng gửi IPN
      return res.json(IpnSuccess);
    } catch (error) {
      console.error(`IPN error: ${error}`);
      return res.json(IpnUnknownError);
    }
  },
};

module.exports = PaymentController;
