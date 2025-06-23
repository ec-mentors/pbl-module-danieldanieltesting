import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'; // <<< IMPORT
import App from './App.tsx'
// import './index.css' // Add back if you have global styles

const rootElement = document.getElementById('root');
if (rootElement) {
  const root = createRoot(rootElement);
  console.log("Rendering App component WITH BrowserRouter in main.tsx...");
  root.render(
    // <React.StrictMode> // Keep StrictMode out for this test
      <BrowserRouter>  {/* <<< WRAP App HERE */}
        <App />
      </BrowserRouter>
    // </React.StrictMode>
  );
  console.log("App component render call with BrowserRouter complete.");
} else {
  console.error("Root element not found!");
}