package com.catcher.core.service;

import com.catcher.common.BaseResponseStatus;
import com.catcher.common.exception.BaseException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {


    private final JavaMailSender emailSender;

    public void sendEmail(String toEmail,
                          String title,
                          String key) {
        try {
            MimeMessage message = createMessage(toEmail, title, key);
            emailSender.send(message);
        } catch (Exception e) {
            log.error("이메일 전송 에러 발생", e);
            log.debug("MailService.sendEmail exception occur toEmail: {}, " +
                    "title: {}, key: {}", toEmail, title, key);
            throw new BaseException(BaseResponseStatus.EMAIL_SEND_ERROR);
        }
    }

    /* TODO: 기획 확인 후 이메일 메시지 포맷, 발송 이메일 변경 */
    private MimeMessage createMessage(String toEmail, String title, String key) throws Exception {
        MimeMessage  message = emailSender.createMimeMessage();

        message.addRecipients(MimeMessage.RecipientType.TO, toEmail);
        message.setSubject(title);

        String msgg="";
        msgg+= "<div style='margin:20px;'>";
        msgg+= "<h3>회원가입 인증 코드입니다.</h3>";
        msgg+= "<div style='font-size:130%'>";
        msgg+= "CODE : <strong>";
        msgg+= key+"</strong><div><br/> ";
        msgg+= "</div>";
        message.setText(msgg, "utf-8", "html");//내용
        message.setFrom(new InternetAddress("djg4053@gmail.com","권덕주"));

        return message;
    }
}
