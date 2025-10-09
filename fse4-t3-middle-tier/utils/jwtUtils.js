const jwt = require('jsonwebtoken');
const { JWT_SECRET } = require('../config/config');

exports.generateToken = (client) => {
    return jwt.sign({
        id: client.id,
        clientId: client.clientId,
        roles: client.roles || []
    }, JWT_SECRET, { expiresIn: '1h' });
};

exports.verifyToken = (token) => jwt.verify(token, JWT_SECRET);

exports.getTokenFromRequest = (req) => {
    if (req.headers.authorization?.startsWith('Bearer ')) {
        return req.headers.authorization.split(' ')[1];
    }
    return req.cookies.token;
};

exports.extractUser = (client) => ({
    id: client.id,
    clientId: client.clientId,
    roles: client.roles || []
});

exports.cookieOptions = () => ({
    httpOnly: false,
    secure: process.env.NODE_ENV === 'production',
    maxAge: 3600000,
    sameSite: 'strict'
});

