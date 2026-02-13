import React from 'react';
import { Routes, Route } from 'react-router-dom';
import AppLayout from './components/AppLayout';
import ProtectedRoute from './components/ProtectedRoute';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import ClubsPage from './pages/ClubsPage';
import TeamsPage from './pages/TeamsPage';
import PlayersPage from './pages/PlayersPage';
import CompetitionsPage from './pages/CompetitionsPage';
import FixturesPage from './pages/FixturesPage';
import StandingsPage from './pages/StandingsPage';
import PostsPage from './pages/PostsPage';
import PostDetailPage from './pages/PostDetailPage';
import AdminPostsPage from './pages/AdminPostsPage';
import UsersPage from './pages/UsersPage';
import ContactsPage from './pages/ContactsPage';
import FacilitiesPage from './pages/FacilitiesPage';
import SubscriptionsPage from './pages/SubscriptionsPage';

const App: React.FC = () => {
  return (
    <AppLayout>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/posts" element={<PostsPage />} />
        <Route path="/posts/:id" element={<PostDetailPage />} />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute roles={["ADMIN", "MANAGER", "COACH"]}>
              <DashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/clubs"
          element={
            <ProtectedRoute roles={["ADMIN", "MANAGER"]}>
              <ClubsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/teams"
          element={
            <ProtectedRoute roles={["ADMIN", "MANAGER", "COACH"]}>
              <TeamsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/players"
          element={
            <ProtectedRoute roles={["ADMIN", "MANAGER", "COACH"]}>
              <PlayersPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/competitions"
          element={
            <ProtectedRoute roles={["ADMIN"]}>
              <CompetitionsPage />
            </ProtectedRoute>
          }
        />
        <Route path="/fixtures" element={<FixturesPage />} />
        <Route path="/standings" element={<StandingsPage />} />
        <Route
          path="/admin/posts"
          element={
            <ProtectedRoute roles={["ADMIN"]}>
              <AdminPostsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/contacts"
          element={
            <ProtectedRoute roles={["ADMIN", "MANAGER"]}>
              <ContactsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/facilities"
          element={
            <ProtectedRoute roles={["ADMIN", "MANAGER", "COACH"]}>
              <FacilitiesPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/subscriptions"
          element={
            <ProtectedRoute roles={["ADMIN", "MANAGER"]}>
              <SubscriptionsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/users"
          element={
            <ProtectedRoute roles={["ADMIN"]}>
              <UsersPage />
            </ProtectedRoute>
          }
        />
      </Routes>
    </AppLayout>
  );
};

export default App;
