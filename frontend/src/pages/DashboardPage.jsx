import { useCallback } from "react";
import { useNavigate } from "react-router-dom";
import AppShell from "../components/layout/AppShell";
import StateView from "../components/common/StateView";
import Button from "../components/common/Button";
import { useApi } from "../hooks/useApi";
import { fetchPortfolio } from "../api/portfolio";
import { formatZAR } from "../utils/format";

/** Sums the balances of every product of a given type. */
function totalByType(products, type) {
  return products
    .filter((product) => product.productType === type)
    .reduce((sum, product) => sum + Number(product.currentBalance), 0);
}

function MetricCard({ label, value, icon, accent, share }) {
  return (
    <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-margin-lg shadow-[0_4px_6px_-1px_rgba(0,0,0,0.05)] transition-shadow hover:shadow-[0_10px_15px_-3px_rgba(0,0,0,0.1)]">
      <div className="mb-2 flex items-start justify-between">
        <p className="text-label-md text-on-surface-variant">{label}</p>
        <span
          className={`material-symbols-outlined rounded-lg p-1.5 text-[20px] ${accent}`}
        >
          {icon}
        </span>
      </div>
      <h3 className="text-headline-md text-on-surface">{value}</h3>
      {share !== undefined && (
        <div className="mt-4 h-1.5 w-full rounded-full bg-surface-container-high">
          <div
            className={`h-1.5 rounded-full ${
              accent.includes("primary") ? "bg-primary" : "bg-secondary"
            }`}
            style={{ width: `${share}%` }}
          />
        </div>
      )}
    </div>
  );
}

export default function DashboardPage() {
  const navigate = useNavigate();

  // Memoised so useApi's effect does not re-run on every render.
  const loadPortfolio = useCallback(() => fetchPortfolio(), []);
  const { data, loading, error, refetch } = useApi(loadPortfolio);

  const products = data?.products ?? [];
  const total = Number(data?.totalPortfolioValue ?? 0);
  const retirement = totalByType(products, "RETIREMENT");
  const savings = totalByType(products, "SAVINGS");

  // Guard against dividing by zero when a portfolio is empty.
  const share = (part) => (total > 0 ? Math.round((part / total) * 100) : 0);

  return (
    <AppShell title="Dashboard">
      <StateView loading={loading} error={error} onRetry={refetch}>
        {data && (
          <>
            <div>
              <h2 className="text-headline-lg text-on-surface">
                Welcome back, {data.fullName.split(" ")[0]}
              </h2>
              <p className="mt-2 text-body-md text-on-surface-variant">
                Age {data.age} · {products.length} active{" "}
                {products.length === 1 ? "product" : "products"}
              </p>
            </div>

            <div className="grid grid-cols-1 gap-gutter md:grid-cols-2 lg:grid-cols-3">
              <div className="relative overflow-hidden rounded-xl bg-primary p-margin-lg text-on-primary shadow-sm">
                <div className="absolute -right-8 -top-8 h-32 w-32 rounded-full bg-white/10 blur-2xl" />
                <p className="mb-2 text-label-md text-on-primary/80">Total Portfolio Value</p>
                <h3 className="text-headline-lg">{formatZAR(total)}</h3>
                <p className="mt-4 text-label-sm text-secondary-fixed">
                  Across all active products
                </p>
              </div>

              <MetricCard
                label="Retirement Products"
                value={formatZAR(retirement)}
                icon="account_balance"
                accent="bg-primary/10 text-primary"
                share={share(retirement)}
              />

              <MetricCard
                label="Savings Products"
                value={formatZAR(savings)}
                icon="savings"
                accent="bg-secondary/10 text-secondary"
                share={share(savings)}
              />
            </div>

            <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-margin-lg shadow-[0_4px_6px_-1px_rgba(0,0,0,0.05)]">
              <div className="mb-6 flex items-center justify-between">
                <h3 className="text-headline-sm text-on-surface">Your Products</h3>
                <Button variant="outline" icon="add" onClick={() => navigate("/withdrawals/new")} className="py-2">
                  New Withdrawal
                </Button>
              </div>

              <div className="overflow-x-auto">
                <table className="w-full border-collapse text-left">
                  <thead>
                    <tr className="bg-surface-container-low/50">
                      <th className="border-b border-surface-container-high px-4 py-3 text-label-md text-on-surface-variant">
                        Product
                      </th>
                      <th className="border-b border-surface-container-high px-4 py-3 text-label-md text-on-surface-variant">
                        Type
                      </th>
                      <th className="border-b border-surface-container-high px-4 py-3 text-right text-label-md text-on-surface-variant">
                        Balance
                      </th>
                      <th className="border-b border-surface-container-high px-4 py-3 text-right text-label-md text-on-surface-variant">
                        Max Withdrawal (90%)
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {products.map((product) => (
                      <tr
                        key={product.id}
                        className="border-b border-surface-container-high transition-colors hover:bg-surface-container-low/50"
                      >
                        <td className="px-4 py-4 text-body-md font-medium text-on-surface">
                          {product.productName}
                        </td>
                        <td className="px-4 py-4">
                          <span
                            className={`inline-flex rounded-full px-2 py-0.5 text-label-sm ${
                              product.productType === "RETIREMENT"
                                ? "bg-primary/10 text-primary"
                                : "bg-secondary-container text-on-secondary-container"
                            }`}
                          >
                            {product.productType}
                          </span>
                        </td>
                        <td className="px-4 py-4 text-right text-body-md text-on-surface">
                          {formatZAR(product.currentBalance)}
                        </td>
                        <td className="px-4 py-4 text-right text-body-md text-on-surface-variant">
                          {formatZAR(product.maximumWithdrawal)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>

                {products.length === 0 && (
                  <p className="py-12 text-center text-body-md text-on-surface-variant">
                    No products found on this portfolio.
                  </p>
                )}
              </div>
            </div>

            <div className="flex items-start gap-3 rounded-xl border border-secondary-container bg-secondary-container/30 p-margin-md">
              <span className="material-symbols-outlined mt-0.5 text-[20px] text-primary">
                gavel
              </span>
              <div>
                <p className="mb-1 text-label-md text-primary">Withdrawal Rules</p>
                <ul className="list-inside list-disc space-y-1 text-body-md text-on-surface-variant">
                  <li>Retirement withdrawals require an age above 65.</li>
                  <li>A withdrawal may not exceed the available balance.</li>
                  <li>A withdrawal may not exceed 90% of the product balance.</li>
                </ul>
              </div>
            </div>
          </>
        )}
      </StateView>
    </AppShell>
  );
}