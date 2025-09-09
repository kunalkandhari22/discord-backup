import { Routes, Route, Link } from "react-router-dom";
import UploadRequests from "./pages/UploadRequests";
import DownloadRequests from "./pages/DownloadRequests";
import { Container, AppBar, Toolbar, Typography, Button } from "@mui/material";
import UploadRequestForm from "./pages/UploadRequestForm";
import DownloadRequestForm from "./pages/DownloadRequestForm";
import Channels from "./pages/Channels";

export default function App() {
  return (
    <>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" sx={{ flexGrow: 1 }}>
            Discord Backup Application
          </Typography>
          <Button color="inherit" component={Link} to="/upload-requests">
            Upload Requests
          </Button>
          <Button color="inherit" component={Link} to="/download-requests">
            Download Requests
          </Button>
          <Button color="inherit" component={Link} to="/channels">
            Channels
          </Button>
        </Toolbar>
      </AppBar>

      <Container sx={{ mt: 3 }}>
        <Routes>
          <Route path="/" element={<UploadRequests />} />
          <Route path="/upload-requests" element={<UploadRequests />} />
          <Route
            path="/upload-request/new"
            element={<UploadRequestForm />}
          />
          <Route
            path="/upload-request/:id/edit"
            element={<UploadRequestForm />}
          />
          <Route path="/download-requests" element={<DownloadRequests />} />
          <Route
            path="/download-request/new"
            element={<DownloadRequestForm />}
          />
          <Route
            path="/download-request/:id/edit"
            element={<DownloadRequestForm />}
          />
          <Route path="/channels" element={<Channels />} />
        </Routes>
      </Container>
    </>
  );
}
