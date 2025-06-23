import { Routes, Route } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import ProtectedRoute from './components/ProtectedRoute';
import AdminLayout from './layouts/AdminLayout';
import UserManagementPage from './pages/UserManagementPage';
import PromptManagementPage from './pages/PromptManagementPage';
import ReviewManagementPage from './pages/ReviewManagementPage'; // Import the new page

function App() {
  console.log("App.tsx: App function component executing");
  return (
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route element={<ProtectedRoute />}>
          <Route element={<AdminLayout />}>
            <Route path="/" element={<DashboardPage />} />
            <Route path="/users" element={<UserManagementPage />} />
            <Route path="/prompts" element={<PromptManagementPage />} />
            <Route path="/reviews" element={<ReviewManagementPage />} /> {/* <<<--- ADDED ROUTE FOR REVIEWS */}
          </Route>
        </Route>
      </Routes>
  );
}
export default App;