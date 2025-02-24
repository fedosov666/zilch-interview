# Payment Service

## Overview

The Payment Service simulates payments while also parallelizing verification
checks. After creating a new payment, checks are scheduled. The service executes checks
and updates the payment status (also reflecting the checks' results).

## API Endpoints

### Initialize Payment

**Endpoint:** `POST /api/payments`

**Description:** Initializes a new payment with the provided details.

### Get Payment Details

**Endpoint:** `GET /api/payments/{paymentId}`

**Description:** Retrieves the details of a payment by its ID.

### Traffic

Run the script if you want to generate some traffic
[generate_requests.py](generate_requests.py)
