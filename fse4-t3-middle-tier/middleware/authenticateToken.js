const jwtUtils = require('../utils/jwtUtils');

const publicPaths = ['/signin', '/login', '/auth/signin', '/register', '/health'];

module.exports = (req, res, next) => {
  // Allow all /preferences routes without authentication
  if (req.path.startsWith('/preferences') || req.path.startsWith('/market')) return next();

  // Allow public paths
  if (publicPaths.some(path => req.path.endsWith(path))) return next();

  // Check session-based authentication
  if (req.session?.authenticated && req.session.user) {
    req.user = req.session.user;
    return next();
  }

  // Check JWT token
  const token = jwtUtils.getTokenFromRequest(req);
  if (!token) {
    return res.status(401).json({
      success: false,
      message: 'Access denied. Authentication required.'
    });
  }

  try {
    const verified = jwtUtils.verifyToken(token);
    req.user = jwtUtils.extractUser(verified);

    // Sync session with JWT
    req.session.authenticated = true;
    req.session.user = req.user;
    req.session.token = token;

    next();
  } catch (err) {
    console.error('JWT verification failed:', err.message);
    req.session?.destroy?.();
    res.clearCookie('token');
    res.status(403).json({
      success: false,
      message: 'Invalid or expired token. Please login again.'
    });
  }
};