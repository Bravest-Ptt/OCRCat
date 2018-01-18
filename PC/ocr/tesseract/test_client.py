# -*- coding=utf-8 -*-
"""
socket client
"""

import socket
import sys
import os
import base64
import json

SERVER_ADDR = '127.0.0.1'
SERVER_PORT = 50028
BLOCK_SIZE = 512

def send(img_path):
    sc = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_address = (SERVER_ADDR, SERVER_PORT)

    try:
        print "Start connect"
        sc.connect(server_address)

        file_size = os.path.getsize(img_path)
        print "file_size = %d " % file_size
        sc.send(bytes(file_size))

        response = sc.recv(BLOCK_SIZE)
        if len(response) != 0 and int(response) == 1:
            print "server receive file_size"
        else:
            print "server not"
            return
        sent_size = 0

        with open(img_path) as f:
            while sent_size < file_size:
                data = f.read(BLOCK_SIZE)
                sent_size += len(data)
                sc.send(data)

        result = sc.recv(4096)

        print str(result)
        result = base64.b64decode(str(result))
        print result
        result = eval(result)
        print json.dumps(result, ensure_ascii=False, encoding='utf-8')
    except socket.error as msg:
        print "socket error: %s" % msg
    finally:
        sc.close()
        print "Closing connect"

if __name__=="__main__":
    send('/home/pt/Pictures/22222.jpg')