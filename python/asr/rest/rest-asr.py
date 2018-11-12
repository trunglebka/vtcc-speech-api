#!/usr/bin/env python
# -*- coding: utf-8 -*-
# Import the json library
import requests
from pathlib import Path
import os.path

dirname = os.path.dirname(__file__)
url = "https://vtcc.ai/voice/api/asr/v1/rest/decode_file"
headers = {
    'token': 'anonymous',
    #'sample_rate': 16000,
    #'format':'S16LE',
    #'num_of_channels':1,
    #'asr_model': 'mode
    # l code'
}
s = requests.Session()
audio_path = os.path.join(Path(dirname).parent, 'test_audio.wav')
cert_path = os.path.join(Path(dirname).parent.parent, 'vtcc-cert/wwwvtccai.crt')

files = {'file': open(audio_path, 'rb')}
response = requests.post(url,files=files, headers=headers, verify=cert_path, timeout=None)


# header
#print(response.headers)
#print('\n\n')

print(response.text)
