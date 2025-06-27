import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom';
import App from './App.tsx'


const rootElement = document.getElementById('root');
if (rootElement) {
  const root = createRoot(rootElement);
  console.log("Rendering App component WITH BrowserRouter in main.tsx...");
  root.render(

      <BrowserRouter>  {}
        <App />
      </BrowserRouter>

  );
  console.log("App component render call with BrowserRouter complete.");
} else {
  console.error("Root element not found!");
}