import { fetchEventsFromSelectedDate } from './eventController.mjs';
import { fetchCurrencyApproximated } from './currencyController.mjs';

export const fetchDailyData = async (db, date, currency) => {
  try {
    const eventsPromise = fetchEventsFromSelectedDate(db, date, null, 1000); // Assurez-vous de définir une taille de page appropriée
    const currencyPromise = fetchCurrencyApproximated(db, currency); // Assurez-vous de définir une taille de page appropriée

    const [eventsData, currencyData] = await Promise.all([eventsPromise, currencyPromise]);

    return {
      date,
      events: eventsData.events,
      currency: currencyData,
    };
  } catch (error) {
    console.error(`Error fetching data for date ${date}:`, error);
    throw error;
  }
};

