package com.movieflix.controllers;

import com.movieflix.auth.entities.ForgotPassword;
import com.movieflix.auth.entities.User;
import com.movieflix.auth.repositories.ForgotPasswordRepository;
import com.movieflix.auth.repositories.UserRepository;
import com.movieflix.auth.utils.ChangePassword;
import com.movieflix.dto.MailBody;
import com.movieflix.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

// Controller dùng để xử lý chức năng quên mật khẩu
@RestController
@RequestMapping("/forgotPassword")
@CrossOrigin(origins = "*") // Cho phép gọi API từ bất kỳ domain nào (Cross-Origin)
public class ForgotPasswordController {

    // Inject các dependency cần thiết
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ForgotPasswordRepository forgotPasswordRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor khởi tạo controller với các dependency được truyền vào
    public ForgotPasswordController(UserRepository userRepository, EmailService emailService, ForgotPasswordRepository forgotPasswordRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.forgotPasswordRepository = forgotPasswordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // API gửi OTP xác thực tới email người dùng
    @PostMapping("/verifyMail/{email}")
    public ResponseEntity<String> verifyEmail(@PathVariable String email) {
        // Tìm user theo email, nếu không có thì báo lỗi
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide an valid email!" + email));

        // Tạo mã OTP ngẫu nhiên
        int otp = otpGenerator();

        // Tạo nội dung email gửi OTP
        MailBody mailBody = MailBody.builder()
                .to(email)
                .text("This is the OTP for your Forgot Password request : " + otp)
                .subject("OTP for Forgot Password request")
                .build();

        // Tạo đối tượng ForgotPassword để lưu vào DB
        ForgotPassword fp = ForgotPassword.builder()
                .otp(otp)
                .expirationTime(new Date(System.currentTimeMillis() + 20 * 100000)) // Thời hạn OTP (tạm tính ~33 phút)
                .user(user)
                .build();

        // Gửi email và lưu OTP vào database
        emailService.sendSimpleMessage(mailBody);
        forgotPasswordRepository.save(fp);

        // Trả về phản hồi OK
        return ResponseEntity.ok("Email sent for verification!");
    }

    // API xác minh OTP người dùng nhập vào
    @PostMapping("/verifyOtp/{otp}/{email}")
    public ResponseEntity<String> verifyOtp(@PathVariable Integer otp, @PathVariable String email) {
        // Tìm user theo email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide an valid email!"));

        // Kiểm tra OTP khớp với user
        ForgotPassword fp = forgotPasswordRepository.findByOtpAndUser(otp, user)
                .orElseThrow(() -> new RuntimeException("Invalid OTP for email: " + email));

        // Kiểm tra thời gian hết hạn OTP
        if (fp.getExpirationTime().before(Date.from(Instant.now()))) {
            // Nếu hết hạn thì xóa OTP và báo lỗi
            forgotPasswordRepository.deleteById(fp.getFpid());
            return new ResponseEntity<>("OTP has expired!", HttpStatus.EXPECTATION_FAILED);
        }

        // OTP hợp lệ
        return ResponseEntity.ok("OTP verified!");
    }

    // API thay đổi mật khẩu khi OTP đã được xác minh
    @PostMapping("/changePassword/{email}")
    public ResponseEntity<String> changePasswordHandler(@RequestBody ChangePassword changePassword,
                                                        @PathVariable String email) {
        // Kiểm tra xem hai mật khẩu nhập lại có giống nhau không
        if (!Objects.equals(changePassword.password(), changePassword.repeatPassword())) {
            return new ResponseEntity<>("Please enter the password again!", HttpStatus.EXPECTATION_FAILED);
        }

        // Mã hóa mật khẩu mới và cập nhật vào DB
        String encodedPassword = passwordEncoder.encode(changePassword.password());
        userRepository.updatePassword(email, encodedPassword);

        return ResponseEntity.ok("Password has been changed!");
    }

    // Hàm tạo mã OTP ngẫu nhiên có 6 chữ số
    private Integer otpGenerator() {
        Random random = new Random();
        return random.nextInt(100_000, 999_999); // Sinh số từ 100000 đến 999999
    }
}
