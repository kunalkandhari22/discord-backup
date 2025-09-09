export type ApiErrorLike = {
  response?: {
    data?: {
      error?: string;
      message?: string;
    };
  };
};

export function isApiErrorLike(error: unknown): error is ApiErrorLike {
  if (
    typeof error === "object" &&
    error !== null
  ) {
    const err = error as Record<string, unknown>;
    if (
      typeof err.response === "object" &&
      err.response !== null
    ) {
      const resp = err.response as Record<string, unknown>;
      if (
        typeof resp.data === "object" &&
        resp.data !== null
      ) {
        const data = resp.data as Record<string, unknown>;
        if (typeof data.error === "string") {
          return true;
        }
      }
    }
  }
  return false;
}

export function getApiErrorMessage(error: unknown, fallback = "Something went wrong"): string {
  if (isApiErrorLike(error)) {
    return (error.response?.data?.error ?? fallback) + "\n" + (error.response?.data?.message ?? "");
  }
  return fallback;
}

