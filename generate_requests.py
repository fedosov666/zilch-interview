import requests
import multiprocessing
import random
import string

def generate_random_string(length=10):
    return ''.join(random.choices(string.ascii_letters + string.digits, k=length))

def send_request(_):
    url = "http://localhost:8080/api/payments"
    payload = {
        "amount": random.randint(1, 100),
        "currency": random.choice(["USD", "EUR", "GBP"]),
        "paymentMethod": random.choice(["PAY_NOW", "PAY_OVER_3_MONTHS", "PAY_OVER_6_MONTHS"]),
        "merchant": generate_random_string()
    }
    headers = {"Content-Type": "application/json"}
    try:
        response = requests.post(url, json=payload, headers=headers)
        if response.status_code == 201:
            print(f"ID: {response.json().get('id')}")
        else:
            print(f"Response Code: {response.status_code}")
    except requests.RequestException as e:
        print(f"Request failed: {e}")

if __name__ == "__main__":
    num_requests = 500
    num_workers = multiprocessing.cpu_count() * 4
    with multiprocessing.Pool(num_workers) as pool:
        pool.map(send_request, range(num_requests))
