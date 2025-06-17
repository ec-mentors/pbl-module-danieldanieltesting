import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';

// Import Core Libraries for UI Polish
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

// Import Reusable Components
import Navbar from './components/Navbar.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';

// Import Page Components
import HomePage from './pages/HomePage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import PromptsListPage from './pages/PromptsListPage.jsx';
import PromptDetailPage from './pages/PromptDetailPage.jsx';
import CreatePromptPage from './pages/CreatePromptPage.jsx';

function App() {
  return (
    <BrowserRouter>
      {/* 
        The ToastContainer is placed here at the top level.
        It listens for `toast()` calls from anywhere in the app and renders the notification.
      */}
      <ToastContainer
        position="top-right"
        autoClose={3000}
        hideProgressBar={false}
        newestOnTop={false}
        closeOnClick
        rtl={false}
        pauseOnFocusLoss
        draggable
        pauseOnHover
        theme="light"
      />

      <Navbar />

      {/* Main content area with responsive padding */}
      <main className="container mx-auto p-4 sm:p-6">
        <Routes>
          {/* --- Public Routes --- */}
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/prompts" element={<PromptsListPage />} />
          <Route path="/prompts/:id" element={<PromptDetailPage />} />

          {/* --- Protected Routes --- */}
          <Route element={<ProtectedRoute />}>
            <Route path="/create-prompt" element={<CreatePromptPage />} />
            {/* Future protected routes like /profile would go here */}
          </Route>
          
          {/* --- Catch-all 404 Route --- */}
          <Route path="*" element={
            <div className="text-center py-20">
              <h1 className="text-4xl font-bold">404: Page Not Found</h1>
              <p className="mt-4">Sorry, the page you are looking for does not exist.</p>
            </div>
          } />
        </Routes>
      </main>
    </BrowserRouter>
  );
}

export default App;