import React, { useEffect, useState } from "react";
import {
  addChannel,
  deleteChannelApi,
  getChannelsUsingPagination,
  type Channel,
  type CreateChannel,
} from "../api/channelApi";
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
  Dialog,
  DialogTitle,
  Grid,
  DialogContent,
  TextField,
  DialogActions,
} from "@mui/material";
import { getApiErrorMessage } from "../utils/errorUtils";

export default function Channels() {
  const [channels, setChannels] = useState<Channel[]>([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(25);
  const [totalRecords, setTotalRecords] = useState(0);

  const fetchChannels = async (pageNumber: number, pageSize: number) => {
    try {
      const res = await getChannelsUsingPagination(pageNumber + 1, pageSize);
      setChannels(res.data.data);
      setTotalRecords(res.data.pagination.totalRecords);
    } catch (err) {
      console.log(err);
      alert(getApiErrorMessage(err, "Failed to fetch dataa"));
    }
  };

  const deleteChannel = async (channelId: string) => {
    if (!window.confirm("Are you sure you want to delete this channel?"))
      return;
    try {
      await deleteChannelApi(channelId);
      alert("Channel deleted successfully");
      fetchChannels(page, rowsPerPage);
    } catch (err) {
      console.log(err);
      alert(getApiErrorMessage(err, "Failed to delete channel"));
    }
  };

  useEffect(() => {
    fetchChannels(page, rowsPerPage);
  }, [page, rowsPerPage]);

  const handleChangePage = (_: unknown, newPage: number) => setPage(newPage);
  const handleChangeRowsPerPage = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<CreateChannel | null>(null);
  const [errors, setErrors] = useState<{
    serverId?: string;
    channelName?: string;
  }>({});

  const handleClose = () => {
    setOpen(false);
    setEditing(null);
  };

  const handleOpen = () => {
    setEditing({serverId: "1360678677087654228", channelName: ""});
    setOpen(true);
  };

  const handleSave = async () => {
    if (!editing) return;
    const newErrors: { serverId?: string; channelName?: string } = {};
    if (!editing.serverId || editing.serverId.trim() === "") {
      newErrors.serverId = "Channel ID is required";
    }
    if (!editing.channelName || editing.channelName.trim() === "") {
      newErrors.channelName = "Channel Name is required";
    }
    setErrors(newErrors);
    if (Object.keys(newErrors).length > 0) {
      return;
    }
    try {
      await addChannel(editing.serverId, editing.channelName);

      fetchChannels(page, rowsPerPage);

      handleClose();
    } catch (err) {
      alert(getApiErrorMessage(err, "Failed to save data"));
    }
  };

  return (
    <Box sx={{ maxWidth: 1000, mx: "auto", mt: 4 }}>
      <Paper elevation={3} sx={{ borderRadius: 3, p: 3 }}>
        <Typography variant="h4" mb={2} align="center" fontWeight={600}>
          Channels
        </Typography>
        <Divider sx={{ mb: 3 }} />

        <Button variant="contained" sx={{ mb: 3 }} onClick={() => handleOpen()}>
          Add New Channel
        </Button>

        <TableContainer component={Paper} sx={{ borderRadius: 2 }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Channel ID</TableCell>
                <TableCell>Channel Name</TableCell>
                <TableCell>Action</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {channels.map((channel) => (
                <TableRow key={channel.channelId} hover>
                  <TableCell>{channel.channelId}</TableCell>
                  <TableCell>{channel.channelName}</TableCell>
                  <TableCell>
                    <Button
                      onClick={() => deleteChannel(channel.channelId)}
                      variant="outlined"
                      size="small"
                      color="error"
                    >
                      Delete
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
              {channels.length === 0 && (
                <TableRow>
                  <TableCell colSpan={5} align="center">
                    No Channels found.
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

      <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
        <DialogTitle>
          {editing?.serverId ? "Edit Schedule" : "Add Channel"}
        </DialogTitle>
        <DialogContent>
          <Grid container spacing={2} mt={1}>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Server ID"
                fullWidth
                value={editing?.serverId}
                onChange={(e) => {
                  setEditing({ ...editing!, serverId: e.target.value });
                  if (errors.serverId)
                    setErrors({ ...errors, serverId: undefined });
                }}
                error={!!errors.serverId}
                helperText={errors.serverId}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Channel Name"
                fullWidth
                value={editing?.channelName || ""}
                onChange={(e) => {
                  setEditing({ ...editing!, channelName: e.target.value });
                  if (errors.channelName)
                    setErrors({ ...errors, channelName: undefined });
                }}
                error={!!errors.channelName}
                helperText={errors.channelName}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose}>Cancel</Button>
          <Button onClick={handleSave} variant="contained">
            Save
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
