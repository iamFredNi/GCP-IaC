import request from "supertest";
import path from "path";
import fs from "fs";
import { exec } from "child_process";
import init from "../srv/FirebaseWrapper.mjs";
// Définir process.env.DIRNAME comme le répertoire courant
process.env.DIRNAME = path.resolve();
let db;
let serverProcess;

function startServer() {
	return new Promise((resolve, reject) => {
		const serverProcess = exec("node server.mjs");

		serverProcess.stdout.on("data", (data) => {
			console.log(`Server output: ${data}`);
			if (data.includes("Server running on port")) {
				resolve(serverProcess);
			}
		});

		serverProcess.stderr.on("data", (data) => {
			console.error(`Server error: ${data}`);
			reject(new Error(data));
		});

		serverProcess.on("close", (code) => {
			console.log(`Server process exited with code ${code}`);
		});

		process.on("exit", () => {
			serverProcess.kill();
		});
	});
}

beforeAll(async () => {
	db = await init();

	// Remplir Firestore avec de fausses données
	await db
		.collection("dailyData")
		.doc("2023-01-01")
		.collection("events")
		.add({
			transactionData: { avg_tone: 10 },
			event: "Event 1",
		});
	await db
		.collection("dailyData")
		.doc("2023-01-01")
		.collection("events")
		.add({
			transactionData: { avg_tone: 20 },
			event: "Event 2",
		});
	await db.collection("dataGraph").doc("USD").collection("transactions").add({
		transaction: "Transaction 1",
	});

	await startServer();
}, 10000);

afterAll(async () => {
	// Nettoyer les données de test
	const dailyDataDocs = await db
		.collection("dailyData")
		.doc("2023-01-01")
		.collection("events")
		.get();
	dailyDataDocs.forEach((doc) => doc.ref.delete());

	const dataGraphDocs = await db
		.collection("dataGraph")
		.doc("USD")
		.collection("transactions")
		.get();
	dataGraphDocs.forEach((doc) => doc.ref.delete());

	if (serverProcess) {
		serverProcess.kill();
	}
});

describe("API Endpoints", () => {
	it("should fetch date indices", async () => {
		const response = await request("http://localhost:5000").get("/api/indices");

		expect(response.status).toBe(200);
		// Ajoutez des assertions pour vérifier les résultats attendus
	});

	it("should fetch events from selected date", async () => {
		const response = await request("http://localhost:5000").get(
			"/api/events/2023-01-01",
		).query({
			cursor: null,
			page_size: 2
		});

		expect(response.status).toBe(200);
		//expect(response.body.events).toHaveLength(2);
		expect(response.body.events[0].event).toBe("Event 2");
		expect(response.body.events[1].event).toBe("Event 1");
	});

	it("should fetch approximated currency data", async () => {
		const response = await request("http://localhost:5000").get(
			"/api/currencies/approximated?currency=USD",
		);

		expect(response.status).toBe(200);
		//expect(response.body).toHaveLength(1);
		expect(response.body[0].transaction).toBe("Transaction 1");
	});
});
