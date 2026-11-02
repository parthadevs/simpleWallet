# SimpleWallet API Documentation

> **Document status:** API contract draft  
> **Project:** SimpleWallet — digital marketplace and wallet  
> **Version:** 1.0.0  
>
> This document is a proposed API contract based on the known SimpleWallet concept (user accounts, wallet balance, deposits, transactions, products, orders, and admin operations). The actual SimpleWallet source code/routes were not available for verification when this file was created. Confirm route names, validation rules, status codes, and response envelopes against the implementation before treating this as the live API specification.

---

## Table of Contents

- [1. Overview](#1-overview)
- [2. Base URL and Conventions](#2-base-url-and-conventions)
- [3. Authentication](#3-authentication)
- [4. Standard Response Formats](#4-standard-response-formats)
- [5. Authentication Endpoints](#5-authentication-endpoints)
- [6. User Profile](#6-user-profile)
- [7. Wallet](#7-wallet)
- [8. Deposits](#8-deposits)
- [9. Transactions](#9-transactions)
- [10. Product Categories](#10-product-categories)
- [11. Products](#11-products)
- [12. Orders](#12-orders)
- [13. Payment Methods](#13-payment-methods)
- [14. Telegram Account Linking](#14-telegram-account-linking)
- [15. Admin Endpoints](#15-admin-endpoints)
- [16. Error Codes](#16-error-codes)
- [17. Security and Financial Integrity](#17-security-and-financial-integrity)
- [18. Environment Configuration](#18-environment-configuration)
- [19. Endpoint Summary](#19-endpoint-summary)

---

## 1. Overview

SimpleWallet is documented here as a digital marketplace with a wallet balance. Users can register, sign in, browse products, submit deposits for review, purchase available stock, and review wallet transactions and orders. Administrators can manage products, stock, deposits, orders, payment methods, users, and settings.

### Proposed API modules

| Module | Purpose |
|---|---|
| Auth | Register, login, logout, current user |
| Profile | Read and update user profile |
| Wallet | Balance and transaction history |
| Deposits | Create deposit requests and check status |
| Transactions | Review wallet ledger entries |
| Categories | Browse product categories |
| Products | Browse product listings |
| Orders | Purchase products and view delivery details |
| Payment Methods | Read active deposit methods |
| Telegram | Link a Telegram account |
| Admin | Manage users, products, stock, deposits, orders, and settings |

## 2. Base URL and Conventions

Set the actual host for your deployment. The following values are examples/placeholders:

```text
Local:       http://localhost:8000/api/v1
Production:  https://your-domain.com/api/v1
```

All examples below use:

```http
Accept: application/json
Content-Type: application/json
```

For authenticated endpoints:

```http
Authorization: Bearer <access_token>
```

If the Laravel application uses session/cookie authentication instead of bearer tokens, replace the Authorization-header approach with the authentication method implemented by the application.

### Path parameters

Values in braces are path parameters. Example: `/products/{productId}` becomes `/products/42`.

### Pagination

List endpoints may accept:

| Query parameter | Type | Description |
|---|---|---|
| `page` | integer | Page number; default `1` |
| `per_page` | integer | Items per page; proposed default `15`, maximum `100` |
| `search` | string | Optional search term |
| `status` | string | Optional status filter |

Proposed paginated response:

```json
{
  "data": [],
  "meta": {
    "current_page": 1,
    "per_page": 15,
    "total": 0,
    "last_page": 1
  }
}
```

## 3. Authentication

Protected routes require a valid authenticated session or access token. Administrative routes additionally require an administrator role/permission.

### Roles

| Role | Access |
|---|---|
| `user` | Own profile, wallet, deposits, orders, and public catalog |
| `admin` | Administrative management endpoints |

The role names are proposed; use the values defined by the actual application.

## 4. Standard Response Formats

The examples below use a consistent response envelope. If the application returns raw resources or Laravel's default validation format, adjust these examples to match it.

### Successful response

```json
{
  "success": true,
  "message": "Request completed successfully.",
  "data": {}
}
```

### Validation error

```json
{
  "success": false,
  "message": "The given data was invalid.",
  "errors": {
    "email": [
      "The email field must be a valid email address."
    ]
  }
}
```

### General error

```json
{
  "success": false,
  "message": "The requested resource was not found.",
  "error": {
    "code": "RESOURCE_NOT_FOUND"
  }
}
```

---

## 5. Authentication Endpoints

### 5.1 Register

Creates a user account.

**`POST /auth/register`**  
**Auth:** Public  
**Success:** `201 Created`

Request body:

```json
{
  "name": "Partha Sarker",
  "email": "partha@example.com",
  "password": "StrongPassword123!",
  "confirmPassword": "StrongPassword123!"
}
```

Response body:

```json
{
  "success": true,
  "message": "Account created successfully.",
  "data": {
    "user": {
      "id": "usr_01",
      "name": "Partha Sarker",
      "email": "partha@example.com",
      "role": "user",
      "created_at": "2026-11-01T12:00:00Z"
    },
    "access_token": "<access_token>",
    "token_type": "Bearer"
  }
}
```

Notes:
- Never return the user's password or password hash.
- Token fields depend on the authentication implementation.
- If email verification is required, document the verification flow separately.

### 5.2 Login

**`POST /auth/login`**  
**Auth:** Public  
**Success:** `200 OK`

Request body:

```json
{
  "email": "partha@example.com",
  "password": "StrongPassword123!"
}
```

Response body:

```json
{
  "success": true,
  "message": "Login successful.",
  "data": {
    "user": {
      "id": "usr_01",
      "name": "Partha Sarker",
      "email": "partha@example.com",
      "role": "user"
    },
    "access_token": "<access_token>",
    "token_type": "Bearer"
  }
}
```

### 5.3 Get current user

**`GET /auth/me`**  
**Auth:** Required  
**Success:** `200 OK`

Request body: none.

Response body:

```json
{
  "success": true,
  "message": "Authenticated user retrieved successfully.",
  "data": {
    "id": "usr_01",
    "name": "Partha Sarker",
    "email": "partha@example.com",
    "role": "user",
    "created_at": "2026-11-01T12:00:00Z"
  }
}
```

### 5.4 Logout

**`POST /auth/logout`**  
**Auth:** Required  
**Success:** `200 OK`

Request body: none.

Response body:

```json
{
  "success": true,
  "message": "Logged out successfully.",
  "data": null
}
```

If tokens are stateless and not revocable, logout behavior must be explained by the implementation.

---

## 6. User Profile

### 6.1 Get profile

**`GET /profile`**  
**Auth:** Required  
**Success:** `200 OK`

Request body: none.

Response body:

```json
{
  "success": true,
  "message": "Profile retrieved successfully.",
  "data": {
    "id": 1,
    "name": "Partha Sarker",
    "email": "parthasarker@example.com",
    "role": "ROLE_USER",
    "phone": null,
    "status": null,
    "profile_image": null,
    "email_verified": false,
    "last_login_at": null,
    "created_at": "2026-11-02T01:35:12.665405"
  },
  "timestamp": "2026-11-02T16:01:33.175100779"
}
```

### 6.2 Update profile

**`PATCH /profile`**  
**Auth:** Required  
**Success:** `200 OK`

Request body:

```json
{
  "name": "Partha Sarker",
  "phone": "+8801XXXXXXXXX",
  "profile_image" : "https://my-image.jpg"
}
```

Response body:

```json
{
  "success": true,
  "message": "Profile updated successfully.",
  "data": {
    "id": 1,
    "name": "Partha Sarker",
    "email": "parthasarker@example.com",
    "role": "ROLE_USER",
    "phone": null,
    "status": null,
    "profile_image": null,
    "email_verified": false,
    "last_login_at": null,
    "created_at": "2026-11-02T01:35:12.665405"
  }
}
```

Only allow fields intended to be user-editable. Do not allow a user to change their role, wallet balance, or account status through this endpoint.

---

## 7. Wallet

### 7.1 Get wallet balance

**`GET /wallet`**  
**Auth:** Required  
**Success:** `200 OK`

Request body: none.

Response body:

```json
{
  "success": true,
  "message": "Wallet retrieved successfully.",
  "data": {
    "currency": "BDT",
    "balance": 0.0000,
    "available_balance": 0.0000,
    "updated_at": "2026-11-02T01:35:12.704219"
  },
  "timestamp": "2026-11-02T17:00:17.490959815"
}
```

Amounts are represented as decimal strings to avoid floating-point precision problems. Use the currency and precision configured by the application.

### 7.2 Get wallet transaction history

**`GET /wallet/transactions?page=1&per_page=15`**  
**Auth:** Required  
**Success:** `200 OK`

Request body: none.

Response body:

```json
{
  "success": true,
  "message": "Wallet transactions retrieved successfully.",
  "data": [
    {
      "id": "txn_1001",
      "type": "deposit",
      "direction": "credit",
      "amount": "500.00",
      "currency": "BDT",
      "balance_before": "750.00",
      "balance_after": "1250.00",
      "status": "completed",
      "reference": "DEP-20261101-001",
      "description": "Deposit approved",
      "created_at": "2026-11-01T12:30:00Z"
    }
  ],
  "meta": {
    "current_page": 1,
    "per_page": 15,
    "total": 1,
    "last_page": 1
  }
}
```

Users must only be able to access their own wallet and transaction history.

---

## 8. Deposits

A deposit is a request to add funds to a wallet. For manual payment methods, a deposit should remain pending until verified and approved by an authorized administrator or trusted payment callback.

### 8.1 List available payment methods

**`GET /payment-methods`**  
**Auth:** Public or authenticated, depending on product policy  
**Success:** `200 OK`

Request body: none.

Response body:

```json
{
  "success": true,
  "message": "Payment methods retrieved successfully.",
  "data": [
    {
      "id": "pm_bkash",
      "name": "bKash",
      "type": "mobile_banking",
      "instructions": "Send the payment to the displayed merchant number.",
      "minimum_amount": "100.00",
      "maximum_amount": "10000.00",
      "currency": "BDT",
      "is_active": true
    }
  ]
}
```

Do not expose private gateway credentials or secret configuration.

### 8.2 Create deposit request

**`POST /deposits`**  
**Auth:** Required  
**Success:** `201 Created`

Request body:

```json
{
  "payment_method_id": "pm_bkash",
  "amount": "500.00",
  "sender_account": "01XXXXXXXXX",
  "payment_reference": "TXN8ABC123",
  "proof_url": "https://example.com/private-proof/receipt.jpg"
}
```

`proof_url` is optional if the implementation accepts an uploaded file instead. For multipart upload, document the exact field name and allowed file types in the implementation-specific version.

Response body:

```json
{
  "success": true,
  "message": "Deposit request submitted successfully.",
  "data": {
    "id": "dep_1001",
    "reference": "DEP-20261101-001",
    "amount": "500.00",
    "currency": "BDT",
    "payment_method": {
      "id": "pm_bkash",
      "name": "bKash"
    },
    "status": "pending",
    "created_at": "2026-11-01T12:30:00Z"
  }
}
```

### 8.3 List my deposits

**`GET /deposits?page=1&per_page=15`**  
**Auth:** Required  
**Success:** `200 OK`

Request body: none.

Response body:

```json
{
  "success": true,
  "message": "Deposits retrieved successfully.",
  "data": [
    {
      "id": "dep_1001",
      "reference": "DEP-20261101-001",
      "amount": "500.00",
      "currency": "BDT",
      "status": "pending",
      "payment_method": {
        "id": "pm_bkash",
        "name": "bKash"
      },
      "created_at": "2026-11-01T12:30:00Z",
      "reviewed_at": null
    }
  ],
  "meta": {
    "current_page": 1,
    "per_page": 15,
    "total": 1,
    "last_page": 1
  }
}
```

### 8.4 Get deposit details

**`GET /deposits/{depositId}`**  
**Auth:** Required; owner or authorized admin  
**Success:** `200 OK`

Request body: none.

Response body:

```json
{
  "success": true,
  "message": "Deposit retrieved successfully.",
  "data": {
    "id": "dep_1001",
    "reference": "DEP-20261101-001",
    "amount": "500.00",
    "currency": "BDT",
    "status": "pending",
    "payment_method": {
      "id": "pm_bkash",
      "name": "bKash"
    },
    "payment_reference": "TXN8ABC123",
    "admin_note": null,
    "created_at": "2026-11-01T12:30:00Z",
    "reviewed_at": null
  }
}
```

Do not return private proof files to unauthorized users. Use short-lived authorized links where necessary.

---

## 9. Transactions

### 9.1 List my transactions

**`GET /transactions?type=deposit&status=completed&page=1&per_page=15`**  
**Auth:** Required  
**Success:** `200 OK`

Request body: none.

Response body:

```json
{
  "success": true,
  "message": "Transactions retrieved successfully.",
  "data": [
    {
      "id": "txn_1001",
      "type": "deposit",
      "direction": "credit",
      "amount": "500.00",
      "currency": "BDT",
      "status": "completed",
      "reference": "DEP-20261101-001",
      "description": "Deposit approved",
      "created_at": "2026-11-01T12:30:00Z"
    }
  ],
  "meta": {
    "current_page": 1,
    "per_page": 15,
    "total": 1,
    "last_page": 1
  }
}
```

### 9.2 Get transaction details

**`GET /transactions/{transactionId}`**  
**Auth:** Required; transaction owner or authorized admin  
**Success:** `200 OK`

Request body: none.

Response body:

```json
{
  "success": true,
  "message": "Transaction retrieved successfully.",
  "data": {
    "id": "txn_1001",
    "type": "deposit",
    "direction": "credit",
    "amount": "500.00",
    "currency": "BDT",
    "balance_before": "750.00",
    "balance_after": "1250.00",
    "status": "completed",
    "reference": "DEP-20261101-001",
    "related_resource": {
      "type": "deposit",
      "id": "dep_1001"
    },
    "created_at": "2026-11-01T12:30:00Z"
  }
}
```

Transactions should be an auditable ledger. Do not let clients create arbitrary wallet credits or edit past transaction records.

---

## 10. Product Categories

### 10.1 List categories

**`GET /categories`**  
**Auth:** Public  
**Success:** `200 OK`

Request body: none.

Response body:

```json
{
  "success": true,
  "message": "Categories retrieved successfully.",
  "data": [
    {
      "id": "cat_01",
      "name": "Email Accounts",
      "slug": "email-accounts",
      "description": "Digital email-account products",
      "is_active": true
    },
    {
      "id": "cat_02",
      "name": "Proxies",
      "slug": "proxies",
      "description": "Proxy products",
      "is_active": true
    }
  ]
}
```

### 10.2 Get category details

**`GET /categories/{categoryId}`**  
**Auth:** Public  
**Success:** `200 OK`

Request body: none.

Response body:

```json
{
  "success": true,
  "message": "Category retrieved successfully.",
  "data": {
    "id": "cat_01",
    "name": "Email Accounts",
    "slug": "email-accounts",
    "description": "Digital email-account products",
    "is_active": true
  }
}
```

---

## 11. Products

### 11.1 List products

**`GET /products?category_id=cat_01&search=mail&page=1&per_page=15`**  
**Auth:** Public  
**Success:** `200 OK`

Request body: none.

Response body:

```json
{
  "success": true,
  "message": "Products retrieved successfully.",
  "data": [
    {
      "id": "prd_100",
      "category": {
        "id": "cat_01",
        "name": "Email Accounts",
        "slug": "email-accounts"
      },
      "name": "Email Account - Standard",
      "slug": "email-account-standard",
      "description": "Standard email account product.",
      "price": "50.00",
      "currency": "BDT",
      "available_quantity": 25,
      "is_available": true
    }
  ],
  "meta": {
    "current_page": 1,
    "per_page": 15,
    "total": 1,
    "last_page": 1
  }
}
```

### 11.2 Get product details

**`GET /products/{productId}`**  
**Auth:** Public  
**Success:** `200 OK`

Request body: none.

Response body:

```json
{
  "success": true,
  "message": "Product retrieved successfully.",
  "data": {
    "id": "prd_100",
    "category": {
      "id": "cat_01",
      "name": "Email Accounts",
      "slug": "email-accounts"
    },
    "name": "Email Account - Standard",
    "slug": "email-account-standard",
    "description": "Standard email account product.",
    "price": "50.00",
    "currency": "BDT",
    "available_quantity": 25,
    "is_available": true,
    "created_at": "2026-10-25T10:00:00Z"
  }
}
```

Never expose private inventory credentials in public product responses.

---

## 12. Orders

### 12.1 Create an order / purchase product

**`POST /orders`**  
**Auth:** Required  
**Success:** `201 Created`

Request body:

```json
{
  "items": [
    {
      "product_id": "prd_100",
      "quantity": 2
    }
  ],
  "idempotency_key": "purchase-7c8f6d0a-unique"
}
```

The server must calculate prices and totals from trusted product data. Do not accept a client-supplied total or client-supplied wallet balance.

Response body:

```json
{
  "success": true,
  "message": "Order created successfully.",
  "data": {
    "id": "ord_1001",
    "reference": "ORD-20261101-001",
    "status": "completed",
    "currency": "BDT",
    "subtotal": "100.00",
    "total": "100.00",
    "items": [
      {
        "product_id": "prd_100",
        "product_name": "Email Account - Standard",
        "quantity": 2,
        "unit_price": "50.00",
        "line_total": "100.00"
      }
    ],
    "wallet": {
      "balance_before": "1250.00",
      "balance_after": "1150.00"
    },
    "created_at": "2026-11-01T12:45:00Z"
  }
}
```

If the product is delivered immediately, delivery data should be returned only to the purchaser and only after the purchase is successfully committed. If the system uses asynchronous delivery, return a pending order and provide a separate authorized delivery endpoint.

### 12.2 List my orders

**`GET /orders?status=completed&page=1&per_page=15`**  
**Auth:** Required  
**Success:** `200 OK`

Request body: none.

Response body:

```json
{
  "success": true,
  "message": "Orders retrieved successfully.",
  "data": [
    {
      "id": "ord_1001",
      "reference": "ORD-20261101-001",
      "status": "completed",
      "currency": "BDT",
      "total": "100.00",
      "item_count": 1,
      "created_at": "2026-11-01T12:45:00Z"
    }
  ],
  "meta": {
    "current_page": 1,
    "per_page": 15,
    "total": 1,
    "last_page": 1
  }
}
```

### 12.3 Get order details

**`GET /orders/{orderId}`**  
**Auth:** Required; order owner or authorized admin  
**Success:** `200 OK`

Request body: none.

Response body:

```json
{
  "success": true,
  "message": "Order retrieved successfully.",
  "data": {
    "id": "ord_1001",
    "reference": "ORD-20261101-001",
    "status": "completed",
    "currency": "BDT",
    "subtotal": "100.00",
    "total": "100.00",
    "items": [
      {
        "id": "oi_01",
        "product_id": "prd_100",
        "product_name": "Email Account - Standard",
        "quantity": 2,
        "unit_price": "50.00",
        "line_total": "100.00"
      }
    ],
    "created_at": "2026-11-01T12:45:00Z"
  }
}
```

### 12.4 Get delivered order items

**`GET /orders/{orderId}/delivery`**  
**Auth:** Required; order owner or authorized admin  
**Success:** `200 OK`

Request body: none.

Response body:

```json
{
  "success": true,
  "message": "Order delivery retrieved successfully.",
  "data": {
    "order_id": "ord_1001",
    "status": "delivered",
    "items": [
      {
        "product_name": "Email Account - Standard",
        "credentials": {
          "username": "example-account",
          "password": "<redacted-secret>"
        }
      }
    ],
    "delivered_at": "2026-11-01T12:45:02Z"
  }
}
```

This example is illustrative. Do not return actual secrets in logs, public endpoints, or to anyone other than the authorized purchaser. Consider one-time reveal, access auditing, and encryption at rest for sensitive stock.

---

## 13. Payment Methods

### 13.1 Get payment method details

**`GET /payment-methods/{paymentMethodId}`**  
**Auth:** Public or authenticated, depending on product policy  
**Success:** `200 OK`

Request body: none.

Response body:

```json
{
  "success": true,
  "message": "Payment method retrieved successfully.",
  "data": {
    "id": "pm_bkash",
    "name": "bKash",
    "type": "mobile_banking",
    "instructions": "Follow the deposit instructions shown in the application.",
    "minimum_amount": "100.00",
    "maximum_amount": "10000.00",
    "currency": "BDT",
    "is_active": true
  }
}
```

---

## 14. Telegram Account Linking

These routes are optional and should only be included if the application actually supports Telegram linking.

### 14.1 Start Telegram linking

**`POST /telegram/link`**  
**Auth:** Required  
**Success:** `200 OK`

Request body:

```json
{
  "telegram_user_id": "123456789",
  "telegram_username": "example_user",
  "verification_code": "482913"
}
```

Response body:

```json
{
  "success": true,
  "message": "Telegram account linked successfully.",
  "data": {
    "linked": true,
    "telegram_user_id": "123456789",
    "telegram_username": "example_user",
    "linked_at": "2026-11-01T13:00:00Z"
  }
}
```

The server must verify ownership using a secure challenge or trusted bot flow. Do not trust an arbitrary client-supplied Telegram user ID.

### 14.2 Unlink Telegram

**`DELETE /telegram/link`**  
**Auth:** Required  
**Success:** `200 OK`

Request body: none.

Response body:

```json
{
  "success": true,
  "message": "Telegram account unlinked successfully.",
  "data": null
}
```

---

## 15. Admin Endpoints

All endpoints in this section require administrator authorization. These are proposed routes and should be aligned with the actual admin route group and permission system.

### 15.1 Dashboard summary

**`GET /admin/dashboard`**  
**Auth:** Admin  
**Success:** `200 OK`

Request body: none.

Response body:

```json
{
  "success": true,
  "message": "Dashboard summary retrieved successfully.",
  "data": {
    "total_users": 1200,
    "total_products": 42,
    "pending_deposits": 8,
    "total_orders": 530,
    "pending_orders": 3,
    "gross_sales": "250000.00",
    "currency": "BDT"
  }
}
```

### 15.2 List users

**`GET /admin/users?search=partha&status=active&page=1&per_page=15`**  
**Auth:** Admin  
**Success:** `200 OK`

Request body: none.

Response body:

```json
{
  "success": true,
  "message": "Users retrieved successfully.",
  "data": [
    {
      "id": "usr_01",
      "name": "Partha Sarker",
      "email": "partha@example.com",
      "role": "user",
      "status": "active",
      "wallet_balance": "1150.00",
      "created_at": "2026-11-01T12:00:00Z"
    }
  ],
  "meta": {
    "current_page": 1,
    "per_page": 15,
    "total": 1,
    "last_page": 1
  }
}
```

### 15.3 Get user details

**`GET /admin/users/{userId}`**  
**Auth:** Admin  
**Success:** `200 OK`

Request body: none.

Response body:

```json
{
  "success": true,
  "message": "User retrieved successfully.",
  "data": {
    "id": "usr_01",
    "name": "Partha Sarker",
    "email": "partha@example.com",
    "role": "user",
    "status": "active",
    "wallet_balance": "1150.00",
    "created_at": "2026-11-01T12:00:00Z"
  }
}
```

### 15.4 Update user status

**`PATCH /admin/users/{userId}/status`**  
**Auth:** Admin  
**Success:** `200 OK`

Request body:

```json
{
  "status": "suspended",
  "reason": "Policy violation"
}
```

Response body:

```json
{
  "success": true,
  "message": "User status updated successfully.",
  "data": {
    "id": "usr_01",
    "status": "suspended",
    "updated_at": "2026-11-01T13:10:00Z"
  }
}
```

### 15.5 Create product

**`POST /admin/products`**  
**Auth:** Admin  
**Success:** `201 Created`

Request body:

```json
{
  "category_id": "cat_01",
  "name": "Email Account - Standard",
  "slug": "email-account-standard",
  "description": "Standard email account product.",
  "price": "50.00",
  "currency": "BDT",
  "is_active": true
}
```

Response body:

```json
{
  "success": true,
  "message": "Product created successfully.",
  "data": {
    "id": "prd_100",
    "category_id": "cat_01",
    "name": "Email Account - Standard",
    "slug": "email-account-standard",
    "price": "50.00",
    "currency": "BDT",
    "is_active": true,
    "created_at": "2026-11-01T13:15:00Z"
  }
}
```

### 15.6 Update product

**`PATCH /admin/products/{productId}`**  
**Auth:** Admin  
**Success:** `200 OK`

Request body:

```json
{
  "name": "Email Account - Premium",
  "description": "Updated product description.",
  "price": "75.00",
  "is_active": true
}
```

Response body:

```json
{
  "success": true,
  "message": "Product updated successfully.",
  "data": {
    "id": "prd_100",
    "name": "Email Account - Premium",
    "description": "Updated product description.",
    "price": "75.00",
    "currency": "BDT",
    "is_active": true,
    "updated_at": "2026-11-01T13:20:00Z"
  }
}
```

### 15.7 Delete/deactivate product

**`DELETE /admin/products/{productId}`**  
**Auth:** Admin  
**Success:** `200 OK` or `204 No Content`

Recommended behavior is soft-delete/deactivation when order history references the product.

Response body (if returning JSON):

```json
{
  "success": true,
  "message": "Product deactivated successfully.",
  "data": null
}
```

### 15.8 Add product stock

**`POST /admin/products/{productId}/stock`**  
**Auth:** Admin  
**Success:** `201 Created`

Request body:

```json
{
  "items": [
    {
      "payload": {
        "username": "stock-account-001",
        "password": "<secret>"
      }
    },
    {
      "payload": {
        "username": "stock-account-002",
        "password": "<secret>"
      }
    }
  ]
}
```

Response body:

```json
{
  "success": true,
  "message": "Product stock added successfully.",
  "data": {
    "product_id": "prd_100",
    "added_count": 2,
    "available_quantity": 27
  }
}
```

Treat stock payloads as sensitive data. Encrypt secrets at rest where appropriate and never include raw stock values in routine admin list responses or logs.

### 15.9 List deposits for review

**`GET /admin/deposits?status=pending&page=1&per_page=15`**  
**Auth:** Admin  
**Success:** `200 OK`

Request body: none.

Response body:

```json
{
  "success": true,
  "message": "Deposit requests retrieved successfully.",
  "data": [
    {
      "id": "dep_1001",
      "reference": "DEP-20261101-001",
      "user": {
        "id": "usr_01",
        "name": "Partha Sarker",
        "email": "partha@example.com"
      },
      "amount": "500.00",
      "currency": "BDT",
      "payment_method": "bKash",
      "payment_reference": "TXN8ABC123",
      "status": "pending",
      "created_at": "2026-11-01T12:30:00Z"
    }
  ],
  "meta": {
    "current_page": 1,
    "per_page": 15,
    "total": 1,
    "last_page": 1
  }
}
```

### 15.10 Approve or reject deposit

**`PATCH /admin/deposits/{depositId}/review`**  
**Auth:** Admin  
**Success:** `200 OK`

Approve request:

```json
{
  "decision": "approve",
  "admin_note": "Payment verified."
}
```

Reject request:

```json
{
  "decision": "reject",
  "admin_note": "Payment reference could not be verified."
}
```

Response body:

```json
{
  "success": true,
  "message": "Deposit reviewed successfully.",
  "data": {
    "id": "dep_1001",
    "reference": "DEP-20261101-001",
    "amount": "500.00",
    "status": "approved",
    "admin_note": "Payment verified.",
    "reviewed_at": "2026-11-01T13:30:00Z"
  }
}
```

On approval, credit the wallet and create the matching ledger transaction atomically in a database transaction. Repeated approval requests must not credit the wallet twice.

### 15.11 List all orders

**`GET /admin/orders?status=completed&page=1&per_page=15`**  
**Auth:** Admin  
**Success:** `200 OK`

Request body: none.

Response body:

```json
{
  "success": true,
  "message": "Orders retrieved successfully.",
  "data": [
    {
      "id": "ord_1001",
      "reference": "ORD-20261101-001",
      "user": {
        "id": "usr_01",
        "name": "Partha Sarker",
        "email": "partha@example.com"
      },
      "status": "completed",
      "total": "100.00",
      "currency": "BDT",
      "created_at": "2026-11-01T12:45:00Z"
    }
  ],
  "meta": {
    "current_page": 1,
    "per_page": 15,
    "total": 1,
    "last_page": 1
  }
}
```

### 15.12 Get order details (admin)

**`GET /admin/orders/{orderId}`**  
**Auth:** Admin  
**Success:** `200 OK`

Request body: none.

Response body:

```json
{
  "success": true,
  "message": "Order retrieved successfully.",
  "data": {
    "id": "ord_1001",
    "reference": "ORD-20261101-001",
    "user_id": "usr_01",
    "status": "completed",
    "subtotal": "100.00",
    "total": "100.00",
    "currency": "BDT",
    "created_at": "2026-11-01T12:45:00Z"
  }
}
```

### 15.13 Create category

**`POST /admin/categories`**  
**Auth:** Admin  
**Success:** `201 Created`

Request body:

```json
{
  "name": "Email Accounts",
  "slug": "email-accounts",
  "description": "Digital email-account products",
  "is_active": true
}
```

Response body:

```json
{
  "success": true,
  "message": "Category created successfully.",
  "data": {
    "id": "cat_01",
    "name": "Email Accounts",
    "slug": "email-accounts",
    "description": "Digital email-account products",
    "is_active": true
  }
}
```

### 15.14 Update category

**`PATCH /admin/categories/{categoryId}`**  
**Auth:** Admin  
**Success:** `200 OK`

Request body:

```json
{
  "name": "Email Accounts",
  "description": "Updated description.",
  "is_active": true
}
```

Response body:

```json
{
  "success": true,
  "message": "Category updated successfully.",
  "data": {
    "id": "cat_01",
    "name": "Email Accounts",
    "slug": "email-accounts",
    "description": "Updated description.",
    "is_active": true
  }
}
```

### 15.15 Create/update payment method

**`POST /admin/payment-methods`**  
**Auth:** Admin  
**Success:** `201 Created`

Request body:

```json
{
  "name": "bKash",
  "type": "mobile_banking",
  "instructions": "Follow the payment instructions.",
  "minimum_amount": "100.00",
  "maximum_amount": "10000.00",
  "currency": "BDT",
  "is_active": true
}
```

Response body:

```json
{
  "success": true,
  "message": "Payment method created successfully.",
  "data": {
    "id": "pm_bkash",
    "name": "bKash",
    "type": "mobile_banking",
    "minimum_amount": "100.00",
    "maximum_amount": "10000.00",
    "currency": "BDT",
    "is_active": true
  }
}
```

For updates, a conventional proposed route is `PATCH /admin/payment-methods/{paymentMethodId}` with the fields to change.

### 15.16 Wallet adjustment

**`POST /admin/users/{userId}/wallet-adjustments`**  
**Auth:** Admin with explicit wallet-adjustment permission  
**Success:** `201 Created`

Request body:

```json
{
  "direction": "credit",
  "amount": "100.00",
  "reason": "Support adjustment",
  "reference": "ADJ-20261101-001",
  "idempotency_key": "adjustment-unique-001"
}
```

Response body:

```json
{
  "success": true,
  "message": "Wallet adjustment recorded successfully.",
  "data": {
    "transaction_id": "txn_2001",
    "user_id": "usr_01",
    "direction": "credit",
    "amount": "100.00",
    "balance_before": "1150.00",
    "balance_after": "1250.00",
    "reason": "Support adjustment",
    "reference": "ADJ-20261101-001",
    "created_at": "2026-11-01T13:45:00Z"
  }
}
```

Every manual adjustment must require a reason, record the acting administrator, and create an immutable ledger entry.

---

## 16. Error Codes

Use HTTP status codes consistently. Exact error response shapes depend on the exception handler.

| HTTP status | Suggested code | Meaning |
|---|---|---|
| `400` | `BAD_REQUEST` | Request cannot be processed |
| `401` | `UNAUTHENTICATED` | Missing, invalid, or expired authentication |
| `403` | `FORBIDDEN` | Authenticated user lacks permission |
| `404` | `RESOURCE_NOT_FOUND` | Resource does not exist or is not accessible |
| `409` | `CONFLICT` | Duplicate request or incompatible state |
| `422` | `VALIDATION_FAILED` | Input validation failed |
| `429` | `RATE_LIMITED` | Too many requests |
| `500` | `INTERNAL_SERVER_ERROR` | Unexpected server error |

### Example: insufficient wallet balance

HTTP status: `422 Unprocessable Entity` (or `409 Conflict`, according to the implemented API contract).

```json
{
  "success": false,
  "message": "Insufficient wallet balance.",
  "error": {
    "code": "INSUFFICIENT_BALANCE"
  }
}
```

### Example: product out of stock

HTTP status: `409 Conflict`.

```json
{
  "success": false,
  "message": "The requested product quantity is not available.",
  "error": {
    "code": "INSUFFICIENT_STOCK"
  }
}
```

### Example: unauthenticated request

HTTP status: `401 Unauthorized`.

```json
{
  "success": false,
  "message": "Unauthenticated.",
  "error": {
    "code": "UNAUTHENTICATED"
  }
}
```

---

## 17. Security and Financial Integrity

Because the application handles wallet balances and purchases, the following rules are essential:

1. **Server-side price calculation:** never trust a price or order total supplied by the client.
2. **Atomic wallet changes:** update wallet balance and create the corresponding ledger transaction in the same database transaction.
3. **Prevent double spending:** lock the wallet row or use an equivalent concurrency-safe update when spending funds.
4. **Idempotency:** use an idempotency key or unique business reference for purchases, deposit approval, and wallet adjustments.
5. **Immutable ledger:** do not edit or delete completed financial transactions; use compensating transactions for corrections.
6. **Stock reservation:** reserve/decrement stock atomically with the order so concurrent purchases cannot sell the same item twice.
7. **Authorization:** enforce ownership checks on every user-specific deposit, transaction, order, and delivery endpoint.
8. **Admin audit logs:** record the administrator, action, target resource, timestamp, and reason for financial or account changes.
9. **Sensitive data:** never log passwords, tokens, payment secrets, or delivered credentials. Encrypt sensitive inventory values at rest.
10. **Rate limiting:** apply stricter limits to login, registration, deposit creation, and other abuse-prone endpoints.
11. **Payment verification:** do not credit a wallet solely because the client says a payment succeeded. Verify through a trusted callback or administrator review.
12. **Webhook security:** if payment webhooks exist, verify signatures, reject replays, and process events idempotently.

---

## 18. Environment Configuration

Example only; use the actual variables required by the project.

```env
APP_URL=http://localhost:8000
APP_ENV=local
APP_DEBUG=true

# Configure the actual authentication and database variables
# from the application's .env.example file.
```

Do not commit `.env` files, API secrets, database credentials, or production tokens to source control. In production, set debug mode off and use HTTPS.

---

## 19. Endpoint Summary

| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `POST` | `/auth/register` | Public | Register |
| `POST` | `/auth/login` | Public | Login |
| `GET` | `/auth/me` | User | Current user |
| `POST` | `/auth/logout` | User | Logout |
| `GET` | `/profile` | User | Get profile |
| `PATCH` | `/profile` | User | Update profile |
| `GET` | `/wallet` | User | Wallet balance |
| `GET` | `/wallet/transactions` | User | Wallet history |
| `GET` | `/payment-methods` | Public/optional auth | Active payment methods |
| `GET` | `/payment-methods/{paymentMethodId}` | Public/optional auth | Payment method detail |
| `POST` | `/deposits` | User | Create deposit request |
| `GET` | `/deposits` | User | My deposits |
| `GET` | `/deposits/{depositId}` | Owner/Admin | Deposit detail |
| `GET` | `/transactions` | User | Transaction history |
| `GET` | `/transactions/{transactionId}` | Owner/Admin | Transaction detail |
| `GET` | `/categories` | Public | List categories |
| `GET` | `/categories/{categoryId}` | Public | Category detail |
| `GET` | `/products` | Public | List products |
| `GET` | `/products/{productId}` | Public | Product detail |
| `POST` | `/orders` | User | Purchase product |
| `GET` | `/orders` | User | My orders |
| `GET` | `/orders/{orderId}` | Owner/Admin | Order detail |
| `GET` | `/orders/{orderId}/delivery` | Owner/Admin | Delivered items |
| `POST` | `/telegram/link` | User | Link Telegram (optional) |
| `DELETE` | `/telegram/link` | User | Unlink Telegram (optional) |
| `GET` | `/admin/dashboard` | Admin | Dashboard summary |
| `GET` | `/admin/users` | Admin | List users |
| `GET` | `/admin/users/{userId}` | Admin | User detail |
| `PATCH` | `/admin/users/{userId}/status` | Admin | Update user status |
| `POST` | `/admin/products` | Admin | Create product |
| `PATCH` | `/admin/products/{productId}` | Admin | Update product |
| `DELETE` | `/admin/products/{productId}` | Admin | Deactivate product |
| `POST` | `/admin/products/{productId}/stock` | Admin | Add stock |
| `GET` | `/admin/deposits` | Admin | List deposits for review |
| `PATCH` | `/admin/deposits/{depositId}/review` | Admin | Approve/reject deposit |
| `GET` | `/admin/orders` | Admin | List all orders |
| `GET` | `/admin/orders/{orderId}` | Admin | Admin order detail |
| `POST` | `/admin/categories` | Admin | Create category |
| `PATCH` | `/admin/categories/{categoryId}` | Admin | Update category |
| `POST` | `/admin/payment-methods` | Admin | Create payment method |
| `PATCH` | `/admin/payment-methods/{paymentMethodId}` | Admin | Update payment method |
| `POST` | `/admin/users/{userId}/wallet-adjustments` | Admin | Manual wallet adjustment |

---

## Final implementation note

This file is a comprehensive **proposed contract**, not a verified inventory of the deployed SimpleWallet API. Before publishing it as official documentation, compare every endpoint against the actual route definitions, controllers, request validators/Form Requests, resources/transformers, authentication middleware, and database models. Remove endpoints that do not exist and replace example payloads with the exact fields and response shapes returned by the application.
