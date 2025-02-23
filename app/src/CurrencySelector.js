import React from "react";
import { FormControl, Select, MenuItem, InputLabel } from "@mui/material";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";

const CurrencySelector = ({
	indices,
	selectedCurrency,
	handleCurrencyChange,
	theme,
}) => {
	return (
		<FormControl 
			fullWidth 
			style={{ 
				marginBottom: "20px", 
				display: "flex",       // Utilise flexbox
				justifyContent: "center", // Centre horizontalement
				alignItems: "center",  // Centre verticalement
			}}
			>
			<Select
				value={selectedCurrency}
				onChange={(e) => {
				handleCurrencyChange(e.target.value);
				}}
				style={{
				color: "#333",
				width: '50%',         // Ajuste la largeur du sélecteur
				marginTop: '3%',
				}}
			>
				{indices.currencyIndex.map((currency) => (
				<MenuItem key={currency} value={currency}>
					{currency}
				</MenuItem>
				))}
			</Select>
		</FormControl>
	);
};

export default CurrencySelector;
