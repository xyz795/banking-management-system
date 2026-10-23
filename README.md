Banking Management System

A web-based banking application built using Java, Spring Boot, Spring Security, Spring Data JPA, MySQL, and Thymeleaf.

The application provides customer banking operations and an admin dashboard with role-based access control.

Features

Customer Features

- User registration and login
- Customer dashboard
- Bank account management
- Check account balance
- Deposit money
- Withdraw money
- Transfer money between accounts
- View transaction history

Admin Features

- Admin dashboard
- View customer information
- View total customer count
- View total balance summary
- Role-based access control

Security and Validation

- Spring Security authentication and authorization
- Password hashing using BCrypt
- Customer account ownership checks
- Validation for positive transaction amounts
- Prevention of transfers between identical accounts
- Balance validation before withdrawals and transfers
- Database transactions for money operations

Technology Stack

- Language: Java
- Framework: Spring Boot
- Security: Spring Security
- Database: MySQL
- ORM: Spring Data JPA and Hibernate
- Frontend: Thymeleaf, HTML, CSS
- Build Tool: Maven
- Testing: JUnit and Mockito
- Version Control: Git and GitHub

Prerequisites

Install the following before running the project:

- Java Development Kit (JDK)
- MySQL Server
- Git (optional)

Run Locally

1. Clone or download this repository.
2. Create a MySQL database named "banking_db".
3. Configure your database connection in "src/main/resources/application.properties".
4. Make sure your database username and password are configured correctly.
5. Open a terminal in the project root directory.
6. Run the application:

Windows

.\mvnw.cmd spring-boot:run

7. Open your browser and visit:

http://localhost:8080

Run Tests

Run the automated tests using:

.\mvnw.cmd test

The project includes application context tests and service-level tests for transaction validation and security-related scenarios.

Project Structure

src/
├── main/
│   ├── java/com/ashish/banking/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── entity/
│   │   ├── repository/
│   │   └── service/
│   └── resources/
│       ├── templates/
│       ├── static/
│       └── application.properties
└── test/
    └── java/com/ashish/banking/

Developer

Ashish Yadav

B.Tech in Computer Science and Engineering | 2026

Future Improvements

- Add automated integration tests for complete money transfer workflows.
- Improve monitoring and audit logging.
- Add additional account and transaction reporting features.
- Enhance deployment and production configuration.