```
simplewallet/
├── pom.xml
├── README.md
├── .env.example
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── simplewallet/
    │   │           ├── SimpleWalletApplication.java
    │   │           │
    │   │           ├── config/
    │   │           │   ├── SecurityConfig.java
    │   │           │   └── CorsConfig.java
    │   │           │
    │   │           ├── common/
    │   │           │   ├── exception/
    │   │           │   │   ├── GlobalExceptionHandler.java
    │   │           │   │   └── ResourceNotFoundException.java
    │   │           │   ├── response/
    │   │           │   │   └── ApiResponse.java
    │   │           │   └── enums/
    │   │           │
    │   │           ├── security/
    │   │           │   ├── JwtService.java
    │   │           │   └── JwtAuthenticationFilter.java
    │   │           │
    │   │           ├── auth/
    │   │           │   ├── AuthController.java
    │   │           │   ├── AuthService.java
    │   │           │   ├── AuthRepository.java
    │   │           │   └── dto/
    │   │           │       ├── LoginRequest.java
    │   │           │       ├── RegisterRequest.java
    │   │           │       └── AuthResponse.java
    │   │           │
    │   │           ├── user/
    │   │           │   ├── User.java
    │   │           │   ├── UserController.java
    │   │           │   ├── UserService.java
    │   │           │   ├── UserRepository.java
    │   │           │   └── dto/
    │   │           │
    │   │           ├── wallet/
    │   │           │   ├── Wallet.java
    │   │           │   ├── WalletController.java
    │   │           │   ├── WalletService.java
    │   │           │   ├── WalletRepository.java
    │   │           │   └── dto/
    │   │           │
    │   │           ├── transaction/
    │   │           │   ├── Transaction.java
    │   │           │   ├── TransactionController.java
    │   │           │   ├── TransactionService.java
    │   │           │   ├── TransactionRepository.java
    │   │           │   └── dto/
    │   │           │
    │   │           ├── deposit/
    │   │           │   ├── Deposit.java
    │   │           │   ├── DepositController.java
    │   │           │   ├── DepositService.java
    │   │           │   ├── DepositRepository.java
    │   │           │   └── dto/
    │   │           │
    │   │           ├── category/
    │   │           │   ├── Category.java
    │   │           │   ├── CategoryController.java
    │   │           │   ├── CategoryService.java
    │   │           │   └── CategoryRepository.java
    │   │           │
    │   │           ├── product/
    │   │           │   ├── Product.java
    │   │           │   ├── ProductController.java
    │   │           │   ├── ProductService.java
    │   │           │   ├── ProductRepository.java
    │   │           │   └── dto/
    │   │           │
    │   │           ├── order/
    │   │           │   ├── Order.java
    │   │           │   ├── OrderItem.java
    │   │           │   ├── OrderController.java
    │   │           │   ├── OrderService.java
    │   │           │   ├── OrderRepository.java
    │   │           │   └── dto/
    │   │           │
    │   │           ├── payment/
    │   │           │   ├── PaymentMethod.java
    │   │           │   ├── PaymentController.java
    │   │           │   ├── PaymentService.java
    │   │           │   └── PaymentRepository.java
    │   │           │
    │   │           └── admin/
    │   │               ├── AdminController.java
    │   │               └── AdminService.java
    │   │
    │   └── resources/
    │       ├── application.yml
    │       └── db/
    │           └── migration/
    │
    └── test/
        └── java/
            └── com/
                └── simplewallet/
                    ├── auth/
                    ├── wallet/
                    └── order/
```