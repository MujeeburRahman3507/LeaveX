package com.boit.boit_leave.service;

import com.boit.boit_leave.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BirthdayEmailService {

    private final JavaMailSender mailSender;

    public void sendBirthdayEmail(User user) {

        try {

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8"
                    );

            helper.setTo(user.getEmail());

            helper.setSubject("🎉 Happy Birthday from BOIT GLOBAL");

            helper.setText(buildBirthdayTemplate(user), true);

            mailSender.send(message);

            log.info("Birthday email sent to {}",
                    user.getEmail());

        } catch (MessagingException ex) {

            log.error("Failed sending birthday email to {}",
                    user.getEmail(),
                    ex);

        }

    }

    /**
     * HTML Birthday Email Template
     */
    private String buildBirthdayTemplate(User user) {

        return """
                <html>

                <body style="font-family:Arial;background:#f5f7fb;padding:30px;">

                <div style="
                        max-width:650px;
                        margin:auto;
                        background:white;
                        border-radius:12px;
                        padding:40px;
                        box-shadow:0 5px 15px rgba(0,0,0,.1);
                        ">

                    <h1 style="color:#6D28D9;text-align:center;">
                        🎂 Happy Birthday!
                    </h1>

                    <h2 style="text-align:center;">
                        %s
                    </h2>

                    <p style="font-size:16px;line-height:1.8;">

                        Dear <b>%s</b>,

                    </p>

                    <p style="font-size:16px;line-height:1.8;">

                        The entire <b>BOIT GLOBAL</b> family wishes you
                        a very Happy Birthday!

                    </p>

                    <p style="font-size:16px;line-height:1.8;">

                        🎉 May your special day be filled with happiness,
                        success, prosperity and wonderful memories.

                    </p>

                    <p style="font-size:16px;line-height:1.8;">

                        We appreciate your dedication and wish you
                        another successful year ahead.

                    </p>

                    <br>

                    <h3 style="color:#6D28D9;">
                        🎁 Happy Birthday!
                    </h3>

                    <p>

                        Regards,

                    </p>

                    <b>BOIT GLOBAL</b>

                    <hr>

                    <div style="
                            text-align:center;
                            color:#999;
                            font-size:13px;
                            ">

                        This is an automated birthday greeting from
                        BOIT Leave Management System.

                    </div>

                </div>

                </body>

                </html>
                """.formatted(

                user.getFullName(),

                user.getFullName()

        );

    }

}