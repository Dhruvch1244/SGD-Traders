const crypto = require('crypto');
require('dotenv').config();

module.exports = {
    PORT: process.env.PORT || 3002,
    JWT_SECRET: process.env.JWT_SECRET || crypto.randomBytes(64).toString('hex'),
    TOKEN_EXPIRY: parseInt(process.env.TOKEN_EXPIRY || '3600000'),
    ANGULAR_APP_URL: process.env.ANGULAR_APP_URL || 'http://localhost:4200',
    JAVA_API_URL: process.env.JAVA_API_URL || 'http://spring-app:8080/api',
    API_KEY: process.env.API_KEY || 'change-this-in-production',
    NODE_ENV: process.env.NODE_ENV || 'development',
    SESSION_SECRET: process.env.SESSION_SECRET || crypto.randomBytes(64).toString('hex')
};

