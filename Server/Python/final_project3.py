from flask import Flask, request, jsonify
from flask_socketio import SocketIO, emit, join_room, leave_room
import mysql.connector

app = Flask(__name__)
app.config['SECRET_KEY'] = 'iems5722'
socketio = SocketIO(app)


@socketio.on('connect')
def connect_handler():
    print('Client Connected')



@socketio.on('disconnect')
def disconnect_handler():
    print("Client Disconnected")

@socketio.on('join')
def on_join(data):
    chatroom = data['chatroom']
    username = data['username']
    join_room(chatroom)
    emit('join', {'username': username, 'chatroom': chatroom}, room=chatroom, include_self=False)

@socketio.on('leave')
def on_leave(data):
    chatroom = data['chatroom']
    username = data['username']
    leave_room(chatroom)
    emit('leave', {'username':username, 'chatroom': chatroom}, room=chatroom, include_self=False)


@app.route('/update', methods=['POST'])
def update():
    time = request.json.get('time')
    chatroom = request.json.get('chatroom')
    username = request.json.get('username')
    message = request.json.get('message')
    Type = request.json.get('type')
    if time and username and message:
        socketio.emit('update', {'type': Type, 'time': time, 'username': username, 'message': message, 'chatroom': chatroom}, room=chatroom)
    return jsonify(status='OK')

if __name__ == '__main__':
    app.debug = True
    socketio.run(app, host='0.0.0.0', port=8002)

