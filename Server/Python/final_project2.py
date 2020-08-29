from flask import Flask, jsonify, request
import mysql.connector
import math
import requests

api = Flask(__name__)


class Database:
    connection = None
    cursor = None

    def __init__(self):
        self.connect()
        return

    def connect(self):
        self.connection = mysql.connector.connect(
            host='localhost',
            user='PeiXingting',
            password='19980106',
            database='iems5722',
        )
        self.cursor = self.connection.cursor(dictionary=True)
        return


@api.route('/api/get_chatrooms', methods=['GET'])
def get_chatrooms():
    username = request.args.get('username')
    database = Database()
    query = 'SELECT name FROM chatrooms WHERE username = "' + str(username) + '"'
    database.cursor.execute(query)
    result = database.cursor.fetchall()
    if result:
        return jsonify(data=result, status='OK')
    else:
        return jsonify(data='No Data', status='ERROR')
    print(result)


@api.route('/api/get_messages', methods=['GET'])
def get_messages():
    chatroom = request.args.get('chatroom_name')
    page = request.args.get('page', type=int)
    database = Database()
    count_query = 'SELECT COUNT(id) FROM messages WHERE chatroom = "' + str(chatroom) + '"'
    database.cursor.execute(count_query)
    count = database.cursor.fetchall()[0]['COUNT(id)']
    total_pages = math.ceil(count / 10.0)
    if page <= total_pages:
        query = 'SELECT type, username, message, time FROM messages WHERE chatroom = "' \
                + str(chatroom) + '" ORDER BY id DESC LIMIT ' + str((page - 1) * 10) + ', 10'
        database.cursor.execute(query)
        messages = database.cursor.fetchall()
        for i in messages:
            time = i['time'].strftime('%m-%d %H:%M')
            i['time'] = str(time)
        data = {'current_page': page, 'messages': messages, 'total_pages': total_pages}
        # print(data)
    else:
        return jsonify(data='No Page', status='ERROR')
    if data:
        return jsonify(data=data, status='OK')
    else:
        return jsonify(data="No Data", status='ERROR')


@api.route('/api/send_message', methods=['POST'])
def send_message():
    Type = request.form.get('type')
    chatroom = request.form.get('chatroom')
    username = request.form.get('username')
    message = request.form.get('message')
    time = request.form.get('time')

    if Type and chatroom and username and message:
        tmp = str(message).replace('!$', '&')
        database = Database()
        query = 'INSERT INTO messages (type, chatroom, username, message) VALUES (' \
                + str(Type) + ', "' + str(chatroom) + '", "' + str(username) + '", "' + tmp + '")'
        print(query)

        database.cursor.execute(query)
        database.connection.commit()

        url1 = 'http://34.229.144.199/broadcast'
        url2 = 'http://34.229.144.199/update'
        headers = {'Content-Type': 'application/json'}
        payload = {
            'type':int(Type),
            'chatroom': str(chatroom),
            'username': str(username),
            'message': tmp,
            'time': time,
        }
        r1 = requests.post(url1, headers=headers, json=payload)
        r2 = requests.post(url2, headers=headers, json=payload)
        return jsonify(status='OK')
    else:
        return jsonify(status='ERROR')


if __name__ == '__main__':
    api.debug = True
    api.run(host='0.0.0.0', port=8003)
