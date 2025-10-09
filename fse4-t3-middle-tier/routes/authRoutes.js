const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');

router.post('/auth/signin', authController.signin);
router.post('/auth/register', authController.register);
router.post('/auth/logout', authController.logout);

module.exports = router;
