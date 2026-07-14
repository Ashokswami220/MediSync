const functions = require("firebase-functions");
const admin = require("firebase-admin");
const cloudinary = require("cloudinary").v2;
require("dotenv").config();

admin.initializeApp();

cloudinary.config({
  cloud_name: process.env.CLOUDINARY_CLOUD_NAME,
  api_key: process.env.CLOUDINARY_API_KEY,
  api_secret: process.env.CLOUDINARY_API_SECRET,
  secure: true,
});

exports.getCloudinarySignature = functions.https.onCall((data, context) => {
  // Ensure the user is authenticated (Optional but recommended for security)
  if (!context.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "User must be logged in to upload files."
    );
  }

  // Generate a UNIX timestamp in seconds
  const timestamp = Math.round(new Date().getTime() / 1000);

  // Any additional parameters you want to strictly enforce on the upload can go here.
  const paramsToSign = {
    timestamp: timestamp,
  };

  try {
    // Generate the signature using the Cloudinary API Secret
    const signature = cloudinary.utils.api_sign_request(
      paramsToSign,
      process.env.CLOUDINARY_API_SECRET
    );

    // Return the signature, timestamp, and API Key to the Android client
    return {
      signature: signature,
      timestamp: timestamp,
      apiKey: process.env.CLOUDINARY_API_KEY,
      cloudName: process.env.CLOUDINARY_CLOUD_NAME,
    };
  } catch (error) {
    console.error("Error generating signature:", error);
    throw new functions.https.HttpsError(
      "internal",
      "Failed to generate upload signature."
    );
  }
});
