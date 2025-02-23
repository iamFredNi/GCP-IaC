import { SecretManagerServiceClient } from '@google-cloud/secret-manager';

export default async function getFirebaseConfig() {
  const client = new SecretManagerServiceClient();

  // Nom complet du secret
  const secretName = 'projects/mmsdtd/secrets/firebase-config/versions/latest';

  try {
    // Accéder au secret
    const [accessResponse] = await client.accessSecretVersion({ name: secretName });

    // Extraire les données en tant que JSON
    const config = JSON.parse(accessResponse.payload.data.toString());
    return config;
  } catch (error) {
    console.error('Error accessing Firebase config:', error);
    throw new Error('Failed to fetch Firebase configuration');
  }
}

