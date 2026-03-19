package com.bookstore.chat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.bookstore.order.OrderItem;
import com.bookstore.order.Orders;
import com.bookstore.user.UserBean;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    
    public void sendVerifyCode(String to, String code, String subject) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            InternetAddress fromAddress = new InternetAddress("onlinebookstoreforjava@gmail.com",
                    "森林書屋 Forest Bookstore", "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            String actionText = subject.contains("重設") ? "重設您的帳戶密碼" : "註冊森林書屋會員";

            String content = "<h3>親愛的讀者您好：</h3>" +
                    "<p>您正在進行 " + actionText + "，您的驗證碼為：</p>" +
                    "<h1 style='color: #2E5C43; letter-spacing: 5px;'>" + code + "</h1>" +
                    "<p>請於 5 分鐘內完成驗證。為了您的帳戶安全，請勿將此代碼告知他人。</p>" +
                    "<p>若非本人操作，請忽略此信。</p>" +
                    "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>" +
                    "<small style='color: #888;'>此為系統自動發送，請勿直接回覆。</small>";

            helper.setText(content, true);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.err.println("郵件發送失敗原因: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("發送郵件失敗: " + e.getMessage());
        }
    } 
    
    public void sendResetSuccessNotification(String to) {
    	try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            InternetAddress fromAddress = new InternetAddress("onlinebookstoreforjava@gmail.com", "森林書屋 Forest Bookstore", "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject("森林書屋 - 帳戶密碼變更通知");

            String content = "<h3>親愛的讀者您好：</h3>" +
                             "<p>您的森林書屋帳戶密碼已於剛才 **成功完成重設**。</p>" +
                             "<p>如果您本人並未執行此操作，請立即聯繫我們的客服人員。</p>" +
                             "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>" +
                             "<small style='color: #888;'>此為系統自動發送，請勿直接回覆。</small>";

            helper.setText(content, true);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.err.println("安全性通知郵件發送失敗: " + e.getMessage());
        }
    }

    public void sendOrderNotification(String to, Orders order, List<OrderItem> items) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            InternetAddress fromAddress = new InternetAddress("onlinebookstoreforjava@gmail.com",
                    "森林書屋 Forest Bookstore",
                    "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject("森林書屋 - 訂單確認通知 (訂單編號: " + order.getOrderId() + ")");

            StringBuilder itemsHtml = new StringBuilder();
            itemsHtml.append("<table style='width: 100%; border-collapse: collapse; margin-top: 10px;'>");
            itemsHtml.append(
                    "<tr style='background-color: #f2f2f2;'><th style='padding: 8px; text-align: left;'>商品名稱</th><th style='padding: 8px; text-align: right;'>數量</th><th style='padding: 8px; text-align: right;'>小計</th></tr>");

            // 使用傳入的 items 清單
            if (items != null) {
                for (com.bookstore.order.OrderItem item : items) {
                    String bookName = "未知書籍";
                    if (item.getBooksBean() != null) {
                        bookName = item.getBooksBean().getBookName();
                    }
                    itemsHtml.append("<tr>");
                    itemsHtml.append("<td style='padding: 8px; border-bottom: 1px solid #ddd;'>").append(bookName)
                            .append("</td>");
                    itemsHtml.append("<td style='padding: 8px; text-align: right; border-bottom: 1px solid #ddd;'>")
                            .append(item.getQuantity()).append("</td>");
                    itemsHtml.append("<td style='padding: 8px; text-align: right; border-bottom: 1px solid #ddd;'>$")
                            .append(item.getPrice()).append("</td>");
                    itemsHtml.append("</tr>");
                }
            }
            itemsHtml.append("</table>");

            String content = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 5px;'>"
                    +
                    "<h2 style='color: #2E5C43; text-align: center;'>感謝您的訂購！</h2>" +
                    "<p>親愛的讀者您好，我們已收到您的訂單。</p>" +
                    "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>" +
                    "<h4 style='color: #333;'></h4>" +
                    "<p><strong>訂單編號：</strong> " + order.getOrderId() + "</p>" +
                    "<p><strong>訂單日期：</strong> " + order.getCreatedAt() + "</p>" +
                    "<p><strong>付款方式：</strong> " + order.getPaymentMethod() + "</p>" +
                    "<p><strong>配送方式：</strong> " + order.getDeliveryMethod() + "</p>" +
                    "<p><strong>收件地址：</strong> " + order.getAddress() + "</p>" +
                    "<h4 style='color: #333;'>訂單明細</h4>" +
                    itemsHtml.toString() +
                    "<p style='text-align: right; margin-top: 10px;'><strong>運費：</strong> $" + order.getShippingFee()
                    + "</p>" +
                    "<h3 style='text-align: right; color: #d9534f;'>總金額：$" + order.getFinalAmount() + "</h3>" +
                    "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>" +
                    "<p style='font-size: 14px; color: #666;'>您可以登入會員中心查看訂單詳細狀態。</p>" +
                    "<div style='text-align: center; margin-top: 30px;'>" +
                    "<a href='http://localhost:5173' style='background-color: #2E5C43; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>前往森林書屋</a>"
                    +
                    "</div>" +
                    "</div>";

            helper.setText(content, true);

            mailSender.send(mimeMessage);
            System.out.println("訂單通知信已發送至: " + to);
        } catch (Exception e) {
            System.err.println("訂單通知信發送失敗: " + e.getMessage());
            e.printStackTrace();
        }
    }

	
	public void sendToAllRegister(List<String> to, String clubName) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
			InternetAddress fromAddress = new InternetAddress("onlinebookstoreforjava@gmail.com",
					"森林書屋 Forest Bookstore", "UTF-8");

			String content = "";
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// 活動取消通知
	@Async
	public void sendClubCancelToRegister(String to, String clubName, String hostName, LocalDateTime localDateTime,
			String memberName, String location, String hostEmail,String phone) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
			InternetAddress fromAddress = new InternetAddress("onlinebookstoreforjava@gmail.com",
					"森林書屋 Forest Bookstore", "UTF-8");
			String formattedDate = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
			String content = "<h3>親愛的 " + memberName + " 您好：</h3>" + "<p>非常遺憾地通知您，您原定參加的讀書會：<strong>「" + clubName
					+ "」</strong> <span style='color: #F56C6C;'>已取消</span>。</p>"

					// 區塊背景改為淡紅色 (#fff0f0)，左側邊框改為紅色 (#F56C6C) 以示區別
					+ "<div style='background-color: #fff0f0; padding: 15px; border-left: 5px solid #F56C6C; margin: 20px 0;'>"
					+ "<p style='margin: 0; font-weight: bold; color: #F56C6C;'>【取消場次資訊】</p>"
					+ "<ul style='margin-top: 10px; list-style: none; padding-left: 0;'>"
					+ "<li>📅 <strong>原定日期：</strong>" + formattedDate + "</li>" 
					+ "<li>📍 <strong>原定地點：</strong>"+ location + "</li>" 
					+ "</ul>" 
					+ "</div>"
					+ "<p>造成您的不便，我們深感抱歉。若您對此次取消有任何疑問，可透過以下資訊聯繫主辦人：</p>"
					+ "<ul style='list-style: none; padding-left: 0; color: #666;'>"
					+ "<li>👤 <strong>主辦人：</strong>"+ hostName + "</li>" 
					+ "<li>✉️ <strong>Email：</strong>" + hostEmail + "</li>" 
					+ "<li>📞 <strong>電話：</strong>" + phone + "</li>" 
					+ "</ul>"
					+ "<br>" 
					+ "<p>期待您未來能繼續支持其他的讀書會活動！</p>" 
					+ "<br>"
					+ "<hr style='border: 0; border-top: 1px solid #eee;'>"
					+ "<small style='color: #888;'>此為系統自動發送，請勿直接回覆。</small>";
			
			helper.setFrom(fromAddress);
			helper.setTo(to);
			helper.setSubject("森林書屋 - 讀書會取消通知");
			helper.setText(content,true);
			mailSender.send(mimeMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 報名成功通知
	@Async
	public void sendRegistrationToMember(String to, String clubName, String location, UserBean userBean,
			LocalDateTime localDateTime, String memberName) {
		try {
			String formattedDate = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
			InternetAddress fromAddress = new InternetAddress("onlinebookstoreforjava@gmail.com",
					"森林書屋 Forest Bookstore", "UTF-8");
			String content = "<h3>親愛的 " + memberName + " 您好：</h3>" + "<p>恭喜您！您已成功報名讀書會：<strong>「" + clubName
					+ "」</strong>。</p>"
					+ "<div style='background-color: #f0f7ff; padding: 15px; border-left: 5px solid #409EFF; margin: 20px 0;'>"
					+ "<p style='margin: 0; font-weight: bold; color: #409EFF;'>【報名資訊確認】</p>"
					+ "<ul style='margin-top: 10px; list-style: none; padding-left: 0;'>"
					+ "<li>📅 <strong>活動日期：</strong>" + formattedDate + "</li>" + "<li>📍 <strong>活動地點：</strong>"
					+ location + "</li>" + "<li>👤 <strong>主辦人：</strong>" + userBean.getUserName() + "</li>"
					+ "<li>✉️ <strong>主辦人信箱:</strong>" + userBean.getEmail() + "</li>"
					+ "<li>📞 <strong>主辦人電話:</strong>" + userBean.getPhoneNum() + "</li>" + "</ul>" + "</div>"
					+ "<p>您可以前往「我參加的讀書會」查看更詳細的讀書會內容或主辦人公告。期待您的參與！</p>" + "<br>" + "<br>"
					+ "<hr style='border: 0; border-top: 1px solid #eee;'>"
					+ "<small style='color: #888;'>此為系統自動發送，請勿直接回覆。若您不克參加，請記得提前取消報名以利名額釋出。</small>";

			helper.setFrom(fromAddress);
			helper.setTo(to);
			helper.setSubject("森林書屋 - 讀書報名成功通知");
			helper.setText(content, true);
			mailSender.send(mimeMessage);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// 核准通知
	@Async
	public void sendAccetToHost(String to, String clubName, String memberName) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
			InternetAddress fromAddress = new InternetAddress("onlinebookstoreforjava@gmail.com",
					"森林書屋 Forest Bookstore", "UTF-8");
			String content = "<h3>親愛的會員 " + memberName + " 您好：</h3>" + "<p>恭喜您！您發起的讀書會：<strong>「" + clubName
					+ "」</strong> 已通過審核，現在可以正式開始運作了！</p>"
					+ "<div style='background-color: #f0f9eb; padding: 15px; border-left: 5px solid #67C23A; margin: 20px 0;'>"
					+ "<p style='margin: 0; font-weight: bold; color: #67C23A;'>【審核狀態：核准通過】</p>"
					+ "<p style='margin-top: 10px;'>您的熱情與分享將為社群帶來更多價值，祝您的讀書會圓滿成功！</p>" + "</div>" + "<h4>接下來您可以：</h4>"
					+ "<ul>" + "<li>前往平台查看您的讀書會頁面。</li>" + "<li>開始招募成員並發布第一次聚會通知。</li>" + "<li>分享讀書會連結，邀請更多同好加入。</li>"
					+ "</ul>" + "<br>" + "<hr style='border: 0; border-top: 1px solid #eee;'>"
					+ "<small style='color: #888;'>此為系統自動發送，請勿直接回覆。如有任何操作問題，歡迎隨時聯繫客服中心。</small>";
			helper.setFrom(fromAddress);
			helper.setTo(to);
			helper.setSubject("森林書屋 - 讀書會申請核准通知");
			helper.setText(content, true);
			mailSender.send(mimeMessage);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// 駁回通知
	@Async
	public void sendRejectToHost(String to, String clubName, String memberName, String RejectReason) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
			InternetAddress fromAddress = new InternetAddress("onlinebookstoreforjava@gmail.com",
					"森林書屋 Forest Bookstore", "UTF-8");

			String content = "<h3>親愛的會員 " + memberName + " 您好：</h3>" + "<p>感謝您對社群的貢獻與對讀書會活動的熱情參與。</p>"
					+ "<p>關於您發起的讀書會：<strong>「" + clubName + "」</strong>，經過相關單位審核後，目前遺憾地通知您<strong>未能通過審查</strong>。</p>"
					+ "<div style='background-color: #f9f9f9; padding: 15px; border-left: 5px solid #EA0000; margin: 20px 0;'>"
					+ "<p style='margin: 0; font-weight: bold;'>駁回原因如下：</p>"
					+ "<p style='color: #EA0000; margin-top: 10px;'>" + RejectReason + "</p>" + "</div>"
					+ "<p>若您願意根據上述原因調整計畫內容，歡迎修改後重新提交申請。期待能看到更完善的讀書會提案！</p>" + "<br>"
					+ "<hr style='border: 0; border-top: 1px solid #eee;'>"
					+ "<small style='color: #888;'>此為系統自動發送，請勿直接回覆。如有任何疑問，請聯繫平台客服中心。</small>";
			helper.setFrom(fromAddress);
			helper.setTo(to);
			helper.setSubject("森林書屋 - 讀書會申請駁回通知");
			helper.setText(content, true);
			mailSender.send(mimeMessage);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 申請成功通知
	@Async
	public void sendPendingMailToHost(String to, String clubName, String memberName) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
			InternetAddress fromAddress = new InternetAddress("onlinebookstoreforjava@gmail.com",
					"森林書屋 Forest Bookstore", "UTF-8");
			String content = "<h3>親愛的會員 " + memberName + " 您好：</h3>" + "<p>我們已收到您發起的讀書會：<strong>「" + clubName
					+ "」</strong> 的申請。</p>"
					+ "<div style='background-color: #f4f4f5; padding: 15px; border-left: 5px solid #909399; margin: 20px 0;'>"
					+ "<p style='margin: 0; font-weight: bold;'>當前狀態：<span style='color: #E6A23C;'>待審核</span></p>"
					+ "<p style='margin-top: 10px; font-size: 0.9em; color: #606266;'>"
					+ "管理員將於 3-5 個工作天內完成審核，審核結果將會透過此郵件通知您。" + "</p>" + "</div>"
					+ "<p>在等待審核期間，您可以先準備讀書會所需的教材或初步的討論大綱。</p>" + "<br>"
					+ "<p style='font-size: 0.85em; color: #999;'>若非本人操作，請忽略此信，您的帳號安全不會受到影響。</p>"
					+ "<hr style='border: 0; border-top: 1px solid #eee;'>"
					+ "<small style='color: #888;'>此為系統自動發送，請勿直接回覆。</small>";

			helper.setFrom(fromAddress);
			helper.setTo(to);
			helper.setSubject("森林書屋 - 讀書會申請通知");
			helper.setText(content, true);

			mailSender.send(mimeMessage);
		} catch (Exception e) {
			e.printStackTrace();

		}

	}

}