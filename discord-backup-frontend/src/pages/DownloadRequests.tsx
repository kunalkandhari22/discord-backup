import React, { useEffect, useState } from "react";
import {
  getDownloadRequests,
  type DownloadRequest,
} from "../api/downloadRequestApi";
import {
  Typography,
  TableContainer,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  Paper,
  TablePagination,
  Button,
  Box,
  Divider,
} from "@mui/material";
import { Link } from "react-router-dom";
import { getApiErrorMessage } from "../utils/errorUtils";

export default function DownloadRequests() {
  const [downloadRequests, setDownloadRequests] = useState<DownloadRequest[]>([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(25);
  const [totalRecords, setTotalRecords] = useState(0);

  const fetchDownloadRequests = async (pageNumber: number, pageSize: number) => {
    try {
      const res = await getDownloadRequests(pageNumber + 1, pageSize);
      setDownloadRequests(res.data.data);
      setTotalRecords(res.data.pagination.totalRecords);
    } catch (err) {
      console.log(err)
      alert(getApiErrorMessage(err, "Failed to fetch dataa"));
    }
  };

  useEffect(() => {
    fetchDownloadRequests(page, rowsPerPage);
  }, [page, rowsPerPage]);

  const handleChangePage = (_: unknown, newPage: number) => setPage(newPage);
  const handleChangeRowsPerPage = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  return (
    <Box sx={{ maxWidth: 1000, mx: "auto", mt: 4 }}>
      <Paper elevation={3} sx={{ borderRadius: 3, p: 3 }}>
        <Typography variant="h4" mb={2} align="center" fontWeight={600}>
          Download Requests
        </Typography>
        <Divider sx={{ mb: 3 }} />

        <Button
          variant="contained"
          component={Link}
          to="/download-request/new"
          sx={{ mb: 3 }}
        >
          Add New Request
        </Button>

        <TableContainer component={Paper} sx={{ borderRadius: 2 }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Download Directory</TableCell>
                <TableCell>File Name</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {downloadRequests.map((downloadRequest) => (
                <TableRow key={downloadRequest.requestId} hover>
                  <TableCell>{downloadRequest.requestId}</TableCell>
                  <TableCell>{downloadRequest.downloadDir}</TableCell>
                  <TableCell>{downloadRequest.fileName}</TableCell>
                  <TableCell>{downloadRequest.status}</TableCell>
                  <TableCell>
                    <Button
                      component={Link}
                      to={`/download-request/${downloadRequest.requestId}/edit`}
                      variant="outlined"
                      size="small"
                    >
                      Edit
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
              {downloadRequests.length === 0 && (
                <TableRow>
                  <TableCell colSpan={5} align="center">
                    No Requests found.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>

          <TablePagination
            component="div"
            count={totalRecords}
            page={page}
            onPageChange={handleChangePage}
            rowsPerPage={rowsPerPage}
            onRowsPerPageChange={handleChangeRowsPerPage}
            rowsPerPageOptions={[5, 10, 25]}
          />
        </TableContainer>
      </Paper>
    </Box>
  );
}
