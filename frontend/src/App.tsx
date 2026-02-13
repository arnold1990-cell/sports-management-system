import React from 'react';
import { Routes, Route } from 'react-router-dom';
import AppLayout from './components/AppLayout';
import RoleRoute from './components/RoleRoute';
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
import SubscriptionAnalyticsPage from './pages/SubscriptionAnalyticsPage';
import UnauthorizedPage from './pages/UnauthorizedPage';

const App: React.FC = () => {
  return (
    <AppLayout>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/unauthorized" element={<UnauthorizedPage />} />
        <Route path="/posts" element={<PostsPage />} />
        <Route path="/posts/:id" element={<PostDetailPage />} />
        <Route
          path="/dashboard"
          element={
            <RoleRoute roles={["ADMIN", "MANAGER", "COACH"]}>
              <DashboardPage />
            </RoleRoute>
          }
        />
        <Route
          path="/clubs"
          element={
            <RoleRoute roles={["ADMIN", "MANAGER"]}>
              <ClubsPage />
            </RoleRoute>
          }
        />
        <Route
          path="/teams"
          element={
            <RoleRoute roles={["ADMIN", "MANAGER", "COACH"]}>
              <TeamsPage />
            </RoleRoute>
          }
        />
        <Route
          path="/players"
          element={
            <RoleRoute roles={["ADMIN", "MANAGER", "COACH"]}>
              <PlayersPage />
            </RoleRoute>
          }
        />
        <Route
          path="/competitions"
          element={
            <RoleRoute roles={["ADMIN"]}>
              <CompetitionsPage />
            </RoleRoute>
          }
        />
        <Route path="/fixtures" element={<FixturesPage />} />
        <Route path="/standings" element={<StandingsPage />} />
        <Route
          path="/admin/posts"
          element={
            <RoleRoute roles={["ADMIN"]}>
              <AdminPostsPage />
            </RoleRoute>
          }
        />
        <Route
          path="/contacts"
          element={
            <RoleRoute roles={["ADMIN", "MANAGER"]}>
              <ContactsPage />
            </RoleRoute>
          }
        />
        <Route
          path="/facilities"
          element={
            <RoleRoute roles={["ADMIN", "MANAGER", "COACH"]}>
              <FacilitiesPage />
            </RoleRoute>
          }
        />
        <Route
          path="/subscriptions"
          element={
            <RoleRoute roles={["ADMIN", "MANAGER"]}>
              <SubscriptionsPage />
            </RoleRoute>
          }
        />

        <Route
          path="/subscriptions/analytics"
          element={
            <RoleRoute roles={["ADMIN", "MANAGER"]}>
              <SubscriptionAnalyticsPage />
            </RoleRoute>
          }
        />
        <Route
          path="/admin/users"
          element={
            <RoleRoute roles={["ADMIN"]}>
              <UsersPage />
            </RoleRoute>
          }
        />
      </Routes>
    </AppLayout>
  );
};

export default App;
