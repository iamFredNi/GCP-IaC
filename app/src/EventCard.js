import React from "react";
import { Card, CardContent, Typography, Grid } from "@mui/material";

export const EventCard = ({
	event,
	selectedDate,
	theme,
	selectedEvent,
	setSelectedEvent,
	index,
}) => {
	const { actor1_name, avg_tone, description } = event;

	const mentionIdentifier =
		event.mentions?.[0]?.mention_identifier || "N/A";

	return (
		<Card
			key={event.id}
			style={{
				backgroundColor:
				selectedEvent?.date === selectedDate ? "#e3f2fd" : "#fff",
				border: "1px solid #ddd",
				cursor: "pointer",
				margin: "10px",
				padding: "10px",
				height: "auto", // Laisse la carte ajuster la taille automatiquement
				maxHeight: "250px", // Hauteur maximum de la carte pour qu'elles aient toutes la même taille
				overflow: "hidden", // Évite que le contenu déborde
			}}
			onClick={() => setSelectedEvent(event)}
			>
			<CardContent style={{ height: "100%", display: "flex", flexDirection: "column" }}>
				<Typography variant="h6" style={{ color: "#3b82f6", overflow: "hidden" }}>
				{selectedDate}
				</Typography>
				<Typography variant="body1" style={{ color: "#555" }}>
				<strong>Actor:</strong> {actor1_name ? actor1_name : "N/A"}
				</Typography>
				<Typography variant="body1" style={{ color: "#555" }}>
				<strong>Avg Tone:</strong> {avg_tone ? avg_tone : "N/A"}
				</Typography>
				<Typography variant="body1" style={{ color: "#555" }}>
				<strong>Mention:</strong>{" "}
				<a
					href={mentionIdentifier}
					target="_blank"
					rel="noopener noreferrer"
					style={{
					color: "#3b82f6",
					wordWrap: "break-word",
					overflow: "hidden", 
					textOverflow: "ellipsis",
					display: "inline-block",
					maxWidth: "100%",
					whiteSpace: "normal",
					}}
				>
					{mentionIdentifier}
				</a>
				</Typography>
				<Typography variant="body1" style={{ color: "#555", marginTop: "10px", flexGrow: 1 }}>
				{description}
				</Typography>
			</CardContent>
		</Card>
	);
};
