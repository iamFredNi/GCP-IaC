import { Server } from "socket.io";
import { FieldPath } from "firebase-admin/firestore";

export const initSocket = (server, db, indices, best_avg_tone) => {
	const io = new Server(server, {
		cors: {
			origin: "*",
			methods: ["GET", "POST"],
		},
	});
	var best_events_avg_tone = best_avg_tone;

	io.on("connection", (socket) => {
		console.log("SOCKET - a user connected");

		socket.on("disconnect", () => {
			console.log("SOCKET - user disconnected");
		});

		socket.on("subscribeToRealtime", () => {
			console.log(`SOCKET - User subscribed to realtime currencies`);
			var unsubscribeCurrencies = [];

			const unsubscribeEvents = db
				.collection("dailyData")
				.doc(indices.dateIndex[indices.dateIndex.length - 1])
				.collection("events")
				.orderBy("transactionData.avg_tone", "desc")
				.limit(10)
				.onSnapshot(async (snapshot) => {
					best_events_avg_tone = snapshot.docs.map((doc) => ({
						id: doc.id,
						data: doc.data(),
					}));
					socket.emit("events-update", best_events_avg_tone);
				});

			for (const currency of indices.currencieIndex) {
				socket.on("join-" + currency, () => {
					console.log("SOCKET - User joined currency room :  " + currency);
					socket.join(currency);

					const unsubscribeCurrencie = db
						.collection("dailyData")
						.doc(indices.dateIndex[indices.dateIndex.length - 1])
						.collection("currencies")
						.doc(currency)
						.collection("timestamps")
						.orderBy("transactionData.trade_id", "desc")
						.limit(175)
						.onSnapshot(async (snapshot) => {
							const currency_data = [];
							snapshot.docChanges().forEach((change) => {
								if (change.type === "added") {
									console.log(currency);
									currency_data.push({
										currency,
										data: { id: change.doc.id, data: change.doc.data() },
									});
								}
							});
							io.to(currency).emit("currency-update", currency_data);
						});
				});
				socket.on("quit-" + currency, () => {
					socket.leave(currency);
				});
			}
		});
	});
};
