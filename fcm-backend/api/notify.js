const admin = require("firebase-admin");

// Initialize Firebase using environment variables for security
if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.cert({
      projectId: process.env.FIREBASE_PROJECT_ID,
      clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
      // The replace() function ensures private key formatting doesn't break
      privateKey: (process.env.FIREBASE_PRIVATE_KEY || "").replace(/\\n/g, '\n'),
    }),
  });
}

export default async function handler(req, res) {
  // Only allow POST requests
  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method not allowed' });
  }

  // Extract the target device token and message data from the Android request
  const { fcmToken, title, message } = req.body;

  if (!fcmToken) {
    return res.status(400).json({ error: 'FCM Token is required' });
  }

  try {
    const payload = {
      token: fcmToken,
      notification: {
        title: title || "New File Uploaded",
        body: message || "An admin has uploaded a file for you."
      }
    };

    // Send the notification to the specific device
    const response = await admin.messaging().send(payload);
    return res.status(200).json({ success: true, response });
  } catch (error) {
    console.error("Error sending notification:", error);
    return res.status(500).json({ error: error.message });
  }
}
