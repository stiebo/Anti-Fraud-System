# SpringBoot Rest API Anti-Fraud System (Java)

## Background and Task
This project demonstrates (in a simplified form) the principles of anti-fraud systems in the financial sector. For this project, we will work on a system with an expanded role model, a set of REST endpoints responsible for interacting with users, and an internal transaction validation logic based on a set of heuristic rules.

Frauds carry significant financial costs and risks for all stakeholders. So, the presence of an anti-fraud system is a necessity for any serious e-commerce platform.

Let's implement a simple anti-fraud system consisting of one rule — heuristics. In the beginning, there's one simple measure that prevents fraudsters from illegally transferring money from an account. Suppose some scammers acquired access to confidential financial information through phishing or pharming. They immediately try to transfer as much as possible. Most of the time, the account holder is not aware of the attack. The anti-fraud system should prevent it before it is too late.

Link to the project: https://hyperskill.org/projects/232

Check out my profile: https://hyperskill.org/profile/500961738


## Data storage
The service includes an H2 file database for all data storage.

## Tests
Integration tests were performed as part of the Hyperskill project with 150+ tests passed. See https://hyperskill.org/projects/232

# REST API Documentation
All documentation retrieved from https://hyperskill.org/projects/232, provided by JetBrains Academy.

Documentation provides an overview of the endpoints, role model, password requirements, security logging events, and other important information related to our REST API.

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

## Endpoints

### POST /api/antifraud/transaction
Allowed role: MERCHANT

Central entry point to the API. The endpoint receives a new transaction and then determines whether the transaction is ALLOWED, PROHIBITED, or requires MANUAL_PROCESSING through a SUPPORT-User

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

Request Body:
```json
{
  "amount": <Long>,
  "ip": "<String value, not empty>",
  "number": "<String value, not empty>",
  "region": "<String value, not empty>",
  "date": "yyyy-MM-ddTHH:mm:ss"
}
```

Regions (Code, Description):
- EAP	East Asia and Pacific
- ECA	Europe and Central Asia
- HIC	High-Income countries
- LAC	Latin America and the Caribbean
- MENA  The Middle East and North Africa
- SA	South Asia
- SSA	Sub-Saharan Africa

Response (200 OK):
```json
{
  "result": <[ALLOWED, MANUAL_PROCESSING, PROHIBITED]>,
  "info": <String>
}
```
In the case of the PROHIBITED or MANUAL_PROCESSING result, the info field contains the reason for rejecting the transaction, options include: amount, card-number, ip, ip-correlation, region-correlation.

### POST /api/auth/user

Allowed role: Anonymous

Available to any unauthorized user to register to the service. Any new user (except the first one) is automatically locked and received the MERCHANT role.

Request Body:
```json
{
   "name": "<String value, not empty>",
   "username": "<String value, not empty>",
   "password": "<String value, not empty>"
}
```

Response (201 CREATED):
```json
{
   "id": <Long value, not empty>,
   "name": "<String value, not empty>",
   "username": "<String value, not empty>",
  "role": <[MERCHANT,ADMINISTRATOR,SUPPORT]>
}
```

### GET /api/auth/list

Allowed role: ADMINISTRATOR, SUPPORT

Return a list of all registered users.

```json
[
    {
        "id": <user1 id>,
        "name": "<user1 name>",
        "username": "<user1 username>",
        "role": "<user1 role>"
    },
     ...
    {
        "id": <userN id>,
        "name": "<userN name>",
        "username": "<userN username>",
        "role": "<userN role>"
    }
]
```

### DELETE /api/auth/user/{username}

Allowed role: ADMINISTRATOR

{username} specifies the user that should be deleted

Response (200 OK):
```json
{
   "username": "<username>",
   "status": "Deleted successfully!"
}
```

### PUT /api/auth/access 

Allowed role: ADMINISTRATOR

Request:
```json
{
  "username": "<String value, not empty>",
  "operation": "<[LOCK, UNLOCK]>"  // determines whether the user will be activated or deactivated
}
```

Response (200 OK):
```json
{
  "status": "User <username> <[locked, unlocked]>!"
}
```

### PUT /api/auth/role 

Allowed role: ADMINISTRATOR

Request:
```json
{
   "username": "<String value, not empty>",
   "role": "<[MERCHANT, SUPPORT]>"
}
```

Response (200 OK):
```json
{
   "username": "<String value, not empty>",
   "role": "<String value, not empty>"
}
```

### POST api/antifraud/suspicious-ip

Allowed role: SUPPORT

