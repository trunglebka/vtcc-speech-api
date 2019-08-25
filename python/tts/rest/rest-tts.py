#!/usr/bin/env python
# -*- coding: utf-8 -*-
# Import the json library
import json
import requests
import os.path
from pathlib import Path

url = "https://vtcc.ai/voice/api/tts/v1/rest/syn"
#edit text field as your need
data = {"text": "Đây là chương trình demo tổng hợp tiếng nói của trung tâm không gian mạng", "voice": "doanngocle", "id": "2", "without_filter": False, "speed": 1.0,
        "tts_return_option": 2}
#add your token to authenticate
headers = {'Content-type': 'application/json', 'token': ''}
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
# generated wav file is ./python-example.wav
with open("python-example.wav", "wb") as f:
        f.write(data)
