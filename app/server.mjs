import express from "express";
import cors from "cors";
import http from "http";
import init from "./srv/FirebaseWrapper.mjs";
import { fetchDateIndex } from "./srv/controllers/indexController.mjs";
import { fetchEventsFromSelectedDate } from "./srv/controllers/eventController.mjs";
import { fetchCurrencyApproximated } from "./srv/controllers/currencyController.mjs";
import { fetchDailyData } from "./srv/controllers/dailyDataController.mjs";
import { initSocket } from "./srv/socket.mjs";
import path from "path";
import fs from "fs";

const app = express();
const server = http.createServer(app);

app.use(cors());
app.use(express.json());

const db = await init();

const indices = await fetchDateIndex(db);

initSocket(server, db, indices);

app.get("/api/indices", async (req, res) => {
	try {
		const results = await fetchDateIndex(db);
		res.status(200).json(results);
	} catch (error) {
		res.status(500).json({ error: error.message });
	}
});

app.get("/api/events/:date", async (req, res) => {
	const { date } = req.params;
	const { cursor, page_size } = req.query;

	try {
		const eventsData = await fetchEventsFromSelectedDate(
			db,
			date,
			cursor,
			page_size,
		);
		res.status(200).json(eventsData);
	} catch (error) {
		res.status(500).json({ error: error.message });
	}
});

app.get("/api/currencies/approximated", async (req, res) => {
	try {
		const {currency} = req.query;
		const currencyData = await fetchCurrencyApproximated(
			db,
			currency
		);
		res.status(200).json(
			currencyData
		);
	} catch (error) {
		res.status(500).json({ error: error.message });
	}
});

// Serve the React app
app.use(express.static(path.join(process.env.DIRNAME, "build")));

app.get("/", (req, res) => {
	const filePath = path.join(process.env.DIRNAME, "build", "index.html");
	fs.readFile(filePath, "utf8", (err, htmlData) => {
		if (err) {
			return res.status(500).send("Error reading file");
		}
		// Inject the API base URL into the HTML
		const modifiedHtmlData = htmlData.replace(
			"</head>",
			`<script>window.__API_BASE_URL__ = "${process.env.API_BASE_URL || "http://localhost:5000"}"</script></head>`,
		);
		res.send(modifiedHtmlData);
	});
});

const PORT = process.env.PORT || 5000;
server.listen(PORT, () => {
	console.log(`Server running on port ${PORT}`);
});
