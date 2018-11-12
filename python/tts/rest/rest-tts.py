#!/usr/bin/env python
# -*- coding: utf-8 -*-
# Import the json library
import json
import requests
import os.path
from pathlib import Path

url = "https://vtcc.ai/voice/api/tts/v1/rest/syn"
data = {"text": "Đây là chương trình demo tổng hợp tiếng nói của trung tâm không gian mạng", "voice": "doanngoclev2", "id": "2", "without_filter": False, "speed": 1.0,
        "tts_return_option": 2, "timeout": 60000, "addition_params": [{ "key": "", "value": ""}]}
headers = {'Content-type': 'application/json', 'token': 'anonymous'}
s = requests.Session()
dirname = os.path.dirname(__file__)
audio_path = os.path.join(Path(dirname).parent, 'test_audio.wav')
cert_path = os.path.join(Path(dirname).parent.parent, 'vtcc-cert/wwwvtccai.crt')
response = requests.post(url, data=json.dumps(data), headers=headers, verify=cert_path)


# Headers is a dictionary
print(response.headers)

# Get status_code.
print(response.status_code)

# Get the response data as a python object.
data = response.content
with open("python-example.wav", "wb") as f:
        f.write(data)