Request:
```json
{
   "ip": "<String value, not empty>"
}
```

Response (200 OK):
```json
{
   "id": "<Long value, not empty>",
   "ip": "<String value, not empty>"
}
```

### DELETE /api/antifraud/suspicious-ip/{ip}

Allowed role: SUPPORT

Response (200 OK):
```json
{
   "status": "IP <ip address> successfully removed!"
}
```

### GET /api/antifraud/suspicious-ip

Allowed role: SUPPORT

Response (200 OK):
```json
[
  {
    "id": 1,
    "ip": "192.168.1.1"
  },
  ...
  {
    "id": 100,
    "ip": "192.168.1.254"
  }
]
```

### POST /api/antifraud/stolencard

Allowed role: SUPPORT

Request
```json
{
   "number": "<String value, not empty>"
}
```

Response (200 OK):
```json
{
   "id": "<Long value, not empty>",
   "number": "<String value, not empty>"
}
```

### DELETE /api/antifraud/stolencard/{number}

Allowed role: SUPPORT

Response (200 OK):
```json
{
   "status": "Card <number> successfully removed!"
}
```

### GET /api/antifraud/stolencard

Allowed role: SUPPORT

Response (200 OK):
```json
[
    {
        "id": 1,
        "number": "4000008449433403"
    },
     ...
    {
        "id": 100,
        "number": "4000009455296122"
    }
]
```

### GET /api/antifraud/history

Allowed user: SUPPORT

Response (200 OK):
```json
[
    {
      "transactionId": <Long>,
      "amount": <Long>,
      "ip": "<String value, not empty>",
      "number": "<String value, not empty>",
      "region": "<String value, not empty>",
      "date": "yyyy-MM-ddTHH:mm:ss",
      "result": "<String>",
      "feedback": "<String>"
    },
     ...
    {
      "transactionId": <Long>,
      "amount": <Long>,
      "ip": "<String value, not empty>",
      "number": "<String value, not empty>",
      "region": "<String value, not empty>",
      "date": "yyyy-MM-ddTHH:mm:ss",
      "result": "<String>",
      "feedback": "<String>"
    }
]
```

### GET /api/antifraud/history/{number}

Allowed user: SUPPORT

Response (200 OK):
```json
[
    {
      "transactionId": <Long>,
      "amount": <Long>,
      "ip": "<String value, not empty>",
      "number": number,
      "region": "<String value, not empty>",
      "date": "yyyy-MM-ddTHH:mm:ss",
      "result": "<String>",
      "feedback": "<String>"
    },
     ...
    {
      "transactionId": <Long>,
      "amount": <Long>,
      "ip": "<String value, not empty>",
      "number": number,
      "region": "<String value, not empty>",
      "date": "yyyy-MM-ddTHH:mm:ss",
      "result": "<String>",
      "feedback": "<String>"
    }
]
```

### PUT /api/antifraud/transaction 

Allowed user: SUPPORT

The mechanism for checking transactions (i.e. <allowed-threshold> and <manuel-processing-threshold>) is adjusted based on a feedback system. Feedback will be carried out manually by a SUPPORT specialist for completed transactions. Based on the feedback results (ALLOWED, MANUAL_PROCESSING or PROHIBITED), the limits of fraud detection algorithms will increase or decrease for future transactions and following special rules. Only one feedback is allowed per transaction.

| Transaction validity / Feedback | ALLOWED                        | MANUAL_PROCESSING              | PROHIBITED                     |
|---------------------------------|--------------------------------|--------------------------------|--------------------------------|
| ALLOWED                         | exception                      | decrease <allowed-threshold>   | decrease <allowed-threshold>   |
|                                 |                                |                                | decrease <manual-threshold>    |
| MANUAL_PROCESSING               | increase <allowed-threshold>   | exception                      | decrease <manual-threshold>    |
| PROHIBITED                      | increase <allowed-threshold>   |                                |                                |
|                                 | increase <manual-threshold>    | increase <manual-threshold>    | exception                      |

Request:
```json
{
   "transactionId": <Long>,
   "feedback": "<ALLOWED, MANUAL_PROCESSING, PROHIBITED>"
}
```

Response (200 OK):
```json
{
  "transactionId": <Long>,
  "amount": <Long>,
  "ip": "<String value, not empty>",
  "number": "<String value, not empty>",
  "region": "<String value, not empty>",
  "date": "yyyy-MM-ddTHH:mm:ss",
  "result": "<String>",
  "feedback": "<String>"
}
```
