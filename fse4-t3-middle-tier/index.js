const express = require('express');
const cors = require('cors');
const session = require('express-session');
const cookieParser = require('cookie-parser');
const helmet = require('helmet');
const morgan = require('morgan');
const rateLimit = require('express-rate-limit');
const axios = require('axios');

const {
  PORT,
  SESSION_SECRET,
  ANGULAR_APP_URL,
  NODE_ENV,
  TOKEN_EXPIRY,
  API_KEY,
  JAVA_API_URL
} = require('./config/config');

const authRoutes = require('./routes/authRoutes');
const authenticateToken = require('./middleware/authenticateToken');

const app = express();

// ✅ Helmet for security
// app.use(helmet({
//   contentSecurityPolicy: false,
//   crossOriginEmbedderPolicy: false
// }));

// ✅ Body parsers
app.use(express.json({ limit: '100kb' }));
app.use(express.urlencoded({ extended: true, limit: '100kb' }));
console.log("Initialized");

// ✅ Cookie & Session
app.use(cookieParser());
app.use(session({  
  secret: SESSION_SECRET,
  name: 'secure_session',
  resave: false,
  saveUninitialized: false,
  cookie: {
    httpOnly: true,
    secure: NODE_ENV === 'production',
    maxAge: TOKEN_EXPIRY,
    sameSite: 'lax'
  }
}));


app.use(cors({
  origin: true,
  credentials: true
}));

// ✅ Rate limiting
// app.use('/api/', rateLimit({
//   windowMs: 15 * 60 * 1000,
//   max: 100
// }));

// ✅ Logging
app.use(morgan('dev'));

// ✅ Routes
app.use('/api', authRoutes);

// ✅ Proxy other /api requests to Java backend
app.use('/api', async (req, res) => {
  try {
    const headers = {
      'X-API-KEY': API_KEY
    };
    if (req.user) {
      headers['X-User-ID'] = req.user.id;
      headers['X-User-Roles'] = req.user.roles?.join(',') || '';
    }

    const response = await axios({
      method: req.method,
      url: `${JAVA_API_URL}${req.url}`,
      data: req.body,
      params: req.query,
      headers
    });

    res.status(response.status).json(response.data);
  } catch (error) {
    console.error('Proxy error:', error.message);
    res.status(error.response?.status || 500).json({
      success: false,
      message: error.response?.data?.message || 'Server error',
      backendError: error.response?.data || null
    });
  }
});

// ✅ Health check
app.get('/health', (req, res) => {
  res.status(200).json({ status: 'ok' });
});

// ✅ Error handler
app.use((err, req, res, next) => {
  console.error('Unhandled error:', err.stack);
  res.status(500).json({ success: false, message: 'Internal server error' });
});

// ✅ Start server
const server = app.listen(PORT, () => {
  console.log(`Server running on port ${PORT} in ${NODE_ENV || 'development'} mode`);
});
