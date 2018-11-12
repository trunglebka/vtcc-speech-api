#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import logging
import os.path
import sys
import time
from pathlib import Path
from threading import Thread

import websocket

if sys.version_info[0] < 3:
    raise Exception("Must be using Python 3.xx")
root = logging.getLogger()
root.setLevel(logging.DEBUG)

handler = logging.StreamHandler(sys.stdout)
handler.setLevel(logging.DEBUG)
formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
handler.setFormatter(formatter)
root.addHandler(handler)

dirname = os.path.dirname(__file__)
audio_path = os.path.join(Path(dirname).parent, 'test_audio.wav')
cert_path = os.path.join(Path(dirname).parent.parent, 'vtcc-cert/wwwvtccai.crt')


def rate_limited(max_per_second):
    min_interval = 1.0 / float(max_per_second)

    def decorate(func):
        last_time_called = [0.0]

        def rate_limited_function(*args, **kargs):
            elapsed = time.clock() - last_time_called[0]
            left_to_wait = min_interval - elapsed
            if left_to_wait > 0:
                time.sleep(left_to_wait)
            ret = func(*args, **kargs)
            last_time_called[0] = time.clock()
            return ret

        return rate_limited_function

    return decorate


@rate_limited(4)
def send_data(ws, data):
    ws.send(data)


def on_message(ws, message):
    print(message)


def on_error(ws, error):
    logging.warn(error)


def on_close(ws):
    logging.info("### closed ###")
    ws.close()


class AsrWebSocket:
    def __init__(self, sample_rate=16000, audio_format='S16LE', channels=1, token='anonymous',
                 url='wss://vtcc.ai/voice/api/asr/v1/ws/decode_online'):
        self.sample_rate = sample_rate
        self.format = audio_format
        self.channels = channels
        self.token = token
        self.url = url
        self.audio_stream = None
        self.ws = None
        self.sslopt = {
            'ca_certs': cert_path
        }

    def generate_url_query(self):
        query_url = self.url + '?content-type=audio/x-raw,+layout=(string)interleaved,+rate=(int)' + str(
            self.sample_rate)
        query_url += ',+format=(string)' + self.format + ',+channels=(int)' + str(self.channels) + 'token=' + self.token
        return query_url

    def on_open(self):
        def run():
            for block in iter(lambda: self.audio_stream.read(self.sample_rate // 4), b''):
                send_data(self.ws, block)
            self.ws.send('EOS')

        logging.info('Upload thread terminated')
        my_thread = Thread(target=run)
        my_thread.start()

    def recognize(self, audio_steam):
        self.audio_stream = audio_steam
        self.ws = websocket.WebSocketApp(self.generate_url_query(),
                                         on_message=on_message,
                                         on_error=on_error,
                                         on_close=on_close,
                                         on_open=self.on_open)
        self.ws.run_forever(sslopt=self.sslopt)


ws_asr = AsrWebSocket()
ws_asr.recognize(open(audio_path, 'rb'))
