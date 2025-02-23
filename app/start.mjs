// start.js
import { exec } from "child_process";
import path from "path";
import { fileURLToPath } from "url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Définir la variable d'environnement DIRNAME
process.env.DIRNAME = __dirname;

// Lancer le serveur
const serverProcess = exec("node server.mjs");

serverProcess.stdout.pipe(process.stdout);
serverProcess.stderr.pipe(process.stderr);
