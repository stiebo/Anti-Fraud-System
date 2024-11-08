# SpringBoot Rest API Anti-Fraud System (Java)

## Background

Frauds carry significant financial costs and risks for all stakeholders. So, the presence of an anti-fraud system is a necessity for any serious e-commerce platform.

The Anti-Fraud System project provides a comprehensive framework for detecting and preventing fraudulent financial transactions. By integrating role-based access control, RESTful APIs, heuristic validation rules, and adaptive feedback mechanisms, the system offers a robust solution for financial institutions to safeguard against fraud. Leveraging Spring Boot and its associated technologies, the project demonstrates best practices in building secure, scalable, and maintainable applications in the financial sector.

Link to Github repository: [https://github.com/stiebo/Anti-Fraud-System](https://github.com/stiebo/Anti-Fraud-System)

Check out my Github profile: [https://github.com/stiebo](https://github.com/stiebo)

Link to the learning project: [https://hyperskill.org/projects/232](https://hyperskill.org/projects/232)

Check out my learning profile: [https://hyperskill.org/profile/500961738](https://hyperskill.org/profile/500961738)

## Key Components of the Anti-Fraud System

1. **Role-Based Access Control**:

    - **User Roles**: The system defines specific roles, including **Administrator**, **Merchant**, and **Support**.

    - **Permissions**:

        - **Administrator**: Manages user roles and access rights.

        - **Merchant**: Submits transactions for validation.

        - **Support**: Reviews and provides feedback on transactions.

    - This structure ensures that users have access only to functionalities pertinent to their roles, enhancing security and operational efficiency.


2. **RESTful API Endpoints**:

    - The system offers a set of REST endpoints for user interactions and transaction management:

        - **User Management**: Endpoints for registering users, assigning roles, and managing access.

        - **Transaction Processing**: Endpoints for submitting transactions and retrieving their statuses.

        - **Feedback Mechanism**: Allows fraud analysts to provide feedback on transaction validations.

    - These endpoints facilitate seamless communication between clients and the server, adhering to REST principles.


3. **Transaction Validation with Heuristic Rules**:

    - The system employs heuristic rules to assess transactions:

        - **Amount-Based Validation**: Transactions are categorized as ALLOWED, MANUAL_PROCESSING, or PROHIBITED based on their amounts.

        - **IP and Card Monitoring**: Identifies and blocks transactions from suspicious IP addresses or using stolen card numbers.

        - **Regional Analysis**: Evaluates transactions based on geographic regions to detect anomalies.

    - These rules help in identifying potentially fraudulent activities by analyzing transaction patterns and attributes.


4. **Feedback Mechanism**:

    - Support users (Fraud analysts) can provide feedback on transaction validations, indicating whether a transaction was correctly categorized.

    - The system adjusts its heuristic thresholds based on this feedback, improving its accuracy over time.

    - This adaptive approach ensures the system evolves with changing fraud patterns and reduces false positives or negatives.


5. **Authentication and Authorization**:

    - Utilizes Spring Security to implement authentication and authorization mechanisms.

    - Ensures that only authenticated users can access the system, with permissions tailored to their roles.

    - This setup protects sensitive operations and data from unauthorized access.


6. **Data Persistence**:

    - Employs Spring Data JPA for database interactions, managing user information, transaction records, and feedback data.

    - Ensures data integrity and supports efficient querying and storage operations.

## Tests
Integration tests were performed as part of the Hyperskill project with 150+ tests passed. See https://hyperskill.org/projects/232

## Users, Roles and Authorization

Our service supports the following roles:

| Endpoint / Role                               | Anonymous | MERCHANT | ADMINISTRATOR | SUPPORT |
|-----------------------------------------------|-----------|----------|---------------|---------|
| POST /api/antifraud/transaction               | -         | +        | -             | -       |
| POST /api/auth/user                           | +         | +        | +             | +       |
| GET /api/auth/list                            | -         | -        | +             | +       |
| DELETE /api/auth/user                         | -         | -        | +             | -       |
| PUT /api/auth/access                          | -         | -        | +             | -       |
| PUT /api/auth/role                            | -         | -        | +             | -       |
| POST, DELETE, GET api/antifraud/suspicious-ip | -         | -        | -             | +       |
| POST, DELETE, GET api/antifraud/stolencard    | -         | -        | -             | +       |
| GET /api/antifraud/history                    | -         | -        | -             | +       |
| PUT /api/antifraud/transaction                | -         | -        | -             | +       |

The service requires Http Basic authentication for all endpoints except for user signup.
Users can sign up themselves via *POST /api/auth/user*. The Administrator is the user who registered first, all subsequent registrations automatically receive the MERCHANT role and their account is locked by default. Users can be unlocked and roles changed by the Administrator (see below).

## Transaction validation
As the central entry point to the API, transactions can be posted by customers (merchants).

Transactions are checked based on
1. Transaction amount:
- Transactions with a sum of lower or equal to <allowed-threshold> are ALLOWED (default threshold: 200) 
- Transactions with a sum of greater than 200 but lower or equal than <manual_processing-threshold> require MANUAL_PROCESSING (default threshold: 1500)
- Transactions with a sum of greater than <manual_processing-threshold> are PROHIBITED.
2. Stolen cards (checked also using the Luhn algorithm) are PROHIBITED
3. Suspicious IP Addresses (must be valid IPv4 address) are PROHIBITED
4. unique regions and IP addresses (correlation):
- Transaction is PROHIBITED if there are transactions with the same number within the last hour from more than 2 regions or more than 2 unique IP addresses of the world other than the region or IP address of the transaction that is currently being verified;
- Transaction is sent for MANUAL_PROCESSING if there are transactions with the same number within the last hour from 2 regions or 2 unique IP addresses of the world other than the region or IP address of the transaction that is currently being verified;

Possible regions (Code, Description):
- EAP	East Asia and Pacific
- ECA	Europe and Central Asia
- HIC	High-Income countries
- LAC	Latin America and the Caribbean
- MENA  The Middle East and North Africa
- SA	South Asia
- SSA	Sub-Saharan Africa

## Threshold adjustment
Fraud analysts provide feedback on transaction validations, indicating whether a transaction was correctly categorized.
The system then adjusts its heuristic thresholds based on this feedback, improving its accuracy over time:

| Transaction Validity / Feedback | ALLOWED                 | MANUAL_PROCESSING                  | PROHIBITED                    |
|---------------------------------|-------------------------|------------------------------------|-------------------------------|
| ALLOWED                         | exception               | decrease `<allowed-threshold>`     | decrease `<allowed-threshold>` |
| MANUAL_PROCESSING               | increase `<allowed-threshold>` | exception                        | decrease `<manual-threshold>`  |
| PROHIBITED                      | increase `<allowed-threshold>` | increase `<manual-threshold>`    | exception                      |

# Anti-Fraud System API Documentation

## User Management Endpoints

### Register User
- **Endpoint**: `POST /api/auth/user`
- **Description**: Registers a new user in the system.
- **Roles Authorized**: All (Anonymous, Merchant, Administrator, Support)
- **Request Body**:
  ```json
  {
    "name": "string",
    "username": "string",
    "password": "string"
  }
  ```
- **Responses**:
    - **201 Created**: User successfully registered.
    - **409 Conflict**: Username already exists.
    - **400 Bad Request**: Invalid input data.

### Delete User
- **Endpoint**: `DELETE /api/auth/user/{username}`
- **Description**: Deletes an existing user.
- **Roles Authorized**: Administrator
- **Path Parameter**:
    - `username` (string): The username of the user to delete.
- **Responses**:
    - **200 OK**: User successfully deleted.
    - **404 Not Found**: User not found.

### List Users
- **Endpoint**: `GET /api/auth/list`
- **Description**: Retrieves a list of all registered users.
- **Roles Authorized**: Administrator, Support
- **Responses**:
    - **200 OK**: List of users returned.

---

## Transaction Management Endpoints

### Process Transaction
- **Endpoint**: `POST /api/antifraud/transaction`
- **Description**: Submits a transaction for fraud analysis.
- **Roles Authorized**: Merchant
- **Request Body**:
  ```json
  {
    "amount": number,
    "ip": "string",
    "number": "string",
    "region": "string",
    "date": "string"
  }
  ```
- **Responses**:
    - **200 OK**: Transaction processed with result.
    - **400 Bad Request**: Invalid input data.

### Provide Transaction Feedback
- **Endpoint**: `PUT /api/antifraud/transaction`
- **Description**: Submits feedback on a transaction's validity.
- **Roles Authorized**: Support
- **Request Body**:
  ```json
  {
    "transactionId": number,
    "feedback": "ALLOWED" | "MANUAL_PROCESSING" | "PROHIBITED"
  }
  ```
- **Responses**:
    - **200 OK**: Feedback submitted.
    - **404 Not Found**: Transaction not found.
    - **400 Bad Request**: Invalid feedback.

### Get Transaction History
- **Endpoint**: `GET /api/antifraud/history`
- **Description**: Retrieves the history of processed transactions.
- **Roles Authorized**: Support
- **Responses**:
    - **200 OK**: List of transactions returned.

### Get Transaction History by Card Number
- **Endpoint**: `GET /api/antifraud/history/{number}`
- **Description**: Retrieves transaction history for a specific card number.
- **Roles Authorized**: Support
- **Path Parameter**:
    - `number` (string): The card number.
- **Responses**:
    - **200 OK**: List of transactions returned.
    - **404 Not Found**: No transactions found for the card number.

---

## Suspicious IP Management Endpoints

### Add Suspicious IP
- **Endpoint**: `POST /api/antifraud/suspicious-ip`
- **Description**: Adds an IP address to the suspicious list.
- **Roles Authorized**: Support
- **Request Body**:
  ```json
  {
    "ip": "string"
  }
  ```
- **Responses**:
    - **201 Created**: IP address added.
    - **409 Conflict**: IP address already exists.
    - **400 Bad Request**: Invalid IP address.

### Delete Suspicious IP
- **Endpoint**: `DELETE /api/antifraud/suspicious-ip/{ip}`
- **Description**: Removes an IP address from the suspicious list.
- **Roles Authorized**: Support
- **Path Parameter**:
    - `ip` (string): The IP address to remove.
- **Responses**:
    - **200 OK**: IP address removed.
    - **404 Not Found**: IP address not found.

### List Suspicious IPs
- **Endpoint**: `GET /api/antifraud/suspicious-ip`
- **Description**: Retrieves the list of suspicious IP addresses.
- **Roles Authorized**: Support
- **Responses**:
    - **200 OK**: List of IP addresses returned.

---

## Stolen Card Management Endpoints

### Add Stolen Card
- **Endpoint**: `POST /api/antifraud/stolencard`
- **Description**: Adds a card number to the stolen list.
- **Roles Authorized**: Support
- **Request Body**:
  ```json
  {
    "number": "string"
  }
  ```
- **Responses**:
    - **201 Created**: Card number added.
    - **409 Conflict**: Card number already exists.
    - **400 Bad Request**: Invalid card number.

### Delete Stolen Card
- **Endpoint**: `DELETE /api/antifraud/stolencard/{number}`
- **Description**: Removes a card number from the stolen list.
- **Roles Authorized**: Support
- **Path Parameter**:
    - `number` (string): The card number to remove.
- **Responses**:
    - **200 OK**: Card number removed.
    - **404 Not Found**: Card number not found.
