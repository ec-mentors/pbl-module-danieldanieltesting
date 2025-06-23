// src/stores/authStore.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { jwtDecode } from 'jwt-decode'; // <<<--- Ensure this is installed and imported

interface DecodedToken {
  sub: string; // Subject (username)
  roles: string[]; // Our custom roles claim from the JWT
  iat: number; // Issued at timestamp
  exp: number; // Expiration timestamp
}

interface UserState {
  username: string | null;
  roles: string[];
}

interface AuthState {
  token: string | null;
  user: UserState | null; // Stores decoded user information like username and roles
  isAdmin: boolean;       // True if the authenticated user has ROLE_ADMIN
  setAuth: (token: string | null) => void; // Unified function to set auth state or clear it
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      user: null,
      isAdmin: false,
      setAuth: (tokenInput) => {
        if (!tokenInput) {
          // This case handles logout or clearing auth due to invalid/non-admin token
          set({ token: null, user: null, isAdmin: false });
          return;
        }

        try {
          const decodedToken: DecodedToken = jwtDecode(tokenInput);
          const currentTimeInSeconds = Date.now() / 1000;

          // Check 1: Is the token expired?
          if (decodedToken.exp < currentTimeInSeconds) {
            console.warn('Authentication attempt with an expired token.');
            set({ token: null, user: null, isAdmin: false }); // Treat as logout
            return;
          }

          // Check 2: Does the user have ROLE_ADMIN?
          const userRoles = decodedToken.roles || [];
          const userIsAdmin = userRoles.includes('ROLE_ADMIN');

          if (userIsAdmin) {
            // Admin user successfully authenticated
            set({
              token: tokenInput,
              user: { username: decodedToken.sub, roles: userRoles },
              isAdmin: true,
            });
          } else {
            // Non-admin token presented. For an admin panel, this is an auth failure for this context.
            console.warn('Non-admin user attempted to authenticate with the admin panel.');
            set({ token: null, user: null, isAdmin: false }); // Treat as logout for admin panel
          }
        } catch (error) {
          console.error('Failed to decode token or token structure is invalid:', error);
          set({ token: null, user: null, isAdmin: false }); // Treat as logout on decoding error
        }
      },
    }),
    {
      name: 'admin-auth-storage', // Name for localStorage item
      // By default, persist middleware saves the entire state.
      // On rehydration (app load), if a token exists in localStorage,
      // it would be good practice to have an effect in your App.tsx
      // to call `setAuth(persistedToken)` to re-validate it.
      // For now, this setup primarily relies on login flow to call setAuth.
    }
  )
);