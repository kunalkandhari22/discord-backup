import React, { useEffect, useState } from "react";
import {
  getUploadRequests,
  type UploadRequest,
} from "../api/uploadRequestApi";
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

export default function UploadRequests() {
  const [uploadRequests, setUploadRequests] = useState<UploadRequest[]>([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(25);
  const [totalRecords, setTotalRecords] = useState(0);

  const fetchUploadRequests = async (pageNumber: number, pageSize: number) => {
    try {
      const res = await getUploadRequests(pageNumber + 1, pageSize);
      setUploadRequests(res.data.data);
      setTotalRecords(res.data.pagination.totalRecords);
    } catch (err) {
      console.log(err)
      alert(getApiErrorMessage(err, "Failed to fetch data"));
    }
  };

  useEffect(() => {
    fetchUploadRequests(page, rowsPerPage);
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
          Upload Requests
        </Typography>
        <Divider sx={{ mb: 3 }} />

        <Button
          variant="contained"
          component={Link}
          to="/upload-request/new"
          sx={{ mb: 3 }}
        >
          Add New Request
        </Button>

        <TableContainer component={Paper} sx={{ borderRadius: 2 }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Input Path</TableCell>
                <TableCell>File Name</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {uploadRequests.map((uploadRequest) => (
                <TableRow key={uploadRequest.requestId} hover>
                  <TableCell>{uploadRequest.requestId}</TableCell>
                  <TableCell>{uploadRequest.inputPath}</TableCell>
                  <TableCell>{uploadRequest.fileName}</TableCell>
                  <TableCell>{uploadRequest.status}</TableCell>
                  <TableCell>
                    <Button
                      component={Link}
                      to={`/upload-request/${uploadRequest.requestId}/edit`}
                      variant="outlined"
                      size="small"
                    >
                      Edit
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
              {uploadRequests.length === 0 && (
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
