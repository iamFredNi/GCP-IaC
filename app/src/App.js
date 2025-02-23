import React, { useState, useEffect } from "react";
import { CircularProgress, Tabs, Tab } from "@mui/material";
import axiosInstance from "./axiosConfig";
import { socket } from "./socketInstance";
import RealtimeDataChart from "./RealtimeChart";
import CurrencySelector from "./CurrencySelector";
import CurrencyChart from "./CurrencyChart";
import EventsList from "./EventsList";
import { EventGrid } from "./EventGrid";
import "./App.css";

const App = () => {
	const first_pagination_size = 10;
	const scroll_infinite_size = 10;
	const currency_realtime_data_chunk_size = 200;
	const [indices, setIndices] = useState({ dateIndex: [], currencyIndex: [] });
	const [dataByDate, setDataByDate] = useState({});

	const [realtimeCurrencyData, setRealtimeCurrencyData] = useState({});
	const [realtimeCurrencyDataSet, setRealtimeCurrencyDataSet] = useState({});
	const [realtimeTopEventsData, setRealtimeTopEventsData] = useState([]);
	const [realtimeTopEventsDataSet, setRealtimeTopEventsDataSet] = useState({
		set: new Set(),
		data: [],
	});

	const [error, setError] = useState(null);
	const [table, setTable] = useState([]);
	const [selectedCurrency, setSelectedCurrency] = useState("BTC-EUR");
	const [selectedEvent, setSelectedEvent] = useState(null);
	const [filteredDate, setFilteredDate] = useState(null);
	const [pageState, setPageState] = useState("loading");
	const [firstLoading, setFirstLoading] = useState(true);
	const [selectedDate, setSelectedDate] = useState(null);
	const [hasMore, setHasMore] = useState(true);
	const [lastEvent, setLastEvent] = useState(null);
	const [selectedTab, setSelectedTab] = useState(0);

	// Gérer le changement d'onglet
	const handleTabChange = (event, newValue) => {
		setSelectedTab(newValue);
	};

	const handleDateChange = (date) => {
		setSelectedDate(date);
		setHasMore(true);
		setPageState("query-data-date");
	};

	const handleCurrencyChange = (currency) => {
		console.log(currency);
		socket.emit("quit-" + selectedCurrency);
		setSelectedCurrency(currency);
		socket.emit("join-" + currency);
		setPageState("query-data-date");
	};

	const isDateAvailable = (date) => {
		const formattedDate = formatDate(date);
		return indices.dateIndex.includes(formattedDate);
	};

	const loadMoreEvents = async () => {
		const newEvents = await fetchEventsFromSelectedDate(
			lastEvent,
			scroll_infinite_size,
		);

		setDataByDate((previous) => ({
			...previous,
			[selectedDate]: {
				...previous[selectedDate],
				events: [...previous[selectedDate].events, ...newEvents],
			},
		}));

		if (newEvents.length < scroll_infinite_size) {
			setHasMore(false);
		}
	};

	const formatDate = (date) => {
		if (!date) return "";
		const year = date.getFullYear();
		const month = String(date.getMonth() + 1).padStart(2, "0");
		const day = String(date.getDate()).padStart(2, "0");
		return `${year}-${month}-${day}`;
	};

	const fetchEventsFromSelectedDate = async (
		cursor,
		page_size,
		date = selectedDate,
	) => {
		try {
			const response = await axiosInstance.get(`/api/events/${date}`, {
				params: { cursor, page_size },
			});
			const { events, lastEventRef } = response.data;
			setLastEvent(lastEventRef);
			setDataByDate({
				...dataByDate,
				[date]: {
					...dataByDate[date],
					lastEventRef: lastEventRef,
				},
			});
			console.log(dataByDate, lastEventRef, cursor);
			return events;
		} catch (error) {
			console.error("Error fetching events:", error);
			return [];
		}
	};

	const fetchCurrency = async (currency = selectedCurrency) => {
		try {
			const response = await axiosInstance.get(`/api/currencies/approximated`, {
				params: { currency },
			});
			return currencyFromSnapshot(response.data, currency);
		} catch (error) {
			console.error("Error fetching currencies:", error);
			return [];
		}
	};

	const fetchDateIndex = async () => {
		try {
			const response = await axiosInstance.get("/api/indices");
			setIndices({
				dateIndex: response.data.dateIndex,
				currencyIndex: response.data.currencieIndex,
			});
			if (firstLoading) {
				setFirstLoading(false);
				setPageState("query-first-data-date");
			}
		} catch (error) {
			console.error("Error fetching indices:", error);
		}
	};

	const currencyFromSnapshot = (currencyData, currency) => {
		if (currencyData === undefined) return [];
		var tmp = [];
		currencyData.forEach((data) => {
			tmp.push({
				timestamp: data.id,
				price: data.transactionData.price,
				currency: currency,
			});
		});
		return tmp;
	};

	const fetchDataByDate = async (
		date,
		pagination_size,
		last_event = lastEvent,
	) => {
		try {
			if (
				dataByDate[date] !== undefined &&
				Object.keys(dataByDate[date].currencies).includes(selectedCurrency) &&
				dataByDate[date].currencies[selectedCurrency].length > 0 &&
				dataByDate[date].events.length > 0
			) {
				setTable((prevTable) => {
					return dataByDate[date].currencies[selectedCurrency].sort(
						(a, b) => new Date(a.timestamp) - new Date(b.timestamp),
					);
				});
				return;
			} else if (
				dataByDate[date] !== undefined &&
				Object.keys(dataByDate[date].currencies).includes(selectedCurrency) &&
				dataByDate[date].currencies[selectedCurrency].length > 0
			) {
				//setPageState("loading");
				const events = await fetchEventsFromSelectedDate(
					last_event,
					pagination_size,
				);

				setTable((prevTable) => {
					return dataByDate[date].currencies[selectedCurrency].sort(
						(a, b) => new Date(a.timestamp) - new Date(b.timestamp),
					);
				});
				setDataByDate((previous) => ({
					...previous,
					[date]: {
						...previous[date],
						events,
					},
				}));
				return;
			} else if (
				dataByDate[date] !== undefined &&
				dataByDate[date].events.length > 0
			) {
				//setPageState("loading");
				const currencySnapshot = await fetchCurrency();

				setTable((prevTable) => {
					return currencySnapshot.sort(
						(a, b) => new Date(a.timestamp) - new Date(b.timestamp),
					);
				});
				setDataByDate((previous) => ({
					...previous,
					[date]: {
						...previous[date],
						currencies: {
							...previous[date],
							[selectedCurrency]: currencySnapshot,
						},
					},
				}));
				return;
			} else {
				setPageState("loading");
				const eventsPromise = fetchEventsFromSelectedDate(
					last_event,
					pagination_size,
					date,
				);
				const currencyPromise = fetchCurrency();

				const [eventsSnapshot, currencySnapshot] = await Promise.all([
					eventsPromise,
					currencyPromise,
				]);

				if (eventsSnapshot.length === 0) {
					setHasMore(false);
				}
				setTable((prevTable) => {
					return currencySnapshot.sort(
						(a, b) => new Date(a.timestamp) - new Date(b.timestamp),
					);
				});
				setDataByDate((previous) => ({
					...previous,
					[date]: {
						...previous[date],
						events: eventsSnapshot,
						currencies: {
							...previous[date],
							[selectedCurrency]: currencySnapshot,
						},
					},
				}));
			}
		} catch (err) {
			setError("Error fetching data for the given date");
			console.log(err);
		} finally {
			setPageState("ready");
		}
	};

	const onTopEventsUpdate = (topEvents) => {
		topEvents.forEach((event) => {
			if (!realtimeTopEventsDataSet.set.has(event.id)) {
				realtimeTopEventsDataSet.set.add(event.id);
				realtimeTopEventsDataSet.data.push(event);
			}
		});

		if (!Object.keys(realtimeTopEventsDataSet).includes("data")) return;
		console.log(realtimeTopEventsDataSet);

		const orderedSet = realtimeTopEventsDataSet.data
			.sort(
				(a, b) =>
					new Date(a.data.transactionData.avg_tone) -
					new Date(b.data.transactionData.avg_tone),
			)
			.slice(0, 10);

		const newIndices = orderedSet.map((data) => data.id);

		setRealtimeTopEventsDataSet((previous) => ({
			set: new Set(newIndices),
			data: orderedSet,
		}));

		setRealtimeTopEventsData((previous) => orderedSet);
	};

	const handleRealtimeDataUpdate = (currency) => {
		if (
			Object.keys(realtimeCurrencyDataSet).length === 0 ||
			!Object.keys(realtimeCurrencyDataSet).includes(currency)
		) {
			return;
		}
		console.log(realtimeCurrencyDataSet, currency);
		var orderedSet = realtimeCurrencyDataSet[currency].data.sort(
			(a, b) => new Date(a.timestamp) - new Date(b.timestamp),
		);

		if (orderedSet.length > currency_realtime_data_chunk_size)
			orderedSet = orderedSet.slice(
				orderedSet.length - currency_realtime_data_chunk_size,
				orderedSet.length,
			);

		const newIndices = orderedSet.map((data) => data.timestamp);

		setRealtimeCurrencyDataSet((previous) => ({
			...previous,
			[currency]: { set: new Set(newIndices), data: orderedSet },
		}));

		setRealtimeCurrencyData((previous) => ({
			...previous,
			[currency]: orderedSet,
		}));
	};

	const onCurrencyUpdate = (currencyData) => {
		if (currencyData.length === 0) return;
		var currency = currencyData[0].currency;
		const dataProcessed = [];
		currencyData.forEach((data) => {
			if (data.data.transactionData === null) return;
			dataProcessed.push({
				timestamp: data.data.id,
				price: data.data.data.transactionData.price,
			});
		});
		console.log(dataProcessed, selectedCurrency);

		if (Object.keys(realtimeCurrencyDataSet).includes(currency)) {
			dataProcessed.forEach((element) => {
				if (realtimeCurrencyDataSet[currency].set.has(element.timestamp))
					return;
				realtimeCurrencyDataSet[currency].set.add(element.timestamp);
				realtimeCurrencyDataSet[currency].data.push(element);
			});
		} else {
			realtimeCurrencyDataSet[currency] = {
				set: new Set(dataProcessed),
				data: dataProcessed,
			};
		}
		handleRealtimeDataUpdate(currency);
	};
	useEffect(() => {
		if (!socket.connected) {
			socket.connect();
		}
		return () => {
			socket.disconnect();
		};
	}, []);

	useEffect(() => {
		socket.on("currency-update", onCurrencyUpdate);
		socket.on("events-update", onTopEventsUpdate);
		return () => {
			socket.off("currency-update", onCurrencyUpdate);
			socket.off("events-update", onTopEventsUpdate);
		};
	}, [realtimeCurrencyData, realtimeTopEventsData]);

	useEffect(() => {
		const loadData = async () => {
			await fetchDateIndex();
		};

		socket.on("connect", () => {
			console.log("Socket connected");
		});
		socket.on("disconnect", () => {
			console.log("Socket disconnected");
		});
		socket.emit("subscribeToRealtime");

		socket.emit("join-" + selectedCurrency);

		loadData();

		return () => {
			socket.off("connect");
			socket.off("disconnect");
			socket.off("currency-update");
			socket.off("events-update");
		};
	}, []);

	useEffect(() => {
		if (indices.dateIndex.length > 0 && pageState === "query-first-data-date") {
			setSelectedDate(indices.dateIndex[indices.dateIndex.length - 1]);
			fetchDataByDate(
				indices.dateIndex[indices.dateIndex.length - 1],
				first_pagination_size,
			);
		} else if (
			indices.dateIndex.length > 0 &&
			pageState === "query-data-date"
		) {
			if (
				dataByDate[selectedDate] !== undefined &&
				dataByDate[selectedDate].lastEventRef !== undefined
			) {
				setLastEvent(dataByDate[selectedDate].lastEventRef);
				fetchDataByDate(
					selectedDate,
					first_pagination_size,
					dataByDate[selectedDate].lastEventRef,
				);
			} else {
				setLastEvent(null);
				fetchDataByDate(selectedDate, first_pagination_size, null);
			}
		}
	}, [pageState, selectedCurrency, selectedDate]);

	if (error) {
		return <div>{error}</div>;
	}

	return (
		<div className={`page`} style={{ overflowY: "auto", height: "100vh" }}>
			<div
				style={{
					display: "flex",
					flexDirection: "column",
					alignItems: "center",
					fontFamily: "Arial, sans-serif",
					height: "100%", // Utiliser 100% de la hauteur de la page
					paddingBottom: "20px", // Pour éviter que le dernier élément soit caché
				}}
			>
				<h1>Tendance des monnaies avec les événements mondiaux</h1>

				{/* Onglets de navigation */}
				<Tabs
					value={selectedTab}
					onChange={handleTabChange}
					centered
					style={{ marginTop: "1%" }}
				>
					<Tab label="Graphique en Temps Réel" />
					<Tab label="Graphique Global" />
				</Tabs>

				{/* Sélecteur de devises */}
				<CurrencySelector
					indices={indices}
					selectedCurrency={selectedCurrency}
					handleCurrencyChange={handleCurrencyChange}
					selectedDate={selectedDate}
					handleDateChange={handleDateChange}
					isDateAvailable={isDateAvailable}
				/>

				{/* Vérification de l'état de chargement */}
				{pageState !== "ready" && pageState !== "error" ? (
					<div
						style={{
							display: "flex",
							justifyContent: "center",
							alignItems: "center",
							flexDirection: "column",
							marginTop: "50px",
						}}
					>
						<CircularProgress style={{ margin: "auto" }} />
						<p>Chargement des données...</p>
					</div>
				) : error ? (
					<div>{error}</div>
				) : (
					<>
						{/* Contenu des onglets */}
						{selectedTab === 0 ? (
							<div
								style={{
									display: "flex",
									flexDirection: "column",
									alignItems: "center",
									width: "100%",
								}}
							>
								{/* Graphique Temps Réel */}
								<RealtimeDataChart
									style={{ width: "100%", height: "50vh" }}
									data={realtimeCurrencyData}
									setFilteredDate={setFilteredDate}
									filteredDate={filteredDate}
									selectedCurrency={selectedCurrency}
								/>
								<EventGrid
									style={{ width: "100%", height: "50vh" }}
									events={realtimeTopEventsData}
									selectedDate={selectedDate}
									selectedEvent={selectedEvent}
									setSelectedEvent={setSelectedEvent}
								/>
							</div>
						) : (
							<div
								style={{
									display: "flex",
									flexDirection: "column",
									width: "100%",
									padding: "10px",
									alignItems: "center",
								}}
							>
								{/* Graphique Global */}
								<CurrencyChart
									table={table}
									selectedCurrency={selectedCurrency}
									selectedDate={selectedDate}
									handleDateChange={handleDateChange}
									style={{ width: "100%", height: "100%" }} // Prendre toute la largeur et la hauteur
								/>

								<h2> {selectedDate} </h2>
								<EventsList
									dataByDate={dataByDate}
									selectedDate={selectedDate}
									loadMoreEvents={loadMoreEvents}
									hasMore={hasMore}
									selectedEvent={selectedEvent}
									setSelectedEvent={setSelectedEvent}
								/>
							</div>
						)}
					</>
				)}
			</div>
		</div>
	);
};

export default App;
