package bookstore.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;

import bookstore.dto.CheckoutRequest;
import bookstore.service.ECPayService;
import bookstore.service.EmailService;
import bookstore.service.OrderService;
import bookstore.bean.UserBean;
import jakarta.servlet.http.HttpServletRequest;
import bookstore.bean.OrderItem;
import bookstore.bean.Orders;

@Controller
@RequestMapping("/orders")
public class CheckoutController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ECPayService ecPayService;

    @Autowired
    private EmailService emailService;

    // 建立訂單並根據付款方式產生綠界參數
    @PostMapping("/checkout")
    @ResponseBody
    public Map<String, Object> checkout(@RequestBody CheckoutRequest checkoutRequest, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            response.put("success", false);
            response.put("message", "請先登入");
            return response;
        }

        try {
            Orders order = orderService.createOrderFromCart(userId, checkoutRequest);

            response.put("success", true);
            response.put("orderId", order.getOrderId());

            response.put("orderId", order.getOrderId());

            System.out.println("DEBUG: Payment Method in Order: " + order.getPaymentMethod());

            // 建立要傳入綠界的商品明細字串
            StringBuilder itemNameBuilder = new StringBuilder();
            List<OrderItem> items = orderService.getOrderItemsByOrderId(order.getOrderId());
            if (items != null && !items.isEmpty()) {
                for (OrderItem item : items) {
                    if (itemNameBuilder.length() > 0) {
                        itemNameBuilder.append("#");
                    }
                    String bookName = "未知書籍";
                    if (item.getBooksBean() != null) {
                        bookName = item.getBooksBean().getBookName();
                    }
                    itemNameBuilder.append(bookName).append(" x ").append(item.getQuantity());
                }
            }
            System.out.println("DEBUG: Generated ECPay ItemName: " + itemNameBuilder.toString());

            // 發送訂單通知信 (移至此處以確保 items 已被查詢並放入 order)
            try {
                System.out.println("DEBUG: Preparing to send email. Items count from DB: "
                        + (items != null ? items.size() : "null"));
                // order.setItems(items); // 不再依賴 setItems，直接傳參數
                UserBean user = order.getUserBean();
                if (user != null && user.getEmail() != null) {
                    emailService.sendOrderNotification(user.getEmail(), order, items);
                } else {
                    System.out.println("DEBUG: User email not found for order notification.");
                }
            } catch (Exception e) {
                System.err.println("Failed to send order notification email: " + e.getMessage());
            }

            // 處理信用卡付款流程
            if ("信用卡".equals(order.getPaymentMethod())) {
                System.out.println("DEBUG: Entering ECPay block");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String tradeDate = sdf.format(order.getCreatedAt());

                // 產生 MerchantTradeNo (長度需在 20 字元內)
                // 格式: Book + OrderID + T + TimeStamp(8碼) ex: Book105T12345678
                // 這樣確保長度不超過 20 且能解析 OrderID
                String merchantTradeNo = "Book" + order.getOrderId() + "T" + (System.currentTimeMillis() % 100000000);

                // 設定回傳網址 (本地測試需透過 ngrok 改為公開網址)
                String returnURL = "https://unpreferable-unmeteorologic-ruthann.ngrok-free.dev/orders/ecpay/return";
                String clientBackURL = "http://localhost:5173/dev/user/orders/success";

                Map<String, String> ecpayParams = ecPayService.genAioCheckOutALL(
                        merchantTradeNo,
                        tradeDate,
                        order.getFinalAmount().setScale(0, RoundingMode.HALF_UP).toString(),
                        "Bookstore Order #" + order.getOrderId(),
                        itemNameBuilder.length() > 0 ? itemNameBuilder.toString() : "網路書店書籍一批",
                        returnURL,
                        clientBackURL);

                response.put("ecpayParams", ecpayParams);
                response.put("paymentUrl", ECPayService.ECPAY_PAYMENT_URL);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "結帳失敗: " + e.getMessage());
        }

        return response;
    }

    // 接收綠界付款結果回傳 (Server Side Callback)
    @PostMapping("/ecpay/return")
    @ResponseBody
    public String ecpayReturn(@RequestParam Map<String, String> params) {
        System.out.println("DEBUG: ECPay Return Params: " + params);

        if (!ecPayService.verifyCheckMacValue(params)) {
            System.out.println("DEBUG: CheckMacValue Verification Failed");
            return "0|CheckMacValue Error";
        }

        if ("1".equals(params.get("RtnCode"))) {
            String merchantTradeNo = params.get("MerchantTradeNo");
            System.out.println("DEBUG: MerchantTradeNo: " + merchantTradeNo);
            try {
                // 解析 OrderId
                // 格式: B + OrderID + T + TimeStamp
                int tIndex = merchantTradeNo.indexOf('T', 4); // 從第4個字元後開始找 T (避開開頭的Book)
                if (tIndex == -1) {
                    System.out.println("DEBUG: Cannot find 'T' separator");
                    return "0|Order Parsing Error";
                }
                Integer orderId = Integer.parseInt(merchantTradeNo.substring(4, tIndex)); // 從 index 4 開始 (避開 'Book')
                System.out.println("DEBUG: Parsed OrderId: " + orderId);

                orderService.updatePaymentStatus(orderId, "已付款");
                return "1|OK";
            } catch (Exception e) {
                e.printStackTrace();
                return "0|Order Parsing Error: " + e.getMessage();
            }
        }
        return "1|OK";
    }

    // 接收綠界地圖回傳(CVS Map)，指定 produces 確保中文不會變成亂碼
    // ECPay Map回傳接收
    @CrossOrigin(origins = { "https://logistics-stage.ecpay.com.tw",
            "https://payment-stage.ecpay.com.tw" }, allowCredentials = "true")
    @PostMapping(value = "/ecpay/mapReturn")
    @ResponseBody
    public String ecpayMapReturn(@RequestParam Map<String, String> params) {

        String storeId = params.get("CVSStoreID");
        String storeName = params.get("CVSStoreName");
        String address = params.get("CVSAddress");

        // 2. 回傳 JS 代碼通知 Vue 視窗
        return "<html><body><script>" +
                "window.opener.postMessage({ type: 'STORE_SELECTED', storeId: '" + storeId +
                "', storeName: '" + storeName + "', address: '" + address + "' }, '*');" +
                "window.close();" +
                "</script></body></html>";
    }

    // 產生並導向綠界物流地圖
    @PostMapping(value = "/ecpay/map", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String ecpayMap(@RequestParam(required = false, defaultValue = "UNIMART") String logisticsSubType) {

        // 確保使用正確的 ServerReplyURL (必須是綠界伺服器能連到的網址)
        String serverReplyURL = "https://unpreferable-unmeteorologic-ruthann.ngrok-free.dev/orders/ecpay/mapReturn";

        // 使用 Service 生成參數
        Map<String, String> params = ecPayService.genCvsMapParams(serverReplyURL);

        // 如果前端有傳送特定超商類型 (FAMI/UNIMART)，則覆蓋
        if (logisticsSubType != null) {
            params.put("LogisticsSubType", logisticsSubType);
        }

        // 建立自動送出的 Hidden Form HTML
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<form id='mapForm' action='").append(ECPayService.ECPAY_MAP_URL).append("' method='POST'>");
        params.forEach((k, v) -> {
            html.append("<input type='hidden' name='").append(k).append("' value='").append(v).append("' />");
        });
        html.append("</form>");
        html.append("<script>document.getElementById('mapForm').submit();</script>");
        html.append("</body></html>");

        return html.toString();
    }
}