import requests
import json

with open('test-upload.json', 'r') as f:
    data = json.load(f)

response = requests.post('http://localhost:8080/upload/text',
                       json=data,
                       headers={'Content-Type': 'application/json'})

print(f"Status Code: {response.status_code}")
print(f"Response: {response.text}")