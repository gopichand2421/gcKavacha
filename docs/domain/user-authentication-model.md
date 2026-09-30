# User & Authentication Model

## 1. Purpose

This document defines the user identity, authentication, and authorization model for GcKavacha.

The model provides the foundation for:

* User registration
* User authentication
* Password management
* JWT-based authentication
* Role-based authorization
* Current-user identification
* Future team and project access control

---

# 2. User Entity

The primary authentication entity is `User`.

```text
User
│
├── Identity
│   ├── id
│   ├── username
│   └── email
│
├── Profile
│   ├── firstName
│   └── lastName
│
├── Credentials
│   └── passwordHash
│
├── Authorization
│   └── roles
│
├── Account
│   ├── status
│   └── timestamps
│
└── Security
    └── authentication metadata
```

---

# 3. User Fields

| Field          | Type    | Required | Description                     |
| -------------- | ------- | -------: | ------------------------------- |
| `id`           | String  |      Yes | Unique user identifier          |
| `username`     | String  |      Yes | Unique login/display identifier |
| `email`        | String  |      Yes | Unique email address            |
| `passwordHash` | String  |      Yes | Securely hashed password        |
| `firstName`    | String  |       No | User first name                 |
| `lastName`     | String  |       No | User last name                  |
| `roles`        | Set     |      Yes | Roles assigned to the user      |
| `status`       | Enum    |      Yes | Account status                  |
| `createdAt`    | Instant |      Yes | Account creation time           |
| `updatedAt`    | Instant |      Yes | Last modification time          |

---

# 4. User Status

The MVP supports:

```text
ACTIVE
DISABLED
LOCKED
```

### ACTIVE

The user can authenticate and access permitted resources.

### DISABLED

The account has been intentionally disabled.

Authentication must be rejected.

### LOCKED

The account has been temporarily locked because of security or administrative conditions.

Authentication must be rejected until the account is unlocked.

---

# 5. Roles

The initial MVP roles are:

```text
USER
ADMIN
```

Future roles may include:

```text
PROJECT_ADMIN
PROJECT_MEMBER
INCIDENT_RESPONDER
ON_CALL_ENGINEER
```

The MVP should keep the authorization model extensible.

---

# 6. Role Semantics

## USER

A normal authenticated GcKavacha user.

Capabilities will depend on project membership and future authorization rules.

## ADMIN

Platform-level administrative user.

The exact administrative permissions should be implemented through explicit authorization rules rather than implicitly granting access to every resource.

---

# 7. Authentication Flow

The initial authentication flow is:

```text
                ┌─────────────┐
                │    Client   │
                └──────┬──────┘
                       │
                       │ Login
                       ▼
              ┌─────────────────┐
              │ Authentication  │
              │     API         │
              └────────┬────────┘
                       │
                       ▼
                Find User
                       │
                       ▼
              Verify Password
                       │
                 ┌─────┴─────┐
                 │           │
              Invalid       Valid
                 │           │
                 ▼           ▼
               401        Generate JWT
                             │
                             ▼
                       Return Token
```

---

# 8. Registration Flow

```text
Client
  │
  │ POST /api/auth/register
  ▼
Authentication API
  │
  ├── Validate request
  │
  ├── Check email uniqueness
  │
  ├── Check username uniqueness
  │
  ├── Hash password
  │
  └── Create User
          │
          ▼
      MongoDB
```

Passwords must never be stored in plaintext.

---

# 9. Login Flow

```text
Client
  │
  │ POST /api/auth/login
  ▼
Authentication API
  │
  ├── Find user
  │
  ├── Validate account status
  │
  ├── Verify password
  │
  └── Generate JWT
          │
          ▼
       Client
```

Invalid credentials must produce a generic authentication failure response.

The API should not reveal whether:

* The username exists
* The email exists
* The password was incorrect
* The account exists but is disabled

---

# 10. JWT

The MVP uses JWT for stateless API authentication.

The token should contain only the information required by the API.

Example conceptual claims:

```json
{
  "sub": "user-id",
  "roles": [
    "USER"
  ],
  "iat": 1759248000,
  "exp": 1759251600
}
```

The exact signing algorithm and secret-management mechanism will be documented in the authentication implementation ADR.

---

# 11. Request Authentication

Authenticated API requests use:

```text
Authorization: Bearer <JWT>
```

The request flow is:

```text
HTTP Request
     │
     ▼
JWT Authentication Filter
     │
     ├── Extract token
     ├── Validate signature
     ├── Validate expiration
     ├── Extract user identity
     └── Create Security Context
             │
             ▼
       Controller / Service
```

---

# 12. Authorization

Authentication answers:

> Who is the user?

Authorization answers:

> What can this user access?

GcKavacha should keep these concerns separate.

```text
Authentication
      │
      ▼
User Identity
      │
      ▼
Authorization
      │
      ├── Role
      ├── Project Membership
      └── Resource Permissions
```

---

# 13. Future Resource Authorization

