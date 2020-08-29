from flask import Flask
from flask_socketio import SocketIO, emit
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


@socketio.on('register')
def register_handler(data):
    username = data['username']
    password = data['password']

    database = Database()
    query1 = 'SELECT * FROM users WHERE username = "' + str(username) + '"'
    query2 = 'INSERT INTO users (username, password) VALUES ("' \
            + str(username) + '", "' + str(password) + '")'
    database.cursor.execute(query1)
    result = database.cursor.fetchall()

    if result:
        emit('registerResult', {'result': 'User Already Exists'})
        print('User Already Exists')
    else:
        database.cursor.execute(query2)
        database.connection.commit()
        emit('registerResult', {'result': 'Register Success'})
        print('Register Success')



@socketio.on('connect')
def connect_handler():
    print('Client Connected')

@socketio.on('disconnect')
def disconnect_handler():
    print('Client Disconnected')


@socketio.on('login')
def login_handler(data):
    username = data['username']
    password = data['password']

    database = Database()
    query = 'SELECT password FROM users WHERE username = "' + str(username) + '"'
    database.cursor.execute(query)
    tmp = database.cursor.fetchall()
    print(tmp)
    if tmp:
        result = tmp[0]['password']
        if password == result:
            emit('login', {'username': username, 'result': 'Login Success'})
            print('Login Success')
        else:
            emit('login', {'username': username, 'result': 'Password Wrong'})
            print('Password Wrong')
    else:
        emit('login', {'result': 'Username Does not Exist'})
        print('Username Does Not Exist')


if __name__ == '__main__':
    app.debug = True
    socketio.run(app, host='0.0.0.0', port=8000)
