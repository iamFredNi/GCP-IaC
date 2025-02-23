import React from "react";
import { Card, CardContent, Typography, Grid } from "@mui/material";
import { EventCard } from "./EventCard";

export const EventGrid = ({
	events,
	selectedDate,
	theme,
	selectedEvent,
	setSelectedEvent,
}) => {
	if(events.length === 0) {
		return (<div></div>);
	}
	return (
		<Grid container spacing={2} style={{ padding: "20px" }}>
			{events.map((event, index) => (
				<Grid item xs={12} sm={6} md={3} lg={3} key={event.id}>
					<EventCard
						event={event.data.transactionData}
						selectedDate={selectedDate}
						theme={theme}
						selectedEvent={selectedEvent}
						setSelectedEvent={setSelectedEvent}
						index={index}
					/>
				</Grid>
			))}
		</Grid>
	);
};
