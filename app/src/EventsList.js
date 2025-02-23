import React from "react";
import InfiniteScroll from "react-infinite-scroll-component";
import { Card, CardContent, Typography, Grid } from "@mui/material";
import { EventCard } from './EventCard';

const EventsList = ({
	dataByDate,
	selectedDate,
	loadMoreEvents,
	hasMore,
	selectedEvent,
	setSelectedEvent,
	theme,
}) => {
	return (
		<div style={{ display: "flex", flexDirection: "column", alignItems: "center", marginTop: "20px" }}>
			<h3
				style={{
					textAlign: "center",
					marginBottom: "20px",
					color: "#333",
				}}
			>
				Événements
			</h3>
			<div
				id="scrollableDiv"
				style={{
					flex: 1,
					padding: "20px",
					maxHeight: "60vh",
					overflowY: "auto",
					backgroundColor: "#f5f5f5",
					borderRadius: "10px",
					boxShadow: "0 4px 10px rgba(0,0,0,0.1)",
					width: "90%", // Ajouté pour contrôler la largeur du conteneur
				}}
			>
				<InfiniteScroll
					dataLength={dataByDate[selectedDate]?.events.length || 0}
					next={loadMoreEvents}
					hasMore={hasMore}
					loader={<h4>Loading...</h4>}
					scrollableTarget="scrollableDiv"
					endMessage={
						<p style={{ textAlign: "center" }}>No more events to load</p>
					}
				>
					<Grid container spacing={2} justifyContent="center">
						{dataByDate[selectedDate]?.events.map((event, index) => {
							return (
								<Grid item xs={12} sm={6} md={4} lg={3} key={event.id}>
									<EventCard
										event={event.transactionData}
										selectedDate={selectedDate}
										theme={theme}
										selectedEvent={selectedEvent}
										setSelectedEvent={setSelectedEvent}
										index={index}
									/>
								</Grid>
							);
						})}
					</Grid>
				</InfiniteScroll>
			</div>
		</div>
	);
};

export default EventsList;
