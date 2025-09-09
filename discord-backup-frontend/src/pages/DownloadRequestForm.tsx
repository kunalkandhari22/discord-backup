import { useEffect, useState } from "react";
import {
  TextField,
  Button,
  Typography,
  Box,
  Paper,
  Grid,
  Divider,
  MenuItem,
} from "@mui/material";
import { useForm } from "react-hook-form";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import {
  getDownloadRequestById,
  addDownloadRequest,
  editDownloadRequest,
  deleteDownloadRequest,
  type DownloadRequest,
} from "../api/downloadRequestApi";
import { getApiErrorMessage } from "../utils/errorUtils";

export default function DownloadRequestForm() {
  const { id } = useParams<{ id: string }>();
  const isEdit = Boolean(id);
  const navigate = useNavigate();

  const [searchParams] = useSearchParams();
  const requestId = searchParams.get("requestId");
  const fileName = searchParams.get("fileName");
  const channelId = searchParams.get("channelId");
  
  const [errors, setErrors] = useState<{
    requestId?: string;
    downloadDir?: string;
    fileName?: string;
    channelId?: string;
  }>({});

  const {
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { isSubmitting },
  } = useForm<DownloadRequest>({
    defaultValues: {
      requestId: Number(requestId) || 0,
      downloadDir: "D:/Personal/backup-folder/dest",
      fileName: fileName || "",
      channelId: channelId || "1360678740031438950",
      status: "INITIATED",
    },
  });

  useEffect(() => {
    if (!id) return;
    getDownloadRequestById(parseInt(id))
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
        await deleteDownloadRequest(parseInt(id));
        alert("Request deleted");
      } else {
        alert("Invalid request");
      }
      navigate("/download-requests");
    } catch (err) {
      alert(getApiErrorMessage(err, "Failed to save data"));
    }
  };

  const onSubmit = async (data: DownloadRequest) => {
    const newErrors: {
      requestId?: string;
      downloadDir?: string;
      fileName?: string;
      channelId?: string;
    } = {};

    setErrors(newErrors);

    
    if (!data.requestId || data.requestId <= 0) newErrors.requestId = "Invalid Request ID";
    if (!data.downloadDir)
      newErrors.downloadDir = "Download directory is required";
    if (!data.fileName) newErrors.fileName = "File name is required";
    if (!data.channelId) newErrors.channelId = "Channel ID is required";

    if (Object.keys(newErrors).length > 0) return;

    try {
      if (isEdit && id) {
        await editDownloadRequest(parseInt(id), data);
        alert("Request updated");
      } else {
        await addDownloadRequest(data);
        alert("Request added");
      }
      navigate("/download-requests");
    } catch (err) {
      alert(getApiErrorMessage(err, "Failed to save data"));
    }
  };

  return (
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
          {isEdit ? "Edit Download Request" : "Add Download Request"}
        </Typography>

        <Grid container spacing={3}>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              label="Request ID"
              fullWidth
              required
              variant="outlined"
              value={watch("requestId") ?? ""}
              onChange={(e) => setValue("requestId", Number(e.target.value))}
              error={!!errors.requestId}
              helperText={errors.requestId}
              disabled={isEdit}
            />
          </Grid>

          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              label="Download Directory"
              fullWidth
              required
              variant="outlined"
              value={watch("downloadDir") ?? ""}
              onChange={(e) => setValue("downloadDir", e.target.value)}
              error={!!errors.downloadDir}
              helperText={errors.downloadDir}
            />
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
              label="Channel ID"
              fullWidth
              multiline
              variant="outlined"
              value={watch("channelId") ?? ""}
              onChange={(e) => setValue("channelId", e.target.value)}
              error={!!errors.channelId}
              helperText={errors.channelId}
            />
          </Grid>

            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                select
                label="Status"
                fullWidth
                value={watch("status") ?? ""}
                onChange={(e) => setValue("status", e.target.value)}
              >
              <MenuItem value="OTHER">OTHER</MenuItem>
              <MenuItem value="INITIATED">INITIATED</MenuItem>
              <MenuItem value="DOWNLOADED">DOWNLOADED</MenuItem>
              <MenuItem value="PROCESSED">PROCESSED</MenuItem>
              </TextField>
            </Grid>
        </Grid>

        <Divider sx={{ my: 4 }} />

        <Box display="flex" justifyContent="center" mt={5} gap={5}>
          <Button
            variant="contained"
            type="submit"
            disabled={isSubmitting}
            size="large"
            sx={{ px: 5, py: 1.5, fontWeight: 600, fontSize: "1.1rem" }}
          >
            {isSubmitting ? "Saving..." : "Save Request"}
          </Button>
          {isEdit && (
            <Button
              variant="outlined"
              onClick={ondelete}
              disabled={isSubmitting}
              size="large"
              color="error"
              sx={{ px: 5, py: 1.5, fontWeight: 600, fontSize: "1.1rem" }}
            >
              {isSubmitting ? "Saving..." : "Delete Request"}
            </Button>
          )}
        </Box>
      </Box>
    </Box>
  );
}
