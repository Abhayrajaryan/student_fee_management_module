# Student Fee Management Module

## Tech Stack

- Java 21
- Spring Boot 3.5.7
- MySQL with MySQL Connector/J
- Maven

## Prerequisites

- JDK 21
- Maven
- MySQL Server

## Project Setup and Execution

The configured database name is 'student_fee_management_module'.

Set the local MySQL connection values in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/student_fee_management_module?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

The application setup creates or updates the database tables from the JPA entities on startup.

Run the application:

```bash
mvn spring-boot:run
```

Populate sample schools and students by executing `sample_data.sql` against the configured database:

```bash
mysql -u root -p student_fee_management_module < sample_data.sql
```

The API runs at `http://localhost:8080` by default.

## API Documentation

All endpoints use this response format:


```json
{
  "statusCode": 200,
  "message": "Response message",
  "data": {}
}
```

### Create Fee Record

`POST /fees/create`

Request body:

```json
{
  "studentId": 3,
  "title": "Tuition Fee - November",
  "totalAmount": 15000.00,
  "dueDate": "2026-12-15"
}
```

Success response:

```json
{
  "statusCode": 201,
  "message": "Fee record created successfully",
  "data": {
    "id": 101,
    "studentId": 3,
    "title": "Tuition Fee - November",
    "totalAmount": 15000.00,
    "dueDate": "2026-12-15",
    "fullyPaid": false
  }
}
```

Error status: `400` for invalid fields; `404` when the student is not found.

### Get Student Fee Records

`GET /fees/students/{studentId}`

- Path parameter: `studentId`
- Query parameters: `page` (`0` by default) and `size` (`10` by default, range `1-100`)

Success response:

```json
{
  "statusCode": 200,
  "message": "Student fee details retrieved successfully",
  "data": {
    "content": [
      {
        "feeId": 101,
        "title": "Tuition Fee - November",
        "totalAmount": 15000.00,
        "dueDate": "2026-12-15",
        "fullyPaid": false,
        "totalAmountPaid": 5000.00,
        "outstandingAmount": 10000.00
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

Error status: `400` for invalid pagination; `404` when the student is not found.

### Record Payment

`POST /payments/recordPayment`

Request body:

```json
{
  "feeId": 101,
  "amount": 5000.00,
  "paymentMethod": "UPI",
  "paidAt": "2026-11-20T16:30:00"
}
```

`paymentMethod` may be `CASH`, `UPI`, `CARD`, `BANK_TRANSFER`, or `CHEQUE`.

Success response:

```json
{
  "statusCode": 201,
  "message": "Payment recorded successfully",
  "data": {
    "id": 201,
    "feeRecordId": 101,
    "amount": 5000.00,
    "paymentMethod": "UPI",
    "paidAt": "2026-11-20T16:30:00"
  }
}
```

Error status: `400` for invalid fields or overpayment; `404` when the fee record is not found.

### Get Payment History

`GET /payments/fees/{feeId}`

- Path parameter: `feeId`

Success response:

```json
{
  "statusCode": 200,
  "message": "Payment history retrieved successfully",
  "data": [
    {
      "paymentId": 201,
      "feeId": 101,
      "amount": 5000.00,
      "paymentMethod": "UPI",
      "paidAt": "2026-11-20T16:30:00"
    }
  ]
}
```

Error status: `404` when the fee record is not found.

### Get Students with Pending Fees

`GET /fees/pending`

- Query parameters: `schoolId` (optional), `page` (`0` by default), and `size` (`10` by default, range `1-100`)

Success response:

```json
{
  "statusCode": 200,
  "message": "Pending fee students retrieved successfully",
  "data": {
    "content": [
      {
        "studentId": 3,
        "studentName": "Kabir Singh",
        "schoolId": 1,
        "schoolName": "Greenwood International School",
        "pendingFees": [
          {
            "feeId": 101,
            "feeTitle": "Tuition Fee - November",
            "totalAmount": 15000.00,
            "paidAmount": 5000.00,
            "outstandingAmount": 10000.00,
            "dueDate": "2026-12-15"
          }
        ]
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

Error status: `400` for invalid pagination.

### Get School Fee Summary

`GET /schools-fee-summary/{schoolId}`

- Path parameter: `schoolId`

Success response:

```json
{
  "statusCode": 200,
  "message": "School fee summary retrieved successfully",
  "data": {
    "schoolId": 1,
    "schoolName": "Greenwood International School",
    "totalFees": 15000.00,
    "totalCollected": 5000.00,
    "totalOutstanding": 10000.00,
    "totalFeeRecords": 1,
    "fullyPaidRecords": 0,
    "pendingRecords": 1
  }
}
```

Error status: `404` when the school is not found.

## Postman

Import `student_fee_management_module.postman_collection.json` into Postman. The collection contains requests for all six API endpoints.

## Running Tests

```bash
mvn test
```
