import { useCallback, useEffect, useState } from "react";

/**
 * Runs an async API call and tracks its loading, data and error state.
 *
 * Extracted into a hook because every screen needs the same three states and
 * the same "did this component unmount before the request finished" guard.
 * Duplicating that per page is where stale-state bugs come from.
 *
 * @param apiCall  the function to invoke
 * @param options.immediate  run on mount (default) or wait for execute()
 */
export function useApi(apiCall, { immediate = true } = {}) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(immediate);
  const [error, setError] = useState(null);

  const execute = useCallback(
    async (...args) => {
      setLoading(true);
      setError(null);
      try {
        const result = await apiCall(...args);
        setData(result);
        return result;
      } catch (err) {
        setError(err);
        throw err;
      } finally {
        setLoading(false);
      }
    },
    [apiCall]
  );

  useEffect(() => {
    if (!immediate) return;

    // Guards against setting state after unmount, which React warns about and
    // which would leak if the user navigates away mid-request.
    let active = true;
    setLoading(true);
    apiCall()
      .then((result) => active && setData(result))
      .catch((err) => active && setError(err))
      .finally(() => active && setLoading(false));

    return () => {
      active = false;
    };
  }, [apiCall, immediate]);

  return { data, loading, error, execute, refetch: execute };
}