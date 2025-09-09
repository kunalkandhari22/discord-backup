# Copilot Instructions for AI Agents

## Project Overview
- This is a React + TypeScript frontend using Vite for fast development and HMR.
- Source code is in `src/`, with main entry points in `src/main.tsx` and `src/App.tsx`.
- API communication is handled via Axios, with API logic in `src/api/` (see `axiosConfig.ts`, `channelApi.ts`, etc).
- Pages are in `src/pages/`, each representing a major UI route or feature.
- Utility functions are in `src/utils/`.

## Key Patterns & Conventions
- **Component Structure:** Use functional React components with hooks. Keep logic modular and colocate related code.
- **API Calls:** Use the API modules in `src/api/` for all backend communication. Do not call Axios directly in components.
- **Error Handling:** Use helpers from `src/utils/errorUtils.ts` for consistent error display and handling.
- **Pagination:** Use helpers from `src/utils/paginatedResponse.tsx` for paginated API responses.
- **Type Safety:** All code should use TypeScript types and interfaces. Prefer explicit types for API responses and props.
- **File Naming:** Use PascalCase for components and camelCase for utility files.

## Developer Workflows
- **Install dependencies:** `npm install`
- **Start dev server:** `npm run dev` (runs Vite)
- **Build for production:** `npm run build`
- **Lint:** `npm run lint` (uses ESLint, see `eslint.config.js` for rules)
- **No test suite is present by default.**

## Integration & Data Flow
- **API Integration:** All data flows through the API layer in `src/api/`. Update or add new endpoints here.
- **Component Communication:** Use props and React context as needed. Avoid global state unless necessary.
- **External Dependencies:**
  - React, ReactDOM
  - Axios (for HTTP)
  - Vite (build/dev)
  - ESLint (linting)

## Examples
- To add a new page, create a file in `src/pages/` and add a route in `App.tsx`.
- To add a new API endpoint, add a function in the appropriate file in `src/api/` and export it.
- To handle errors, use `handleApiError` from `src/utils/errorUtils.ts`.

## References
- See `README.md` for Vite/ESLint setup details.
- See `vite.config.ts` and `tsconfig*.json` for build and TypeScript configuration.

---

If you are unsure about a pattern, check for similar examples in the relevant directory before introducing new approaches.
