# Supply Gate React Frontend

This is the React (JavaScript) frontend for the Supply Gate application.

## Prerequisites

- Node.js (v14 or higher)
- npm or yarn
- Backend Spring Boot application running on `http://localhost:8080` (or configure via `.env`)

## Setup Instructions

1. **Install Dependencies**
   ```bash
   npm install
   ```

2. **Configure Environment Variables**
   
   Create a `.env` file in the root directory:
   ```
   REACT_APP_API_URL=http://localhost:8080
   ```
   
   Adjust the URL if your backend runs on a different port.

3. **Start Development Server**
   ```bash
   npm start
   ```
   
   The app will open at `http://localhost:3000`

4. **Build for Production**
   ```bash
   npm run build
   ```
   
   The production build will be in the `build` folder.

## Project Structure

```
src/
├── components/          # Reusable UI components
│   ├── ui/             # Base UI components (Button, Card, Dialog, etc.)
│   └── Providers.js    # Context providers
├── contexts/           # React contexts (Notifications, etc.)
├── lib/                # Utilities and API configuration
│   ├── api.js         # API client and endpoints
│   └── auth-utils.js  # Authentication utilities
├── pages/              # Page components
│   ├── auth/          # Authentication pages (Login, SignUp, etc.)
│   ├── dashboard/     # Supplier dashboard pages
│   ├── industryDashboard/  # Industry worker dashboard pages
│   └── website/       # Public website pages
└── App.js             # Main app component with routing
```

## Key Features

- ✅ React Router for client-side routing
- ✅ Axios for API communication
- ✅ Tailwind CSS for styling
- ✅ Radix UI components
- ✅ JWT authentication
- ✅ Protected routes
- ✅ Responsive design

## Backend Integration

The frontend communicates with the Spring Boot backend via REST APIs. All API endpoints are configured in `src/lib/api.js`.

**Important:** The backend must be running and CORS must be configured to allow requests from `http://localhost:3000` (or your frontend URL).

## Available Scripts

- `npm start` - Start development server
- `npm run build` - Build for production
- `npm test` - Run tests
- `npm run eject` - Eject from Create React App (irreversible)

## Project Status

This React frontend is fully functional and integrated with the Spring Boot backend. All features have been implemented:

- ✅ Core API configuration and utilities
- ✅ Authentication utilities and hooks
- ✅ UI component library (Button, Card, Dialog, Input, etc.)
- ✅ Routing structure with React Router
- ✅ All authentication pages (Login, SignUp, 2FA, Password Reset)
- ✅ Supplier dashboard with all features
- ✅ Industry worker dashboard
- ✅ Website pages (Landing, Products, Pricing, Support)
- ✅ Protected routes and role-based access

## Notes

- Built with React and JavaScript
- Uses React Router for client-side routing
- Environment variables use `REACT_APP_` prefix

## Troubleshooting

**Build Errors:**
- Ensure all dependencies are installed: `npm install`
- Check Node.js version (v14+ required)
- Clear node_modules and reinstall if needed

**API Connection Issues:**
- Verify backend is running on the configured port
- Check CORS configuration in backend
- Verify `REACT_APP_API_URL` in `.env` file

**Styling Issues:**
- Ensure Tailwind CSS is properly configured
- Check `tailwind.config.js` and `postcss.config.js`
- Verify `index.css` imports Tailwind directives
