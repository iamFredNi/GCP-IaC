export const fetchCurrencyApproximated = async (db, currency) => {
	try {
		const currencyPromise = await db
			.collection("dataGraph")
			.doc(currency)
			.collection("transactions")
			.get();
		const currencyData = currencyPromise.docs.map((doc) => ({
			id: doc.id,
			...doc.data(),
		}));

		return currencyData;
	} catch (error) {
		console.error("Error fetching currencies:", error);
		throw error;
	}
};
