import { useCallback, useMemo, useState } from "react";
import AppShell from "../components/layout/AppShell";
import StateView from "../components/common/StateView";
import Button from "../components/common/Button";
import Alert from "../components/common/Alert";
import { useApi } from "../hooks/useApi";
import { fetchWithdrawals, downloadStatement, saveBlob } from "../api/withdrawals";
import { fetchPortfolio } from "../api/portfolio";
import { formatZAR, formatDate } from "../utils/format";

const STATUS_STYLES = {
  COMPLETED: "bg-status-complete-bg text-status-complete-fg",
  PENDING: "bg-status-pending-bg text-status-pending-fg",
  REJECTED: "bg-status-rejected-bg text-status-rejected-fg",
};

const EMPTY_FILTERS = { productId: "", status: "", from: "", to: "" };

export default function WithdrawalHistoryPage() {
  const loadWithdrawals = useCallback(() => fetchWithdrawals(), []);
  const loadPortfolio = useCallback(() => fetchPortfolio(), []);

  const { data: withdrawals, loading, error, refetch } = useApi(loadWithdrawals);
  const { data: portfolio } = useApi(loadPortfolio);

  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const [downloading, setDownloading] = useState(false);
  const [downloadError, setDownloadError] = useState(null);

  function handleFilterChange(event) {
    const { name, value } = event.target;
    setFilters((previous) => ({ ...previous, [name]: value }));
  }

  /**
   * Filters the table client-side for immediate feedback. The CSV export sends
   * the same filters to the server, which applies them in the query — so the
   * downloaded file always matches what is on screen.
   */
  const visible = useMemo(() => {
    if (!withdrawals) return [];
    return withdrawals.filter((item) => {
      if (filters.productId && String(item.productId) !== filters.productId) return false;
      if (filters.status && item.status !== filters.status) return false;
      if (filters.from && new Date(item.requestedAt) < new Date(filters.from)) return false;
      if (filters.to) {
        const end = new Date(filters.to);
        end.setHours(23, 59, 59, 999);
        if (new Date(item.requestedAt) > end) return false;
      }
      return true;
    });
  }, [withdrawals, filters]);

  async function handleDownload() {
    setDownloading(true);
    setDownloadError(null);
    try {
      const { blob, filename } = await downloadStatement(filters);
      saveBlob(blob, filename);
    } catch (err) {
      setDownloadError(err.message ?? "Could not download the statement.");
    } finally {
      setDownloading(false);
    }
  }

  return (
    <AppShell title="Withdrawal History">
      <div className="mb-8 flex items-start justify-between">
        <div>
          <h2 className="text-headline-lg text-on-surface">Withdrawal History</h2>
          <p className="mt-2 text-body-md text-on-surface-variant">
            View and export your withdrawal notices.
          </p>
        </div>
        <Button
          variant="outline"
          icon="download"
          onClick={handleDownload}
          loading={downloading}
          className="py-2"
        >
          {downloading ? "Preparing…" : "Download CSV"}
        </Button>
      </div>

      {downloadError && (
        <div className="mb-6">
          <Alert tone="error" title="Download failed" messages={[downloadError]} />
        </div>
      )}

      <div className="mb-gutter rounded-xl border border-outline-variant bg-surface-container-lowest p-margin-lg">
        <div className="mb-4 flex items-center justify-between">
          <h3 className="text-label-md text-on-surface-variant">Filter</h3>
          <button
            type="button"
            onClick={() => setFilters(EMPTY_FILTERS)}
            className="text-label-md text-primary hover:underline"
          >
            Clear filters
          </button>
        </div>

        <div className="grid grid-cols-1 gap-4 md:grid-cols-4">
          <div>
            <label htmlFor="productId" className="mb-1 block text-label-md text-on-surface-variant">
              Product
            </label>
            <select
              id="productId"
              name="productId"
              value={filters.productId}
              onChange={handleFilterChange}
              className="w-full rounded-lg border border-outline-variant bg-surface-container-low px-3 py-2 text-body-md focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
            >
              <option value="">All products</option>
              {(portfolio?.products ?? []).map((product) => (
                <option key={product.id} value={product.id}>
                  {product.productName}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label htmlFor="status" className="mb-1 block text-label-md text-on-surface-variant">
              Status
            </label>
            <select
              id="status"
              name="status"
              value={filters.status}
              onChange={handleFilterChange}
              className="w-full rounded-lg border border-outline-variant bg-surface-container-low px-3 py-2 text-body-md focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
            >
              <option value="">All statuses</option>
              <option value="COMPLETED">Completed</option>
              <option value="PENDING">Pending</option>
              <option value="REJECTED">Rejected</option>
            </select>
          </div>

          <div>
            <label htmlFor="from" className="mb-1 block text-label-md text-on-surface-variant">
              From
            </label>
            <input
              id="from"
              name="from"
              type="date"
              value={filters.from}
              onChange={handleFilterChange}
              className="w-full rounded-lg border border-outline-variant bg-surface-container-low px-3 py-2 text-body-md focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
            />
          </div>

          <div>
            <label htmlFor="to" className="mb-1 block text-label-md text-on-surface-variant">
              To
            </label>
            <input
              id="to"
              name="to"
              type="date"
              value={filters.to}
              onChange={handleFilterChange}
              className="w-full rounded-lg border border-outline-variant bg-surface-container-low px-3 py-2 text-body-md focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
            />
          </div>
        </div>
      </div>

      <StateView loading={loading} error={error} onRetry={refetch}>
        <div className="overflow-hidden rounded-xl border border-outline-variant bg-surface-container-lowest">
          <div className="overflow-x-auto">
            <table className="w-full border-collapse text-left">
              <thead>
                <tr className="bg-surface-container-low/50">
                  <th className="border-b border-surface-container-high px-4 py-3 text-label-md text-on-surface-variant">
                    Reference
                  </th>
                  <th className="border-b border-surface-container-high px-4 py-3 text-label-md text-on-surface-variant">
                    Product
                  </th>
                  <th className="border-b border-surface-container-high px-4 py-3 text-label-md text-on-surface-variant">
                    Date
                  </th>
                  <th className="border-b border-surface-container-high px-4 py-3 text-right text-label-md text-on-surface-variant">
                    Amount
                  </th>
                  <th className="border-b border-surface-container-high px-4 py-3 text-right text-label-md text-on-surface-variant">
                    Balance After
                  </th>
                  <th className="border-b border-surface-container-high px-4 py-3 text-label-md text-on-surface-variant">
                    Status
                  </th>
                </tr>
              </thead>
              <tbody>
                {visible.map((item) => (
                  <tr
                    key={item.id}
                    className="border-b border-surface-container-high transition-colors hover:bg-surface-container-low/50"
                  >
                    <td className="px-4 py-4 text-body-md font-medium text-on-surface">
                      {item.reference}
                    </td>
                    <td className="px-4 py-4 text-body-md text-on-surface-variant">
                      {item.productName}
                    </td>
                    <td className="px-4 py-4 text-body-md text-on-surface-variant">
                      {formatDate(item.requestedAt)}
                    </td>
                    <td className="px-4 py-4 text-right text-body-md text-on-surface">
                      {formatZAR(item.amount)}
                    </td>
                    <td className="px-4 py-4 text-right text-body-md text-on-surface-variant">
                      {formatZAR(item.balanceAfter)}
                    </td>
                    <td className="px-4 py-4">
                      <span
                        className={`inline-flex rounded-full px-2 py-0.5 text-label-sm ${
                          STATUS_STYLES[item.status] ?? ""
                        }`}
                      >
                        {item.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {visible.length === 0 && (
            <p className="py-16 text-center text-body-md text-on-surface-variant">
              {withdrawals?.length
                ? "No withdrawals match the selected filters."
                : "No withdrawals recorded yet."}
            </p>
          )}
        </div>

        <p className="mt-4 text-label-md text-on-surface-variant">
          Showing {visible.length} of {withdrawals?.length ?? 0} withdrawals
        </p>
      </StateView>
    </AppShell>
  );
}