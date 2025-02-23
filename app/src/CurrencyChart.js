import React from "react";
import { Line } from "react-chartjs-2";

const CurrencyChart = ({
	table,
	selectedCurrency,
	selectedDate,
	handleDateChange,
}) => {
	const chartData = {
		labels: table.map((entry) => {
			const timestamp = entry.timestamp;
			return timestamp.split("T")[0]; // Formater les labels sous 'YYYY-MM-DD'
		  }),
		datasets: [
			{
				label: `${selectedCurrency} Price`,
				data: table.map((entry) => entry.price),
				borderColor: "rgba(75, 192, 192, 1)",
				backgroundColor: "rgba(75, 192, 192, 0.2)",
				fill: true,
				pointRadius: 0,
			},
		],
	};

	const chartOptions = {
		responsive: true,
		plugins: {
			tooltip: {
				enabled: true,
				intersect: false,
				callbacks: {
					label: (tooltipItem) => {
						const price = tooltipItem.raw;
						return `Price: ${price}`;
					},
				},
			},
		},
		scales: {
			x: {
			  type: 'category',
			  labels: chartData.labels,
			  ticks: {
				autoSkip: true,
			  },
			},
			y: {
			  ticks: {
				// Formatage des valeurs sur l'axe Y si nécessaire
			  },
			},
		  },
		onClick: (event, elements, chart) => {
			const points = chart.getElementsAtEventForMode(
				event,
				"nearest",
				{ intersect: false },
				false,
			);
			if (points.length > 0) {
				const hoveredIndex = points[0].index;
				const hoveredDate = chart.data.labels[hoveredIndex];
				const formattedDate = hoveredDate.split("T")[0];
				handleDateChange(formattedDate);
			}
		},
	};

	return (
		<div style={{ 
			height: "70vh",        // Augmente la hauteur pour agrandir le graphique
			width: "100%",          // Garde la largeur à 80%, ou augmente si besoin
			margin: "0 auto",      // Centre le graphique horizontalement
			display: "flex",       // Utilise flexbox pour centrer le contenu
			justifyContent: "center",  // Centre horizontalement
			alignItems: "center",  // Centre verticalement
		  }}>
			{table.length > 0 ? (
			  <Line
				data={chartData}
				options={chartOptions}
				style={{
				  width: "100%",    // Le graphique prend toute la largeur du conteneur
				  height: "100%",   // Le graphique prend toute la hauteur du conteneur
				}}
			  />
			) : (
			  <div>No data available for the selected currency</div>
			)}
		  </div>
	);
};

export default CurrencyChart;
