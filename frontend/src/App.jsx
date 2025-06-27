import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

import Navbar from './components/Navbar.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import HomePage from './pages/HomePage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import PromptsListPage from './pages/PromptsListPage.jsx';
import PromptDetailPage from './pages/PromptDetailPage.jsx';
import CreatePromptPage from './pages/CreatePromptPage.jsx';
import EditPromptPage from './pages/EditPromptPage.jsx';
import UserProfilePage from './pages/UserProfilePage.jsx';
import BookmarkedPromptsPage from './pages/BookmarkedPromptsPage.jsx';
import OAuth2RedirectHandler from './pages/OAuth2RedirectHandler.jsx';


import UserCollectionsPage from './pages/UserCollectionsPage.jsx';
import CollectionDetailPage from './pages/CollectionDetailPage.jsx';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
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

        <main className="container mx-auto p-4 sm:p-6">
          <Routes>
            {/* --- Public Routes --- */}
            <Route path="/" element={<HomePage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/prompts" element={<PromptsListPage />} />
            <Route path="/prompts/:id" element={<PromptDetailPage />} />
            <Route path="/profile/:username" element={<UserProfilePage />} />
            <Route path="/oauth2/redirect" element={<OAuth2RedirectHandler />} />

            {/* --- Protected Routes --- */}
            <Route element={<ProtectedRoute />}>
              <Route path="/create-prompt" element={<CreatePromptPage />} />
              <Route path="/prompts/:id/edit" element={<EditPromptPage />} />
              <Route path="/bookmarks" element={<BookmarkedPromptsPage />} />
              {/* --- NEW COLLECTION ROUTES --- */}
              <Route path="/collections" element={<UserCollectionsPage />} />
              <Route path="/collections/:collectionId" element={<CollectionDetailPage />} />
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
    </AuthProvider>
  );
}

export default App;