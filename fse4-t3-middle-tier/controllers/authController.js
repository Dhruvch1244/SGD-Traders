const jwtUtils = require('../utils/jwtUtils');
const axios = require('axios');
const { JAVA_API_URL, API_KEY } = require('../config/config');

exports.signin = async (req, res) => {
  const { email, password } = req.body;

  try {

const response = await axios.post(`http://spring-app:8080/api/auth/signin`, { email, password }, {
      headers: { 'X-API-KEY': API_KEY }
    });

    if (response.data.success) {
      const client = response.data.data.client;
      const token = jwtUtils.generateToken(client);

      res.cookie('token', token, jwtUtils.cookieOptions());
      req.session.authenticated = true;
      req.session.user = jwtUtils.extractUser(client);

      const needsPreferences = response.data.message?.toLowerCase().includes('investment preferences not set');
      res.status(200).json({ ...response.data, needsPreferences });
    } else {
      res.status(401).json({ success: false, message: 'Authentication failed' });
    }
  } catch (error) {
  console.error('🔴 Signin error occurred');

  if (error.response) {
    console.error('🧾 Response error details:');
    console.error('Status:', error.response.status);
    console.error('Status Text:', error.response.statusText);
    console.error('Headers:', error.response.headers);
    console.error('Data:', error.response.data);
  } else if (error.request) {
    console.error('📡 No response received from backend');
    console.error('Request:', error.request);
  } else {
    console.error('⚠️ Error setting up request:', error.message);
  }

  console.error('🛠 Axios config:', error.config);

  res.status(error.response?.status || 500).json({
    success: false,
    message: error.response?.data?.message || 'Server error during authentication',
    debug: {
      status: error.response?.status,
      statusText: error.response?.statusText,
      headers: error.response?.headers,
      data: error.response?.data,
      message: error.message
    }
  });
}
};

exports.register = async (req, res) => {
  try {
    const response = await axios.post(`http:/spring-app:8080/api/auth/register`, req.body, {
      headers: { 'X-API-KEY': API_KEY }
    });

    if (response.data.success) {
      const client = response.data.data.client;
      const token = jwtUtils.generateToken(client);

      res.cookie('token', token, jwtUtils.cookieOptions());
      req.session.authenticated = true;
      req.session.user = jwtUtils.extractUser(client);

      res.status(201).json(response.data);
    } else {
      res.status(400).json(response.data);
    }
  } catch (error) {
    console.error('Register error:', error.response?.data || error.message);
    res.status(error.response?.status || 500).json({
      success: false,
      message: error.response?.data?.message || 'Server error during registration'
    });
  }
};

exports.logout = (req, res) => {
  res.clearCookie('token');
  res.clearCookie('secure_session');
  req.session.destroy(() => {
    res.status(200).json({ success: true });
  });
};
