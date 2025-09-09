import { useEffect, useState } from "react";
import {
  TextField,
  Button,
  Checkbox,
  Typography,
  FormControlLabel,
  Box,
  Paper,
  Grid,
  Divider,
  TableContainer,
  Table,
  TableRow,
  TableHead,
  TableCell,
  TableBody,
  TablePagination,
  MenuItem,
} from "@mui/material";
import { useForm } from "react-hook-form";
import { useNavigate, useParams } from "react-router-dom";
import {
  getUploadRequestById,
  addUploadRequest,
  editUploadRequest,
  deleteUploadRequest,
  type UploadRequest,
  type UploadedFile,
  getUploadedFiles,
} from "../api/uploadRequestApi";
import { getApiErrorMessage } from "../utils/errorUtils";
import { getAllChannels, type Channel } from "../api/channelApi";

export default function UploadRequestForm() {
  const { id } = useParams<{ id: string }>();
  const isEdit = Boolean(id);
  const navigate = useNavigate();

  const [channels, setChannels] = useState<Channel[]>([]);

  const [errors, setErrors] = useState<{
    inputPath?: string;
    fileName?: string;
    outputDir?: string;
    channelId?: string;
  }>({});

  const {
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { isSubmitting },
  } = useForm<UploadRequest>({
    defaultValues: {
      inputPath: "D:/Personal/backup-folder/source",
      fileName: "",
      isFolder: false,
      outputDir: "D:/Personal/backup-folder/source/split",
      channelId: "",
      status: "INITIATED",
    },
  });

  useEffect(() => {
    const fetchMasterData = async () => {
      try {
        setChannels((await getAllChannels()).data);
      } catch (err) {
        alert(getApiErrorMessage(err, "Failed to fetch data"));
      }
    };
    fetchMasterData();
  }, []);

  useEffect(() => {
    if (!id) return;
    getUploadRequestById(parseInt(id))
      .then((res) => {
        reset({
          ...res.data,
        });
      })
      .catch((err: unknown) =>
        alert(getApiErrorMessage(err, "Failed to fetch data"))
      );
  }, [id, reset]);

  const ondelete = async () => {
    try {
      if (isEdit && id) {
        await deleteUploadRequest(parseInt(id));
        alert("Request deleted");
      } else {
        alert("Invalid request");
      }
      navigate("/upload-requests");
    } catch (err) {
      alert(getApiErrorMessage(err, "Failed to save data"));
    }
  };

  const onSubmit = async (data: UploadRequest) => {
    const newErrors: {
      inputPath?: string;
      fileName?: string;
      outputDir?: string;
      channelId?: string;
    } = {};

    setErrors(newErrors);

    if (!data.inputPath) newErrors.inputPath = "Input path is required";
    if (!data.fileName) newErrors.fileName = "File name is required";
    if (!data.outputDir) newErrors.outputDir = "Output directory is required";
    if (!data.channelId) newErrors.channelId = "Channel ID is required";
    
    if (Object.keys(newErrors).length > 0) return;

    try {
      if (isEdit && id) {
        await editUploadRequest(parseInt(id), data);
        alert("Request updated");
      } else {
        await addUploadRequest(data);
        alert("Request added");
      }
      navigate("/upload-requests");
    } catch (err) {
      alert(getApiErrorMessage(err, "Failed to save data"));
    }
  };

  //Parts
  const [uploadedFiles, setUploadedFiles] = useState<UploadedFile[]>([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(25);
  const [totalRecords, setTotalRecords] = useState(0);

  const fetchUploadedFiles = async (pageNumber: number, pageSize: number) => {
    try {
      if (!id) {
        return;
      }

      const res = await getUploadedFiles(pageNumber + 1, pageSize, id);
      setUploadedFiles(res.data.data);
      setTotalRecords(res.data.pagination.totalRecords);
    } catch (err) {
      console.log(err);
      alert(getApiErrorMessage(err, "Failed to fetch dataa"));
    }
  };

  useEffect(() => {
    fetchUploadedFiles(page, rowsPerPage);
  }, [page, rowsPerPage]);

  const handleChangePage = (_: unknown, newPage: number) => setPage(newPage);

  const handleChangeRowsPerPage = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  return (
    <>
      <Box
        component="form"
        onSubmit={handleSubmit(onSubmit)}
        sx={{ maxWidth: 800, mx: "auto", mt: 4 }}
      >
        <Box
          component={Paper}
          elevation={3}
          sx={{ p: 4, borderRadius: 3, background: "#fafbfc" }}
        >
          <Typography variant="h4" mb={3} align="center" fontWeight={600}>
            {isEdit ? "Edit Upload Request" : "Add Upload Request"}
          </Typography>

          <Grid container spacing={3}>
            <Grid size={{ xs: 12, sm: 12 }} container>
              {isEdit && (
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                    label="Request ID"
                    fullWidth
                    required
                    variant="outlined"
                    value={watch("requestId") ?? ""}
                    disabled
                  />
                </Grid>
              )}

              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField
                  label="Input Path"
                  fullWidth
                  required
                  variant="outlined"
                  value={watch("inputPath") ?? ""}
                  onChange={(e) => setValue("inputPath", e.target.value)}
                  error={!!errors.inputPath}
                  helperText={errors.inputPath}
                />
              </Grid>
            </Grid>

            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="File Name"
                fullWidth
                multiline
                variant="outlined"
                value={watch("fileName") ?? ""}
                onChange={(e) => setValue("fileName", e.target.value)}
                error={!!errors.fileName}
                helperText={errors.fileName}
              />
            </Grid>

            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                label="Output Directory"
                fullWidth
                multiline
                variant="outlined"
                value={watch("outputDir") ?? ""}
                onChange={(e) => setValue("outputDir", e.target.value)}
                error={!!errors.outputDir}
                helperText={errors.outputDir}
              />
            </Grid>

            <Grid size={{ xs: 12, sm: 12 }} container>

              <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                select
                label="Channel ID"
                fullWidth
                value={watch("channelId") ?? ""}
                onChange={(e) => setValue("channelId", e.target.value)}
                error={!!errors.channelId}
                helperText={errors.channelId}
              >
                {
                  channels.map((channel) => (
                    <MenuItem key={channel.channelId} value={channel.channelId}>
                      {channel.channelName}
                    </MenuItem>
                  ))
                }
              </TextField>
            </Grid>

              <Grid size={{ xs: 12, sm: 3 }}>
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={watch("isFolder") ?? false}
                      onChange={(e) => setValue("isFolder", e.target.checked)}
                    />
                  }
                  label="Is Folder"
                />
              </Grid>
            </Grid>

            <Grid size={{ xs: 12, sm: 3 }}>
              <TextField
                select
                label="Status"
                fullWidth
                value={watch("status") ?? ""}
                onChange={(e) => setValue("status", e.target.value)}
              >
                <MenuItem value="OTHER">OTHER</MenuItem>
                <MenuItem value="INITIATED">INITIATED</MenuItem>
                <MenuItem value="SPLITTED">SPLITTED</MenuItem>
                <MenuItem value="PROCESSED">PROCESSED</MenuItem>
              </TextField>
            </Grid>
          </Grid>

          <Divider sx={{ my: 4 }} />

          <Box display="flex" justifyContent="center" mt={2} gap={2}>
            <Button
              variant="contained"
              type="submit"
              disabled={isSubmitting}
              size="large"
              sx={{ px: 1.5, py: 1.5, fontWeight: 600, fontSize: "1rem" }}
            >
              {isSubmitting ? "Saving..." : "Save Request"}
            </Button>
            {isEdit && (
              <>
                <Button
                  variant="outlined"
                  onClick={ondelete}
                  disabled={isSubmitting}
                  size="large"
                  color="error"
                  sx={{ px: 1.5, py: 1.5, fontWeight: 600, fontSize: "1rem" }}
                >
                  {isSubmitting ? "Saving..." : "Delete Request"}
                </Button>
                <Button
                  variant="contained"
                  onClick={() =>
                    navigate("/download-request/new?requestId=" + id + "&fileName=" + watch("fileName") + "&channelId=" + watch("channelId"))
                  }
                  size="large"
                  sx={{ px: 1.5, py: 1.5, fontWeight: 600, fontSize: "1rem" }}
                >
                  Initiate Download Request
                </Button>
              </>
            )}
          </Box>
        </Box>
      </Box>

      {id && (
        <Box
          component="form"
          onSubmit={handleSubmit(onSubmit)}
          sx={{ maxWidth: 800, mx: "auto", mt: 4 }}
        >
          <Box
            component={Paper}
            elevation={3}
            sx={{ p: 4, borderRadius: 3, background: "#fafbfc" }}
          >
            <TableContainer component={Paper} sx={{ borderRadius: 2 }}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Part Number</TableCell>
                    <TableCell>Message ID</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {uploadedFiles.map((uploadedFile) => (
                    <TableRow key={uploadedFile.partNumber} hover>
                      <TableCell>{uploadedFile.partNumber}</TableCell>
                      <TableCell>{uploadedFile.messageId}</TableCell>
                    </TableRow>
                  ))}
                  {uploadedFiles.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={5} align="center">
                        No Files found.
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
          </Box>
        </Box>
      )}
    </>
  );
}
