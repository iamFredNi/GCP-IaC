const cursors_refs = {};

export const fetchEventsFromSelectedDate = async (
	db,
	date,
	cursor,
	page_size,
) => {
	try {
		let eventsQuery = db
			.collection("dailyData")
			.doc(date)
			.collection("events")
			.orderBy("transactionData.avg_tone", "desc")
			.limit(parseInt(page_size));

		if (cursor !== null && cursor !== undefined) {
			if (Object.keys(cursors_refs).includes(cursor)) {
				eventsQuery = eventsQuery.startAfter(cursors_refs[cursor]);
			}
		}

		const eventsSnapshot = await eventsQuery.get();
		const lastEvent = eventsSnapshot.docs[eventsSnapshot.docs.length - 1];
		const eventsData = eventsSnapshot.docs.map((eventDoc) => ({
			id: eventDoc.id,
			...eventDoc.data(),
		}));
		if (lastEvent !== undefined) {
			cursors_refs[lastEvent.id] = lastEvent;
		}

		return {
			date,
			lastEventRef: lastEvent.id,
			events: eventsData,
		};
	} catch (error) {
		console.error("Error fetching events:", error);
		throw error;
	}
};
