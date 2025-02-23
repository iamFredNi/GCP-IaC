import { io } from 'socket.io-client';

// "undefined" means the URL will be computed from the `window.location` object
const URL = process.env.NODE_ENV === 'production' ? window.__api_base_url__ : 'http://localhost:5000';

export const socket = io(URL);
