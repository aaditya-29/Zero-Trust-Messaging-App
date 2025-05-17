# Zero Trust Messaging App (ZTMA)

A secure, real-time chat and file transfer application built with Spring Boot, WebSockets, and MongoDB, featuring end-to-end encryption, forward secrecy, device/IP anomaly detection, and multi-factor authentication.

## Features

- **User Registration & Email Verification**  
  Users register with email verification to activate their account.

- **Multi-Factor Authentication (MFA)**  
  Login requires both password and a one-time password (OTP) sent via email.

- **End-to-End Encrypted Messaging**  
  Messages are encrypted with AES, and the AES key is encrypted with the recipient's RSA public key.

- **Secure File Transfer**  
  Files are encrypted, chunked, and transferred securely with per-file AES keys.

- **Sensitive Mode**  
  Messages/files marked as sensitive expire and are deleted after a short period.

- **Device & IP Anomaly Detection**  
  New device or IP logins trigger email alerts.

- **Admin Dashboard**  
  Admins can view trusted devices and user activity logs.

- **Spam & Large File Alerts**  
  Users are notified of unusual activity or large file transfers.

## Technology Stack

- Java 17, Spring Boot 3
- Spring Security, Spring WebSocket, Spring Data MongoDB
- Thymeleaf for frontend templates
- MongoDB for data storage
- [CryptoJS](https://cryptojs.gitbook.io/docs/), [JSEncrypt](https://github.com/travist/jsencrypt), [TweetNaCl.js](https://github.com/dchest/tweetnacl-js) for client-side crypto
- Tailwind CSS for UI

## Getting Started

### Prerequisites

- Java 17+
- Maven
- MongoDB (local or Atlas)

### Setup

1. **Clone the repository**

   ```sh
   git clone https://github.com/aaditya-29/Zero-Trust-Messaging-App.git
   cd ztma
   ```

2. **Configure MongoDB**

   Edit `src/main/resources/application.properties` if needed:

   ```
   spring.data.mongodb.uri=mongodb://localhost:27017/ztma
   ```

3. **Configure Email (for OTP/alerts)**

   Update the SMTP settings in `application.properties` with your email credentials.

4. **Build and Run**

   ```sh
   ./mvnw spring-boot:run
   ```

   The app will be available at [http://localhost:8080](http://localhost:8080).

## Usage

- **Register**: Go to `/register`, fill in your details, and verify your email.
- **Login**: Enter your credentials, then enter the OTP sent to your email.
- **Chat**: Send encrypted messages and files to other users.
- **Sensitive Mode**: Check "Sensitive Mode" to send self-destructing messages/files.
- **Admin Dashboard**: Access `/admin/dashboard` (requires admin privileges).

## Security Highlights

- **End-to-End Encryption**: All messages and files are encrypted client-side.
- **Device/IP Monitoring**: Alerts for new device or IP logins.
- **Multi-Factor Authentication**: Email OTP required for login.
- **Spam & Anomaly Detection**: Alerts for suspicious activity.

## Project Structure

- `src/main/java/com/ztma/` - Main Java source code
- `src/main/resources/templates/` - Thymeleaf HTML templates
- `src/main/resources/application.properties` - Configuration

## License

This project is for educational purposes.

---
