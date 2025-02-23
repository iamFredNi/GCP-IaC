import { initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import getFirebaseConfig from "./fetchFirebaseConfig.mjs";

export default async function init() {
	try {
		const firebaseConfig = await getFirebaseConfig();
		const app = initializeApp(firebaseConfig);
		const db = getFirestore(app);
		return db;
	} catch (error) {
		console.error("Failed to initialize Firebase:", error);
	}
	return null;
}


