from flask import Flask, request, jsonify
from flask_socketio import SocketIO, emit, join_room
import mysql.connector

app = Flask(__name__)
app.config['SECRET_KEY'] = 'iems5722'
socketio = SocketIO(app)


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

@socketio.on('connect')
def connect_handler():
    print('Client Connected')

# @socketio.on('user_connect')
# def user_connect(username):
#     database = Database()
#     query = 'SELECT name FROM chatrooms WHERE username = "' + str(username) + '"'
#     database.cursor.execute(query)
#     result = database.cursor.fetchall()
#     if result:
#         emit('connect_result', {'result':result})
#     print(result)

@socketio.on('connectToBroadcast')
def connect_Broadcast(name):
    join_room(name)
    print(name)

@app.route('/broadcast', methods=['POST'])
def broadcast_room():
    chatroom = request.json.get('chatroom')
    username = request.json.get('username')
    message = request.json.get('message')
    Type = request.json.get('type')
    print(chatroom,username,message,Type)
    if chatroom and username and message:
        socketio.emit('new message', {'type': Type, 'chatroom': chatroom, 'username': username, 'message': message}, room=chatroom)
    return jsonify(status='OK')

@socketio.on('create')
def create_handler(data):
    username = data['username']
    name = data['name']
    password = data['password']

    database = Database()
    query1 = 'SELECT * FROM chatrooms WHERE name = "' + str(name) + '"'
    query2 = 'INSERT INTO chatrooms (name, username, password) VALUES ("' + str(name) + '", "'\
            + str(username) + '", "' + str(password) + '")'
    database.cursor.execute(query1)
    result = database.cursor.fetchall()
    if result:
        emit('create', {'result': 'Chatroom Already Exists'})
        print('Chatroom Already Exists')
    else:
        database.cursor.execute(query2)
        database.connection.commit()
        emit('create', {'result': 'Create Success', 'name': name})
        print('Create Success')


@socketio.on('join')
def join_handler(data):
    username = data['username']
    name = data['name']
    password = data['password']

    database = Database()
    query1 = 'SELECT password FROM chatrooms WHERE name = "' + str(name) \
            + '" LIMIT 0, 1'
    query2 = 'INSERT INTO chatrooms (name, username, password) VALUES ("' + str(name) + '", "'\
            + str(username) + '", "' + str(password) + '")'
    database.cursor.execute(query1)
    tmp = database.cursor.fetchall()
    if tmp:
        result = tmp[0]['password']
        if password == result:
            database.cursor.execute(query2)
            database.connection.commit()
            emit('join', {'result': 'Join Success', 'name': name})
            print('Join Success')
        else:
            emit('join', {'result': 'Password Wrong'})
            print('Password Wrong')
    else:
        emit('join', {'result': 'Chatroom Does Not Exist'})
        print('Chatroom Does Not Exist')





@socketio.on('disconnect')
def disconnect_handler():
    print("Client Disconnected")


if __name__ == '__main__':
    app.debug = True
    socketio.run(app, host='0.0.0.0', port=8001)