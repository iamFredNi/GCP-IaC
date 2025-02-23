import React, { useState, useEffect } from "react";
import {
	Chart as ChartJS,
	CategoryScale,
	LinearScale,
	PointElement,
	LineElement,
	Tooltip,
} from "chart.js";
import { Line } from "react-chartjs-2";

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Tooltip);

const RealtimeDataChart = ({ data, setFilteredDate, filteredDate, theme, selectedCurrency }) => {
	const [colors, setColors] = useState({});

	useEffect(() => {
		const newColors = {};
		Object.keys(data).forEach((currency) => {
			if (Object.keys(colors).includes(currency)) {
				newColors[currency] = colors[currency];
				return;
			}
			newColors[currency] =
				`rgba(${Math.floor(Math.random() * 256)}, ${Math.floor(Math.random() * 256)}, ${Math.floor(Math.random() * 256)}, 1)`;
		});
		setColors(newColors);
	}, [data]);

	if(Object.keys(data).length === 0 || ! Object.keys(data).includes(selectedCurrency)) return <></>;

	const chartData = {
		labels: data[selectedCurrency]
			? data[selectedCurrency].map((entry) =>
					new Date(entry.timestamp).toLocaleTimeString(),
				)
			: [],
		datasets: [ {
			label: selectedCurrency,
			data: data[selectedCurrency].map((entry) => entry.price),
			borderColor: colors[selectedCurrency],
			fill: true,
			pointRadius: 0,
		} ],
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
	};

	return (
		<div 
			style={{ 
				height: "60vh",        // Augmente la hauteur pour agrandir le graphique
				width: "80%",          // Garde la largeur à 80%, ou augmente si besoin
				margin: "0 auto",      // Centre le graphique horizontalement
				display: "flex",       // Utilise flexbox pour centrer le contenu
				justifyContent: "center",  // Centre horizontalement
				alignItems: "center",  // Centre verticalement
			}}
			>
			{Object.keys(data).length > 0 ? (
				<Line data={chartData} options={chartOptions} />
			) : (
				<div>No data available</div>
			)}
		</div>
	);
};

export default RealtimeDataChart;

