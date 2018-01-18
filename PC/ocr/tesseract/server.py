# -*- coding=utf-8 -*-
"""
file: service.py
socket service
"""

import socket
import threading
import time
import sys
import logging
import os
import subprocess
import json, re
import base64

SERVER_ADDR = '127.0.0.1'
SERVER_PORT = 50028
SERVER_FOLDER = "/tmp/tesseract/"
SERVER_LOG_FILE = SERVER_FOLDER + "ocr_tesseract.log"
SERVER_IMAGES_FOLDER = SERVER_FOLDER + "images"

BLOCK_SIZE = 512

DELETE_IMAGE = False
DELETE_RESULT_TXT = False

def local_log(msg):
    logging.info(msg)

def checkDirExists(path):
    return os.path.isdir(path)

def initLogging():
  """Init for logging
  """
  if not checkDirExists(SERVER_FOLDER):
      os.makedirs(SERVER_FOLDER)
  logging.basicConfig(
                    level    = logging.DEBUG,
                    format='%(asctime)s-%(levelname)s-%(message)s',
                    datefmt  = '%y-%m-%d %H:%M',
                    filename = SERVER_LOG_FILE,
                    filemode = 'w')
  console = logging.StreamHandler()
  console.setLevel(logging.INFO)
  formatter = logging.Formatter('%(asctime)s-%(levelname)s-%(message)s')
  console.setFormatter(formatter)
  logging.getLogger('').addHandler(console)

s = None

def start_socket_service():
    global s
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        # 防止socket server重启后端口被占用（socket.error: [Errno 98] Address already in use）
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        s.bind((SERVER_ADDR, SERVER_PORT))
        s.listen(10)
    except socket.error as msg:
        local_log("socket.error: %s" % msg)
        sys.exit(1)

    local_log('Waiting connection...')

    while 1:
        conn, addr = s.accept()
        t = threading.Thread(target=handle, args=(conn, addr))
        t.start()

def handle(conn, addr):
    start_time = time.time() * 1000
    try:
        local_log(BLOCK_SIZE)
        local_log('<<<<<< Accept new connection from {0}'.format(addr))
        # image file path
        ifp = SERVER_IMAGES_FOLDER + "/" + str(time.time()) + ".jpg"
        if not checkDirExists(SERVER_IMAGES_FOLDER):
            local_log('/tmp/tesseract/images not exists, create it')
            os.makedirs(SERVER_IMAGES_FOLDER)

        # reveive file size info here
        file_size = int(conn.recv(BLOCK_SIZE))

        conn.send(bytes(1))

        # receive file data here
        with open(ifp, 'w') as f:
            recv_size = 0
            while recv_size < file_size:
                data = conn.recv(BLOCK_SIZE)
                recv_size += len(data)
                f.write(data)
            local_log('recv end')
            f.close()
            local_log('parse start...')

            subprocess.check_output("tesseract {0} {1} -l chi_sim+eng".format(ifp, ifp), shell=True)

            local_log('parse done...')

        with open(ifp+".txt", 'r') as ifpt:
            result = {}
            cache = []
            has_q = False
            while 1:
                line = ifpt.readline().replace(" ", "")
                if not line:
                    print "no line"
                    break
                group = re.match(r'^\s', line)
                if group:
                    print "a bad line"
                    continue

                line = line.replace("\n", "")

                if not has_q:
                    if line.endswith("?") or line.endswith("？"):
                        cache.append(line)
                        print "pttt" + line

                        q = ''.join(cache)
                        result["question"] = q
                        has_q = True
                        cache = []
                    else:
                        cache.append(line)
                else:
                    cache.append(line)
            result["answers"] = cache

            # 防止乱码
            result_json = json.dumps(result, ensure_ascii=False, encoding='UTF-8')
            local_log(result_json)

            # base64 编码
            local_log(result)
            result = base64.b64encode(str(result))
            local_log(result)

            ifpt.close()
            conn.send(result)

        if DELETE_IMAGE:
            os.remove(ifp)
        if DELETE_RESULT_TXT:
            os.remove(ifp+".txt")

    except Exception as e:
        local_log("error happened in handle : %s" % e.message)
    finally:
        total_time = time.time() * 1000 - start_time
        local_log("------------ total time = %dms" % total_time)
        conn.close()

if __name__=="__main__":
    initLogging()
    start_socket_service()