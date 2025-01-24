#!/bin/bash

echo "Starting MongoDB..."
echo stud | sudo -S mongod --fork --logpath /var/log/mongodb.log --bind_ip_all

echo "Starting MySQL..."
echo stud | sudo -S service mysql start
sleep 5

echo "Configuring MySQL..."
echo stud | sudo -S mysql -u root << EOF
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root';
FLUSH PRIVILEGES;
CREATE DATABASE IF NOT EXISTS imagedb;
SET GLOBAL max_allowed_packet=1073741824;
EOF

echo "Starting Node.js application..."
cd /home/stud/app
npm start