As project and service management is introduced, authorization will evolve toward:

```text
User
 │
 ├── Role
 │
 └── Project Membership
        │
        └── Project
              │
              ├── Services
              ├── Environments
              └── Incidents
```

This prevents authentication roles from becoming the only authorization mechanism.

---

# 14. MongoDB Collection

Users will be stored in:

```text
users
```

Conceptual document:

```json
{
  "_id": "user-123",
  "username": "gopi",
  "email": "user@example.com",
  "passwordHash": "<hashed-password>",
  "firstName": "Gopi",
  "lastName": "Chand",
  "roles": [
    "USER"
  ],
  "status": "ACTIVE",
  "createdAt": "2026-09-30T10:00:00Z",
  "updatedAt": "2026-09-30T10:00:00Z"
}
```

The password hash must never be returned through public APIs.

---

# 15. Database Constraints

The following fields must be unique:

```text
username
email
```

Recommended indexes:

```text
username
email
```

Email comparisons should use a consistent normalization strategy.

---

# 16. Security Requirements

The authentication implementation must follow these principles:

### Password Storage

Never store plaintext passwords.

Use a strong password hashing algorithm such as BCrypt or an equivalent adaptive password hashing mechanism.

### Secrets

JWT signing secrets must not be committed to Git.

Use environment variables or a secret-management solution.

### Token Exposure

Authentication tokens must not be written to application logs.

### Error Responses

Authentication errors should not expose sensitive account information.

### Input Validation

Registration and login requests must be validated before processing.

---

# 17. API Boundary

Initial authentication APIs:

```text
POST /api/auth/register
POST /api/auth/login
GET  /api/auth/me
```

Future APIs:

```text
POST /api/auth/logout
POST /api/auth/refresh
POST /api/auth/change-password
POST /api/auth/forgot-password
POST /api/auth/reset-password
```

Only the first three are part of the initial MVP authentication flow.

---

# 18. Authentication Error Model

Authentication failures should use consistent API responses.

Conceptually:

```json
{
  "code": "AUTHENTICATION_FAILED",
  "message": "Authentication failed"
}
```

Do not expose internal security details.

---

# 19. Domain Relationships

Current:

```text
User
 │
 └── roles
```

Future:

```text
User
 │
 ├── roles
 │
 ├── project memberships
 │
 └── team memberships
```

---

# 20. Authentication Architecture

```text
┌───────────────────────┐
│      React UI         │
└───────────┬───────────┘
            │
            │ HTTPS
            ▼
┌───────────────────────┐
│   Spring Boot API     │
│                       │
│ Authentication        │
│ Authorization         │
│ JWT Filter            │
└───────────┬───────────┘
            │
            ▼
┌───────────────────────┐
│       MongoDB         │
│                       │
│       users           │
└───────────────────────┘
```

---

# 21. Testing Requirements

Authentication implementation must eventually include tests for:

### Registration

* Valid registration
* Duplicate email
* Duplicate username
* Invalid email
* Invalid password
* Missing required fields

### Login

* Valid credentials
* Invalid password
* Unknown user
* Disabled user
* Locked user

### JWT

* Valid token
* Expired token
* Invalid signature
* Missing token
* Malformed token

### Authorization

* Authenticated user
* Unauthenticated request
* USER role
* ADMIN role
* Insufficient permissions

---

# 22. Future Enhancements

The following are outside the initial implementation:

* Refresh tokens
* OAuth2 / OIDC
* SSO
* MFA
* Password reset
* Account verification
* Session management
* API keys
* Service-to-service authentication
* Fine-grained permissions
* Team-level authorization

These should be introduced through separate stories.

---

# 23. Definition of Done

S18 is complete when:

* [ ] User entity is defined
* [ ] User fields are documented
* [ ] User statuses are defined
* [ ] Initial roles are defined
* [ ] Registration flow is documented
* [ ] Login flow is documented
* [ ] JWT responsibility is defined
* [ ] Authentication and authorization are separated
* [ ] MongoDB user collection is defined
* [ ] Unique username/email requirements are documented
* [ ] Security requirements are documented
* [ ] Initial authentication APIs are defined
* [ ] Authentication testing requirements are documented
* [ ] Future authentication capabilities are clearly separated from MVP

````

### After S18

The E02 implementation sequence is:

```text
S18 User & Authentication Model
        ↓
S19 User Persistence
        ↓
S20 User Registration API
        ↓
S21 Secure Password Hashing
        ↓
S22 User Login API
        ↓
S23 JWT Authentication
        ↓
S24 JWT Authentication Filter
        ↓
S25 User Roles
        ↓
S26 Role-Based Authorization
        ↓
S27 Current User API
        ↓
S28 Frontend Authentication State
        ↓
S29 Login Screen
        ↓
S30 Protected Routes
        ↓
S31 Logout
        ↓
S32 Authentication Integration Tests
````

**Next immediate task: S19 — Implement User Persistence (2h).**