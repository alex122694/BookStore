package bookstore.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class ECPayService {

    // ECPay Staging (Test) Credentials
    public static final String MERCHANT_ID = "3002607";
    public static final String HASH_KEY = "pwFHCqoQZGmho4w6";
    public static final String HASH_IV = "EkRm7iFT261dpevs";

    // ECPay Test Environment URLs
    public static final String ECPAY_PAYMENT_URL = "https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5";
    public static final String ECPAY_MAP_URL = "https://logistics-stage.ecpay.com.tw/Express/map";

    // 生成付款請求 (AioCheckOut) 所需的完整參數（含 CheckMacValue）
    public Map<String, String> genAioCheckOutALL(String merchantTradeNo, String tradeDate, String totalAmount,
            String tradeDesc, String itemName, String returnURL, String clientBackURL) {

        Map<String, String> params = new TreeMap<>();

        params.put("MerchantID", MERCHANT_ID);
        params.put("MerchantTradeNo", merchantTradeNo);
        params.put("MerchantTradeDate", tradeDate); // 格式必須為 yyyy/MM/dd HH:mm:ss
        params.put("PaymentType", "aio");
        params.put("TotalAmount", totalAmount);
        params.put("TradeDesc", tradeDesc);
        params.put("ItemName", itemName);
        params.put("ReturnURL", returnURL);
        params.put("ClientBackURL", clientBackURL);
        params.put("ChoosePayment", "Credit"); // 指定僅使用信用卡
        params.put("EncryptType", "1"); // 使用 SHA256 加密

        // 計算檢查碼
        String checkMacValue = genCheckMacValue(params);
        params.put("CheckMacValue", checkMacValue);

        return params;
    }

    // 生成物流地圖 (CVS Map) 所需參數
    // 地圖 API 主要傳送基本資訊，通常不需 CheckMacValue，但 MerchantID 必須正確
    public Map<String, String> genCvsMapParams(String serverReplyURL) {
        Map<String, String> params = new TreeMap<>();
        params.put("MerchantID", MERCHANT_ID);
        params.put("LogisticsType", "CVS");
        params.put("LogisticsSubType", "UNIMART"); // 預設 7-11
        params.put("IsCollection", "N"); // 是否代收貨款
        params.put("ServerReplyURL", serverReplyURL); // 使用者選完門市後，資料回傳的後端網址

        return params;
    }

    // 計算綠界檢查碼 (CheckMacValue)
    public String genCheckMacValue(Map<String, String> params) {
        // 1. 參數名稱依字典順序排序 (TreeMap 已處理)

        // 2. 組合字串
        String queryString = params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        // 3. 前後加上 HashKey 與 HashIV
        String rawString = "HashKey=" + HASH_KEY + "&" + queryString + "&HashIV=" + HASH_IV;

        System.out.println("DEBUG: Raw string before MD5/SHA256: " + rawString);

        // 4. URL Encode 並符合綠界要求的符號替換 (重要：必須小寫後計算 SHA256)
        String urlEncoded = urlEncodeDotNet(rawString).toLowerCase();

        // 5. 計算 SHA256 並轉大寫
        String result = getSHA256(urlEncoded).toUpperCase();

        System.out.println("DEBUG: Generated CheckMacValue: " + result);

        return result;
    }

    // 驗證回傳的檢查碼是否正確
    public boolean verifyCheckMacValue(Map<String, String> params) {
        if (!params.containsKey("CheckMacValue")) {
            return false;
        }

        String receivedChecksum = params.get("CheckMacValue");
        Map<String, String> calculationParams = new TreeMap<>(params);
        calculationParams.remove("CheckMacValue");

        String calculatedChecksum = genCheckMacValue(calculationParams);
        return calculatedChecksum.equals(receivedChecksum);
    }

    // --- 工具方法 ---

    // 符合 .NET HttpUtility.UrlEncode 規範的編碼
    private String urlEncodeDotNet(String s) {
        try {
            String encoded = URLEncoder.encode(s, "UTF-8");
            // 綠界規範：將編碼後的符號轉回特定字元，並將 + 轉為 %20
            return encoded
                    .replace("%21", "!")
                    .replace("%28", "(")
                    .replace("%29", ")")
                    .replace("%2a", "*")
                    .replace("%2A", "*")
                    .replace("%7e", "~") 
                    .replace("%7E", "~")
                    .replace("%2d", "-") 
                    .replace("%2D", "-")
                    .replace("%5f", "_") 
                    .replace("%5F", "_")
                    .replace("%2e", ".") // 確保點不被編碼
                    .replace("%2E", ".");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private String getSHA256(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(s.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}